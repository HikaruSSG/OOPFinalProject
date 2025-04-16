import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;

public class Transaction {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    public String timestamp;
    private String type;
    private BigDecimal amount;
    private String bankNumber;
    private String description;

    public Transaction(LocalDateTime timestamp, String type, BigDecimal amount, String bankNumber, String description) {
        this.timestamp = timestamp.format(formatter);
        this.type = type;
        this.amount = amount;
        this.bankNumber = bankNumber;
        this.description = description;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getType() {
        return type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getBankNumber() {
        return bankNumber;
    }

    public String getDescription() {
        return description;
    }

    public void saveToDb() {
        String sql = "INSERT INTO transactions (timestamp, type, amount, bankNumber, description) VALUES (?, ?, ?, ?, ?)";
        String dbFilePath = Bank.getDbFilePath(); // Use Bank's method to get the path
        System.out.println("Database file path: " + dbFilePath);
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + dbFilePath);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, timestamp);
            statement.setString(2, type);
            statement.setDouble(3, amount.doubleValue());
            statement.setString(4, bankNumber);
            statement.setString(5, description);
            statement.executeUpdate();
            System.out.println("Transaction saved to database: " + this.toString());
        } catch (SQLException e) {
            System.err.println("Error saving transaction to database: " + e.getMessage());
            e.printStackTrace(); // Print the stack trace to see the full error
        }
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "timestamp=" + timestamp +
                ", type='" + type + '\'' +
                ", amount=" + amount +
                ", bankNumber='" + bankNumber + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
