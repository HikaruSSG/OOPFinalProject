public class Account {
    private String accountType;
    private double balance;

    /**
     * Constructor for the Account class.
     * Initializes a new Account object with the specified account type and balance.
     * @param accountType String - The type of account (e.g., "checking", "saving").
     * @param balance double - The initial balance of the account.
     *
     * Scope: Public constructor to create Account objects.
     * Purpose: To set up a new account with its type and initial balance.
     */
    public Account(String accountType, double balance) {
        /*
         * Scope: Public constructor
         * Purpose: Initializes Account object with account type and balance.
         */
        this.accountType = accountType; // Sets the account type;
        this.balance = balance; // Sets the account balance;
    }

    /**
     * Gets the account type.
     * Returns the type of this account.
     * @return String - The account type.
     *
     * Scope: Public getter method for accountType.
     * Purpose: To retrieve the account type of this Account object.
     */
    public String getAccountType() {
        /*
         * Scope: Public getter
         * Purpose: Returns the account type.
         */
        return accountType; // Returns the account type;
    }

    /**
     * Sets the account type.
     * Modifies the type of this account to the specified account type.
     * @param accountType String - The new account type to set.
     *
     * Scope: Public setter method for accountType.
     * Purpose: To modify or update the account type of this Account object.
     */
    public void setAccountType(String accountType) {
        /*
         * Scope: Public setter
         * Purpose: Sets the account type.
         */
        this.accountType = accountType; // Sets the account type;
    }

    /**
     * Gets the balance of the account.
     * Returns the current balance held in this account.
     * @return double - The account balance.
     *
     * Scope: Public getter method for balance.
     * Purpose: To retrieve the current balance of this Account object.
     */
    public double getBalance() {
        /*
         * Scope: Public getter
         * Purpose: Returns the account balance.
         */
        return balance; // Returns the account balance;
    }

    /**
     * Sets the balance of the account.
     * Updates the account balance to the specified amount.
     * @param balance double - The new balance to set for the account.
     *
     * Scope: Public setter method for balance.
     * Purpose: To modify or update the balance of this Account object.
     */
    public void setBalance(double balance) {
        /*
         * Scope: Public setter
         * Purpose: Sets the account balance.
         */
        this.balance = balance; // Sets the account balance;
    }
}
