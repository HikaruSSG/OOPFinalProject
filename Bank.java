import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.math.RoundingMode;
import java.sql.*;

public class Bank {
    private List<User> users;
    private static String DB_FILE_PATH = "target/bank.db";

    /**
     * Retrieves the list of users.
     * @return List of User objects.
     */
    public List<User> getUsers() {
        return users; // Returns the list of users.
    }

    /**
     * Checks if an admin user exists in the list of users.
     * @return true if an admin exists, false otherwise.
     */
    public boolean adminExists() {
        for (User user : users) { // Iterate through the list of users.
            if (user.isAdmin()) { // Check if the current user is an admin.
                return true; // Return true if an admin is found.
            }
        }
        return false; // Return false if no admin is found.
    }

    /**
     * Constructor for the Bank class. Initializes the user list, database, loads users,
     * and sets up a timer to apply interest daily.
     */
    public Bank() {
        this.users = new ArrayList<>(); // Initialize the user list.
        initializeDatabase(); // Initialize the database.
        loadUsersFromDb(); // Load users from the database.
        applyInterestToAllUsers(); // Apply interest to all users.
        Timer timer = new Timer(); // Create a new Timer object.
        long delay = 0; // Set the initial delay to 0.
        long period = 24 * 60 * 60 * 1000; // Set the period to 24 hours in milliseconds.
        timer.scheduleAtFixedRate(new TimerTask() { // Schedule a task to run at a fixed rate.
            @Override
            public void run() { // Override the run method of TimerTask.
                applyInterestToAllUsers(); // Apply interest to all users.
            }
        }, delay, period); // Schedule the task with the specified delay and period.
    }

    /**
     * Initializes the database by creating the users and transactions tables if they don't exist.
     */
    private void initializeDatabase() {
        System.out.println("Initializing database...");
        try {
            Class.forName("org.sqlite.JDBC"); // Load the SQLite JDBC driver.
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC driver not found: " + e.getMessage()); // Print an error message if the driver is not found.
            System.exit(1); // Exit the program.
        }

        String usersTableSql = "CREATE TABLE IF NOT EXISTS users (" + // SQL for creating the users table.
                "userName TEXT, " +
                "bankNumber TEXT PRIMARY KEY, " +
                "pinNumber TEXT, " +
                "accountType TEXT, " +
                "balance REAL DEFAULT 0.0, " +
                "isAdmin INTEGER DEFAULT 0)";

        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + DB_FILE_PATH); // Create a database connection.
             Statement statement = connection.createStatement()) { // Create a statement.

            statement.executeUpdate(usersTableSql); // Execute the SQL to create the users table.
            System.out.println("Users table created successfully.");
            dropTransactionsTable(connection); // drop existing transaction table if exists
            createTransactionsTable(connection, DB_FILE_PATH); // create new transaction table.

        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage()); // Print an error message if an SQL exception occurs.
            System.exit(1); // Exit the program.
        }
    }

    /**
     * Drops the transactions table if it exists.
     * @param connection The database connection.
     */
    private void dropTransactionsTable(Connection connection) {
        String sql = "DROP TABLE IF EXISTS transactions"; // SQL for dropping the transactions table.
        try (Statement statement = connection.createStatement()) { // Create a statement.
            statement.executeUpdate(sql); // Execute the SQL to drop the transactions table.
            System.out.println("Transactions table dropped successfully.");
        } catch (SQLException e) {
            System.err.println("Error dropping transactions table: " + e.getMessage()); // Print an error message if an SQL exception occurs.
        }
    }

    /**
     * Applies interest to all users in the list.
     */
    private void applyInterestToAllUsers() {
        for (User user : users) { // Iterate through the list of users.
            applyInterest(user.getBankNumber()); // Apply interest to each user.
        }
    }

    /**
     * Creates the transactions table in the database if it doesn't exist.
     * @param connection The database connection.
     * @param dbFilePath The path to the database file.
     * @throws SQLException If an SQL exception occurs.
     */
    private void createTransactionsTable(Connection connection, String dbFilePath) throws SQLException {
        String transactionsTableSql = "CREATE TABLE IF NOT EXISTS transactions (" + // SQL for creating the transactions table.
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "timestamp TEXT NOT NULL, " +
                "type TEXT NOT NULL, " +
                "amount REAL NOT NULL, " +
                "bankNumber TEXT NOT NULL, " +
                "description TEXT" +
                ")";

        try (Statement statement = connection.createStatement()) { // Create a statement.
            statement.executeUpdate(transactionsTableSql); // Execute the SQL to create the transactions table.
            System.out.println("Transactions table created successfully.");
        }
    }

    /**
     * Registers a new user and inserts them into the database.
     * @param userName The user's name.
     * @param pinNumber The user's PIN.
     * @param accountType The user's account type.
     * @param isAdmin Whether the user is an admin.
     * @return The generated bank number, or null if registration fails.
     */
    public String registerUser(String userName, String pinNumber, String accountType, boolean isAdmin) {
        String bankNumber = generateBankNumber(); // Generate a bank number.
        User newUser = new User(userName, bankNumber, pinNumber, accountType, 0.0, isAdmin); // Create a new User object.
        if (insertUserIntoDb(newUser)) { // Insert the user into the database.
            users.add(newUser); // Add the user to the list of users.
            return bankNumber; // Return the bank number.
        } else {
            System.err.println("Failed to register user: " + userName); // Print an error message if registration fails.
            return null; // Return null if registration fails.
        }
    }

    /**
     * Logs in a user by verifying their bank number and PIN.
     * @param bankNumber The user's bank number.
     * @param pinNumber The user's PIN.
     * @return The User object if login is successful, or null otherwise.
     */
    public User login(String bankNumber, String pinNumber) {
        for (User user : users) { // Iterate through the list of users.
            if (user.getBankNumber().equals(bankNumber) && user.getPinNumber().equals(pinNumber)) { // Check if the bank number and PIN match.
                user.setLoggedIn(true); // Set the user's loggedIn status to true.
                Transaction transaction = new Transaction(LocalDateTime.now(), "Login", BigDecimal.ZERO, bankNumber, "User logged in"); // create login transaction
                transaction.saveToDb(); // save the login transaction to db.
                return user; // Return the User object.
            }
        }
        return null; // Return null if login fails.
    }

    /**
     * Logs a transaction.
     * @param bankNumber The bank number associated with the transaction.
     * @param type The transaction type.
     * @param amount The transaction amount.
     * @param description The transaction description.
     */
    public void logTransaction(String bankNumber, String type, BigDecimal amount, String description) {
        Transaction transaction = new Transaction(LocalDateTime.now(), type, amount, bankNumber, description); // Create a new Transaction object.
        saveTransaction(transaction); // Save the transaction.
    }

    /**
     * Saves a transaction to the database.
     * @param transaction The Transaction object to save.
     */
    private void saveTransaction(Transaction transaction) {
        transaction.saveToDb(); // Save the transaction to the database.
    }

    /**
     * Deposits an amount into a user's account.
     * @param bankNumber The user's bank number.
     * @param amount The amount to deposit.
     * @return true if the deposit is successful, false otherwise.
     */
    public boolean deposit(String bankNumber, double amount) {
        User user = findUserInList(bankNumber); // Find the user in the list.
        if (user != null && amount > 0) { // Check if the user exists and the amount is valid.
            BigDecimal depositAmount = new BigDecimal(String.valueOf(amount)); // Convert the amount to a BigDecimal.
            BigDecimal newBalance = user.getBalance().add(depositAmount); // Calculate the new balance.
            if (updateUserBalanceInDb(bankNumber, newBalance)) { // Update the user's balance in the database.
                user.setBalance(newBalance); // Update the user's balance in the list.
                Transaction transaction = new Transaction(LocalDateTime.now(), "Deposit", depositAmount, bankNumber, "Deposit transaction"); // create transaction
                saveTransaction(transaction); // save transaction
                return true; // Return true if the deposit is successful.
            } else {
                System.err.println("Failed to update balance in DB for deposit."); // Print an error message if the update fails.
                return false; // Return false if the update fails.
            }
        } else {
            System.out.println("User not found or invalid amount for deposit."); // Print a message if the user is not found or the amount is invalid.
        }
        return false; // Return false if the deposit fails.
    }

    /**
     * Withdraws an amount from a user's account.
     * @param bankNumber The user's bank number.
     * @param amount The amount to withdraw.
     * @return true if the withdrawal is successful, false otherwise.
     */
    public boolean withdraw(String bankNumber, double amount) {
        User user = findUserInList(bankNumber); // Find the user in the list.
        if (user != null && amount > 0) { // Check if the user exists and the amount is valid.
            BigDecimal withdrawAmount = new BigDecimal(String.valueOf(amount)); // Convert the amount to a BigDecimal.
            if (user.getBalance().compareTo(withdrawAmount) >= 0) { // Check if the user has sufficient balance.
                BigDecimal newBalance = user.getBalance().subtract(withdrawAmount); // Calculate the new balance.
                if (updateUserBalanceInDb(bankNumber, newBalance)) { // Update the user's balance in the database.
                    user.setBalance(newBalance); // Update the user's balance in the list.
                    Transaction transaction = new Transaction(LocalDateTime.now(), "Withdraw", withdrawAmount, bankNumber, "Withdrawal transaction"); // create transaction.
                    saveTransaction(transaction); // save transaction
                    return true; // Return true if the withdrawal is successful.
                } else {
                    System.err.println("Failed to update balance in DB for withdrawal."); // Print an error message if the update fails.
                    return false; // Return false if the update fails.
                }
            } else {
                System.out.println("Insufficient balance for withdrawal."); // Print a message if the user has insufficient balance.
                return false; // Return false if the user has insufficient balance.
            }
        } else {
            System.out.println("User not found or invalid amount for withdrawal."); // Print a message if the user is not found or the amount is invalid.
        }
        return false; // Return false if the withdrawal fails.
    }

    /**
     * Applies interest to a user's account.
     * @param bankNumber The user's bank number.
     * @return true if the interest is applied successfully, false otherwise.
     */
    public boolean applyInterest(String bankNumber) {
        User user = findUserInList(bankNumber); // Find the user in the list.
        if (user != null && user.getAccountType().equalsIgnoreCase("saving") && user.getBalance().compareTo(BigDecimal.ZERO) > 0) { // check account type and balance
            BigDecimal interestRate = new BigDecimal("0.05"); // Set the interest rate.
            BigDecimal interest = user.getBalance().multiply(interestRate).setScale(2, RoundingMode.HALF_UP); // Calculate the interest.
            BigDecimal newBalance = user.getBalance().add(interest); // Calculate the new balance.

            if (updateUserBalanceInDb(bankNumber, newBalance)) { // Update the user's balance in the database.
                user.setBalance(newBalance); // Update the user's balance in the list.
                Transaction transaction = new Transaction(LocalDateTime.now(), "Interest", interest, bankNumber, "Interest applied"); // create transaction.
                saveTransaction(transaction); // save transaction.
                logTransaction(bankNumber, "Interest", interest, "Interest applied"); // log transaction
                return true; // Return true if the interest is applied successfully.
            } else {
                System.err.println("Failed to update balance in DB for interest application."); // Print an error message if the update fails.
                return false; // Return false if the update fails.
            }
        }
        return false; // Return false if the interest application fails.
    }

    /**
     * Changes a user's account type.
     * @param bankNumber The user's bank number.
     * @param newAccountType The new account type.
     * @return true if the account type is changed successfully, false otherwise.
     */
    public boolean changeAccountType(String bankNumber, String newAccountType) {
        User user = findUserInList(bankNumber); // Find the user in the list.
        if (user != null && newAccountType != null && !newAccountType.trim().isEmpty()) { // Check if the user exists and the new account type is valid.
            if (updateUserAccountTypeInDb(bankNumber, newAccountType)) { // Update the user's account type in the database.
                user.setAccountType(newAccountType); // Update the user's account type in the list.
                logTransaction(bankNumber, "Change Account Type", BigDecimal.ZERO, "Account type changed to " + newAccountType); // log transaction
                return true; // Return true if the account type is changed successfully.
            } else {
                System.err.println("Failed to update account type in DB."); // Print an error message if the update fails.
                return false; // Return false if the update fails.
            }
        }
        return false; // Return false if the account type change fails.
    }

    /**
     * Grants admin privileges to a user.
     * @param bankNumber The user's bank number.
     * @return true if admin privileges are granted successfully, false otherwise.
     */
    public boolean grantAdmin(String bankNumber) {
        User user = findUserInList(bankNumber); // Find the user in the list.
        if (user != null) { // Check if the user exists.
            if (updateUserAdminStatusInDb(bankNumber, true)) { // Update the user's admin status in the database.
                user.setAdmin(true); // Update the user's admin status in the list.
                logTransaction(bankNumber, "Grant Admin", BigDecimal.ZERO, "Admin privileges granted"); // log transaction
                return true; // Return true if admin privileges are granted successfully.
            } else {
                System.err.println("Failed to update admin status in DB."); // Print an error message if the update fails.
                return false; // Return false if the update fails.
            }
        }
        return false; // Return false if granting admin privileges fails.
    }

    /**
     * Revokes admin privileges from a user.
     * @param bankNumber The user's bank number.
     * @return true if admin privileges are revoked successfully, false otherwise.
     */
    public boolean revokeAdmin(String bankNumber) {
        User user = findUserInList(bankNumber); // Find the user in the list.
        if (user != null) { // Check if the user exists.
            if (updateUserAdminStatusInDb(bankNumber, false)) { // Update the user's admin status in the database.
                user.setAdmin(false); // Update the user's admin status in the list.
                logTransaction(bankNumber, "Revoke Admin", BigDecimal.ZERO, "Admin privileges revoked"); // log transaction
                return true; // Return true if admin privileges are revoked successfully.
            } else {
                System.err.println("Failed to update admin status in DB."); // Print an error message if the update fails.
                return false; // Return false if the update fails.
            }
        }
        return false; // Return false if revoking admin privileges fails.
    }

    /**
     * Generates a random 6-digit bank number.
     * @return The generated bank number.
     */
    private String generateBankNumber() {
        Random random = new Random(); // Create a new Random object.
        StringBuilder sb = new StringBuilder(); // Create a new StringBuilder object.
        for (int i = 0; i < 6; i++) { // Generate 6 random digits.
            sb.append(random.nextInt(10)); // Append a random digit to the StringBuilder.
        }
        return sb.toString(); // Return the generated bank number.
    }

    /**
     * Loads users from the database into the list of users.
     */
    private void loadUsersFromDb() {
        String sql = "SELECT userName, bankNumber, pinNumber, accountType, balance, isAdmin FROM users"; // SQL for selecting all users.
        List<User> loadedUsers = new ArrayList<>(); // Create a new list to store loaded users.

        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + DB_FILE_PATH); // Create a database connection.
             Statement statement = connection.createStatement(); // Create a statement.
             ResultSet resultSet = statement.executeQuery(sql)) { // Execute the SQL and get the result set.

            while (resultSet.next()) { // Iterate through the result set.
                String userName = resultSet.getString("userName"); // Get the user name.
                String bankNumber = resultSet.getString("bankNumber"); // Get the bank number.
                String pinNumber = resultSet.getString("pinNumber"); // Get the PIN.
                String accountType = resultSet.getString("accountType"); // Get the account type.
                double balance = resultSet.getDouble("balance"); // Get the balance.
                boolean isAdmin = resultSet.getInt("isAdmin") == 1; // Get the admin status.

                User user = new User(userName, bankNumber, pinNumber, accountType, balance, isAdmin); // Create a new User object.
                loadedUsers.add(user); // Add the user to the list of loaded users.
            }
        } catch (SQLException e) {
            System.err.println("Error loading users from database: " + e.getMessage()); // Print an error message if an SQL exception occurs.
        }
        this.users = loadedUsers; // Set the list of users to the loaded users.
    }

    /**
     * Inserts a new user into the database.
     * @param user The User object to insert.
     * @return true if the insertion is successful, false otherwise.
     */
    private boolean insertUserIntoDb(User user) {
        String sql = "INSERT INTO users (userName, bankNumber, pinNumber, accountType, balance, isAdmin) VALUES (?, ?, ?, ?, ?, ?)"; // SQL for inserting a user.
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + DB_FILE_PATH); // Create a database connection.
             PreparedStatement statement = connection.prepareStatement(sql)) { // Create a prepared statement.
            statement.setString(1, user.getUserName()); // Set the user name.
            statement.setString(2, user.getBankNumber()); // Set the bank number.
            statement.setString(3, user.getPinNumber()); // Set the PIN.
            statement.setString(4, user.getAccountType()); // Set the account type.
            statement.setDouble(5, user.getBalance().doubleValue()); // Set the balance.
            statement.setInt(6, user.isAdmin() ? 1 : 0); // Set the admin status.
            statement.executeUpdate(); // Execute the SQL.
            return true; // Return true if the insertion is successful.
        } catch (SQLException e) {
            System.err.println("Error inserting user into database: " + e.getMessage()); // Print an error message if an SQL exception occurs.
            return false; // Return false if the insertion fails.
        }
    }

    /**
     * Updates a user's balance in the database.
     * @param bankNumber The user's bank number.
     * @param newBalance The new balance.
     * @return true if the update is successful, false otherwise.
     */
    private boolean updateUserBalanceInDb(String bankNumber, BigDecimal newBalance) {
        String sql = "UPDATE users SET balance = ? WHERE bankNumber = ?"; // SQL for updating a user's balance.
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + DB_FILE_PATH); // Create a database connection.
             PreparedStatement statement = connection.prepareStatement(sql)) { // Create a prepared statement.
            statement.setDouble(1, newBalance.doubleValue()); // Set the new balance.
            statement.setString(2, bankNumber); // Set the bank number.
            int affectedRows = statement.executeUpdate(); // Execute the SQL.
            return affectedRows > 0; // Return true if the update is successful.
        } catch (SQLException e) {
            System.err.println("Error updating user balance in database: " + e.getMessage()); // Print an error message if an SQL exception occurs.
            return false; // Return false if the update fails.
        }
    }

    /**
     * Updates a user's account type in the database.
     * @param bankNumber The user's bank number.
     * @param accountType The new account type.
     * @return true if the update is successful, false otherwise.
     */
    private boolean updateUserAccountTypeInDb(String bankNumber, String accountType) {
        String sql = "UPDATE users SET accountType = ? WHERE bankNumber = ?"; // SQL for updating a user's account type.
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + DB_FILE_PATH); // Create a database connection.
             PreparedStatement statement = connection.prepareStatement(sql)) { // Create a prepared statement.
            statement.setString(1, accountType); // Set the new account type.
            statement.setString(2, bankNumber); // Set the bank number.
            int affectedRows = statement.executeUpdate(); // Execute the SQL.
            return affectedRows > 0; // Return true if the update is successful.
        } catch (SQLException e) {
            System.err.println("Error updating account type in database: " + e.getMessage()); // Print an error message if an SQL exception occurs.
            return false; // Return false if the update fails.
        }
    }

    /**
     * Updates a user's admin status in the database.
     * @param bankNumber The user's bank number.
     * @param isAdmin The new admin status.
     * @return true if the update is successful, false otherwise.
     */
    private boolean updateUserAdminStatusInDb(String bankNumber, boolean isAdmin) {
        String sql = "UPDATE users SET isAdmin = ? WHERE bankNumber = ?"; // SQL for updating a user's admin status.
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + DB_FILE_PATH); // Create a database connection.
             PreparedStatement statement = connection.prepareStatement(sql)) { // Create a prepared statement.
            statement.setInt(1, isAdmin ? 1 : 0); // Set the new admin status.
            statement.setString(2, bankNumber); // Set the bank number.
            int affectedRows = statement.executeUpdate(); // Execute the SQL.
            return affectedRows > 0; // Return true if the update is successful.
        } catch (SQLException e) {
            System.err.println("Error updating admin status in database: " + e.getMessage()); // Print an error message if an SQL exception occurs.
            return false; // Return false if the update fails.
        }
    }

    /**
     * Finds a user in the list of users by their bank number.
     * @param bankNumber The user's bank number.
     * @return The User object if found, or null otherwise.
     */
    private User findUserInList(String bankNumber) {
        for (User user : users) { // Iterate through the list of users.
            if (user.getBankNumber().equals(bankNumber)) { // Check if the bank numbers match.
                return user; // Return the User object if found.
            }
        }
        return null; // Return null if the user is not found.
    }

    /**
     * Retrieves the transaction history for a user.
     * @param bankNumber The user's bank number.
     * @return A list of Transaction objects.
     */
    public List<Transaction> getTransactionHistory(String bankNumber) {
        List<Transaction> userTransactions = new ArrayList<>(); // Create a new list to store transactions.
        String sql = "SELECT timestamp, type, amount, bankNumber, description FROM transactions WHERE bankNumber = ?"; // SQL for selecting transactions.
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + DB_FILE_PATH); // Create a database connection.
             PreparedStatement statement = connection.prepareStatement(sql)) { // Create a prepared statement.
            statement.setString(1, bankNumber); // Set the bank number.
            ResultSet resultSet = statement.executeQuery(); // Execute the SQL and get the result set.
            while (resultSet.next()) { // Iterate through the result set.
                String timestamp = resultSet.getString("timestamp"); // Get the timestamp.
                String type = resultSet.getString("type"); // Get the transaction type.
                BigDecimal amount = BigDecimal.valueOf(resultSet.getDouble("amount")); // Get the amount.
                String description = resultSet.getString("description"); // Get the description.
                LocalDateTime ldt = LocalDateTime.parse(timestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME); // Parse the timestamp.
                Transaction transaction = new Transaction(ldt, type, amount, bankNumber, description); // Create a new Transaction object.
                transaction.timestamp = timestamp; // set timestamp.
                userTransactions.add(transaction); // Add the transaction to the list.
            }
        } catch (SQLException e) {
            System.err.println("Error getting transaction history from database: " + e.getMessage()); // Print an error message if an SQL exception occurs.
        }
        return userTransactions; // Return the list of transactions.
    }

    /**
     * Gets the database file path.
     * @return The database file path.
     */
    public static String getDbFilePath() {
        return DB_FILE_PATH; // Return the database file path.
    }
}