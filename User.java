import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class User {
    private String userName;
    private String bankNumber;
    private String pinNumber;
    private String accountType;
    private BigDecimal balance;
    private boolean isAdmin;
    private List<Transaction> transactionHistory = new ArrayList<>();

    public User(String userName, String bankNumber, String pinNumber, String accountType, double balance, boolean isAdmin) {
        this.userName = userName;
        this.bankNumber = bankNumber;
        this.pinNumber = pinNumber;
        this.accountType = accountType;
        // Use valueOf for better precision handling compared to new BigDecimal(double)
        this.balance = BigDecimal.valueOf(balance);
        this.isAdmin = isAdmin;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getBankNumber() {
        return bankNumber;
    }

    public void setBankNumber(String bankNumber) {
        this.bankNumber = bankNumber;
    }

    public String getPinNumber() {
        return pinNumber;
    }

    public void setPinNumber(String pinNumber) {
        this.pinNumber = pinNumber;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    private boolean loggedIn;

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public List<Transaction> getTransactionHistory() {
        return transactionHistory;
    }

    public void addTransaction(Transaction transaction) {
        this.transactionHistory.add(transaction);
    }
}
