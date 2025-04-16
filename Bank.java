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
import java.io.File;
import java.net.URISyntaxException;

public class Bank {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private List<User> users;
    private List<Transaction> transactions;
    private static final String DB_FILE_NAME = "bank.db";
    private static String DB_FILE_PATH = "target/bank.db"; // Relative path
    /*static {
        try {
            File jarFile = new File(Bank.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            String parentDir = jarFile.getParent();
            if (parentDir != null) {
                DB_FILE_PATH = parentDir + File.separator + DB_FILE_NAME;
            } else {
                DB_FILE_PATH = DB_FILE_NAME; // Use default if parent directory is null
            }
        } catch (URISyntaxException e) {
            System.err.println("Error getting database path: " + e.getMessage());
            DB_FILE_PATH = DB_FILE_NAME; // Fallback to default
        }
    }*/

    /**
     * Retrieves the list of users in the bank.
     * This method returns the current list of users managed by the bank.
     * @return List<User> - A list of User objects.
     */
    public List<User> getUsers() {
        return users; // Returns the list of users
    }

    public boolean adminExists() {
        for (User user : users) {
            if (user.isAdmin()) {
                return true;
            }
        }
        return false;
    }

    public Bank() {
        this.users = new ArrayList<>();
        initializeDatabase();
        loadUsersFromDb(); // Changed from loadUsersFromCSV
        applyInterestToAllUsers();
        Timer timer = new Timer();
        long delay = 0; // Start immediately
        long period = 24 * 60 * 60 * 1000;
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                applyInterestToAllUsers();
            }
        }, delay, period);
    }

    private void initializeDatabase() {
        System.out.println("Initializing database...");
        // Ensure the JDBC driver is loaded
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC driver not found: " + e.getMessage());
            System.exit(1); // Critical error, cannot proceed
        }

        // Use try-with-resources for Connection and Statement
        String usersTableSql = "CREATE TABLE IF NOT EXISTS users (" +
                "userName TEXT, " +
                "bankNumber TEXT PRIMARY KEY, " +
                "pinNumber TEXT, " +
                "accountType TEXT, " +
                "balance REAL DEFAULT 0.0, " + // Added default value
                "isAdmin INTEGER DEFAULT 0)"; // Added default value

        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + DB_FILE_PATH);
             Statement statement = connection.createStatement()) {

            statement.executeUpdate(usersTableSql);
            System.out.println("Users table created successfully.");
            dropTransactionsTable(connection);
            createTransactionsTable(connection, DB_FILE_PATH);

        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
            // Consider if System.exit is appropriate or if exception should be thrown
            System.exit(1);
        }
    }

    private void dropTransactionsTable(Connection connection) {
        String sql = "DROP TABLE IF EXISTS transactions";
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
            System.out.println("Transactions table dropped successfully.");
        } catch (SQLException e) {
            System.err.println("Error dropping transactions table: " + e.getMessage());
        }
    }

    private void applyInterestToAllUsers() {
        for (User user : users) {
            applyInterest(user.getBankNumber());
        }
    }

    private void createTransactionsTable(Connection connection, String dbFilePath) throws SQLException {
        String transactionsTableSql = "CREATE TABLE IF NOT EXISTS transactions (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "timestamp TEXT NOT NULL, " +
                "type TEXT NOT NULL, " +
                "amount REAL NOT NULL, " +
                "bankNumber TEXT NOT NULL, " +
                "description TEXT" +
                ")";

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(transactionsTableSql);
            System.out.println("Transactions table created successfully.");
        }
    }

    public String registerUser(String userName, String pinNumber, String accountType, boolean isAdmin) {
        String bankNumber = generateBankNumber();
        User newUser = new User(userName, bankNumber, pinNumber, accountType, 0.0, isAdmin);
        if (insertUserIntoDb(newUser)) {
            users.add(newUser);
            return bankNumber;
        } else {
            System.err.println("Failed to register user: " + userName);
            return null;
        }
    }

    public User login(String bankNumber, String pinNumber) {
        for (User user : users) {
            if (user.getBankNumber().equals(bankNumber) && user.getPinNumber().equals(pinNumber)) {
                user.setLoggedIn(true);
                Transaction transaction = new Transaction(LocalDateTime.now(), "Login", BigDecimal.ZERO, bankNumber, "User logged in");
                transaction.saveToDb();
                return user;
            }
        }
        return null;
    }

    public void logTransaction(String bankNumber, String type, BigDecimal amount, String description) {
        Transaction transaction = new Transaction(LocalDateTime.now(), type, amount, bankNumber, description);
        saveTransaction(transaction);
    }

    private void saveTransaction(Transaction transaction) {
        transaction.saveToDb();
    }

    public boolean deposit(String bankNumber, double amount) {
        User user = findUserInList(bankNumber);
        if (user != null && amount > 0) { // Ensure user exists and amount is positive
            BigDecimal depositAmount = new BigDecimal(String.valueOf(amount)); // Use String constructor for precision
            BigDecimal newBalance = user.getBalance().add(depositAmount);
            if (updateUserBalanceInDb(bankNumber, newBalance)) {
                user.setBalance(newBalance); // Update in-memory balance only if DB update succeeds
                Transaction transaction = new Transaction(LocalDateTime.now(), "Deposit", depositAmount, bankNumber, "Deposit transaction");
                saveTransaction(transaction);
                return true;
            } else {
                System.err.println("Failed to update balance in DB for deposit.");
                return false;
            }
        } else {
            System.out.println("User not found or invalid amount for deposit.");
        }
        return false; // User not found or invalid amount
    }

    public boolean withdraw(String bankNumber, double amount) {
        User user = findUserInList(bankNumber);
        if (user != null && amount > 0) { // Ensure user exists and amount is positive
            BigDecimal withdrawAmount = new BigDecimal(String.valueOf(amount)); // Use String constructor
            if (user.getBalance().compareTo(withdrawAmount) >= 0) {
                BigDecimal newBalance = user.getBalance().subtract(withdrawAmount);
                if (updateUserBalanceInDb(bankNumber, newBalance)) {
                    user.setBalance(newBalance); // Update in-memory balance only if DB update succeeds
                    Transaction transaction = new Transaction(LocalDateTime.now(), "Withdraw", withdrawAmount, bankNumber, "Withdrawal transaction");
                    saveTransaction(transaction);
                    return true;
                } else {
                    System.err.println("Failed to update balance in DB for withdrawal.");
                    return false; // DB update failed
                }
            } else {
                System.out.println("Insufficient balance for withdrawal.");
                return false; // Insufficient funds
            }
        } else {
            System.out.println("User not found or invalid amount for withdrawal.");
        }
        return false; // User not found or invalid amount
    }

    public boolean applyInterest(String bankNumber) {
        User user = findUserInList(bankNumber);
        // Apply interest only to saving accounts with a positive balance
        if (user != null && user.getAccountType().equalsIgnoreCase("saving") && user.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal interestRate = new BigDecimal("0.05"); // Consider making this configurable
            BigDecimal interest = user.getBalance().multiply(interestRate).setScale(2, RoundingMode.HALF_UP); // Calculate and round interest
            BigDecimal newBalance = user.getBalance().add(interest);

            if (updateUserBalanceInDb(bankNumber, newBalance)) {
                user.setBalance(newBalance); // Update in-memory balance
                Transaction transaction = new Transaction(LocalDateTime.now(), "Interest", interest, bankNumber, "Interest applied");
                saveTransaction(transaction);
                logTransaction(bankNumber, "Interest", interest, "Interest applied");
                return true;
            } else {
                System.err.println("Failed to update balance in DB for interest application.");
                return false;
            }
        }
        return false; // Not applicable or user not found
    }

    // Changed to use bankNumber for lookup, as it's the primary key
    public boolean changeAccountType(String bankNumber, String newAccountType) {
        User user = findUserInList(bankNumber);
        if (user != null && newAccountType != null && !newAccountType.trim().isEmpty()) {
            if (updateUserAccountTypeInDb(bankNumber, newAccountType)) {
                user.setAccountType(newAccountType); // Update in-memory object
                logTransaction(bankNumber, "Change Account Type", BigDecimal.ZERO, "Account type changed to " + newAccountType);
                return true;
            } else {
                System.err.println("Failed to update account type in DB.");
                return false;
            }
        }
        return false; // User not found or invalid account type
    }

    // Changed to use bankNumber for lookup
    public boolean grantAdmin(String bankNumber) {
        User user = findUserInList(bankNumber);
        if (user != null) {
            if (updateUserAdminStatusInDb(bankNumber, true)) {
                user.setAdmin(true); // Update in-memory object
                logTransaction(bankNumber, "Grant Admin", BigDecimal.ZERO, "Admin privileges granted");
                return true;
            } else {
                System.err.println("Failed to update admin status in DB.");
                return false;
            }
        }
        return false; // User not found
    }

    // Optional: Add a method to revoke admin status
    public boolean revokeAdmin(String bankNumber) {
        User user = findUserInList(bankNumber);
        if (user != null) {
            if (updateUserAdminStatusInDb(bankNumber, false)) {
                user.setAdmin(false); // Update in-memory object
                logTransaction(bankNumber, "Revoke Admin", BigDecimal.ZERO, "Admin privileges revoked");
                return true;
            } else {
                System.err.println("Failed to update admin status in DB.");
                return false;
            }
        }
        return false; // User not found
    }

    private String generateBankNumber() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    private void loadUsersFromDb() {
        String sql = "SELECT userName, bankNumber, pinNumber, accountType, balance, isAdmin FROM users";
        List<User> loadedUsers = new ArrayList<>(); // Load into a temporary list first

        // Use try-with-resources for Connection, Statement, and ResultSet
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + DB_FILE_PATH);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                String userName = resultSet.getString("userName");
                String bankNumber = resultSet.getString("bankNumber");
                String pinNumber = resultSet.getString("pinNumber");
                String accountType = resultSet.getString("accountType");
                double balance = resultSet.getDouble("balance");
                boolean isAdmin = resultSet.getInt("isAdmin") == 1;

                User user = new User(userName, bankNumber, pinNumber, accountType, balance, isAdmin);
                loadedUsers.add(user);
            }
        } catch (SQLException e) {
            System.err.println("Error loading users from database: " + e.getMessage());
            // Consider throwing an exception or handling more gracefully
        }
        // Replace the existing list atomically (improves consistency)
        this.users = loadedUsers;
    }

    // --- Removed the deprecated saveUsersToDb method ---

    // --- Existing specific DB operation methods ---

    private boolean insertUserIntoDb(User user) {
        String sql = "INSERT INTO users (userName, bankNumber, pinNumber, accountType, balance, isAdmin) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + DB_FILE_PATH);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getUserName());
            statement.setString(2, user.getBankNumber());
            statement.setString(3, user.getPinNumber());
            statement.setString(4, user.getAccountType());
            statement.setDouble(5, user.getBalance().doubleValue());
            statement.setInt(6, user.isAdmin() ? 1 : 0);
            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error inserting user into database: " + e.getMessage());
            return false;
        }
    }

    private boolean updateUserBalanceInDb(String bankNumber, BigDecimal newBalance) {
         String sql = "UPDATE users SET balance = ? WHERE bankNumber = ?";
         try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + DB_FILE_PATH);
              PreparedStatement statement = connection.prepareStatement(sql)) {
             statement.setDouble(1, newBalance.doubleValue());
             statement.setString(2, bankNumber);
             int affectedRows = statement.executeUpdate();
             return affectedRows > 0;
         } catch (SQLException e) {
             System.err.println("Error updating user balance in database: " + e.getMessage());
             return false;
         }
     }

     private boolean updateUserAccountTypeInDb(String bankNumber, String accountType) {
         String sql = "UPDATE users SET accountType = ? WHERE bankNumber = ?";
         try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + DB_FILE_PATH);
              PreparedStatement statement = connection.prepareStatement(sql)) {
             statement.setString(1, accountType);
             statement.setString(2, bankNumber);
             int affectedRows = statement.executeUpdate();
             return affectedRows > 0;
         } catch (SQLException e) {
             System.err.println("Error updating account type in database: " + e.getMessage());
             return false;
         }
     }

     private boolean updateUserAdminStatusInDb(String bankNumber, boolean isAdmin) {
         String sql = "UPDATE users SET isAdmin = ? WHERE bankNumber = ?";
         try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + DB_FILE_PATH);
              PreparedStatement statement = connection.prepareStatement(sql)) {
             statement.setInt(1, isAdmin ? 1 : 0);
             statement.setString(2, bankNumber);
             int affectedRows = statement.executeUpdate();
             return affectedRows > 0;
         } catch (SQLException e) {
             System.err.println("Error updating admin status in database: " + e.getMessage());
             return false;
         }
     }

     // Method to find user in the local list (needed after DB updates)
     private User findUserInList(String bankNumber) {
         for (User user : users) {
             if (user.getBankNumber().equals(bankNumber)) {
                 return user;
             }
         }
         return null;
     }

    public List<Transaction> getTransactionHistory(String bankNumber) {
        List<Transaction> userTransactions = new ArrayList<>();
        String sql = "SELECT timestamp, type, amount, bankNumber, description FROM transactions WHERE bankNumber = ?";
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + DB_FILE_PATH);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, bankNumber);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String timestamp = resultSet.getString("timestamp");
                String type = resultSet.getString("type");
                BigDecimal amount = BigDecimal.valueOf(resultSet.getDouble("amount"));
                String description = resultSet.getString("description");
                LocalDateTime ldt = LocalDateTime.parse(timestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                Transaction transaction = new Transaction(ldt, type, amount, bankNumber, description);
                transaction.timestamp = timestamp;
                userTransactions.add(transaction);
            }
        } catch (SQLException e) {
            System.err.println("Error getting transaction history from database: " + e.getMessage());
        }
        return userTransactions;
    }

    public static String getDbFilePath() {
        return DB_FILE_PATH;
    }
}
