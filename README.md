# Banking System

This is a simple banking system implemented in Java.

## Overview

This banking system allows users to create accounts, deposit and withdraw funds, view transaction history, and manage user profiles.

## Classes

### Account.java

This class represents a bank account. It stores account information such as account number, balance, and transaction history.

**Functionalities:**

*   `deposit(double amount)`: Deposits the specified amount into the account.
*   `withdraw(double amount)`: Withdraws the specified amount from the account.
*   `getBalance()`: Returns the current balance of the account.
*   `getTransactionHistory()`: Returns a list of transactions associated with the account.

### Bank.java

This class manages the bank accounts. It provides functionalities to create new accounts, find existing accounts, and perform transactions.

**Functionalities:**

*   `createAccount(User user)`: Creates a new bank account for the given user.
*   `findAccount(int accountNumber)`: Finds an account by its account number.
*   `transferFunds(int fromAccountNumber, int toAccountNumber, double amount)`: Transfers funds from one account to another.

### GUI.java

This class provides a graphical user interface for interacting with the banking system.

**Functionalities:**

*   Displays account information.
*   Allows users to deposit and withdraw funds.
*   Shows transaction history.

### Main.java

This is the main class that starts the banking system.

**Functionalities:**

*   Initializes the bank and creates sample accounts.
*   Starts the GUI.

### Transaction.java

This class represents a transaction. It stores transaction information such as transaction type, amount, and timestamp.

**Functionalities:**

*   `getTransactionType()`: Returns the type of transaction (deposit, withdrawal, transfer).
*   `getAmount()`: Returns the amount of the transaction.
*   `getTimestamp()`: Returns the timestamp of the transaction.

### User.java

This class represents a user of the banking system. It stores user information such as name, address, and contact details.

**Functionalities:**

*   `getName()`: Returns the name of the user.
*   `getAddress()`: Returns the address of the user.
*   `getContactDetails()`: Returns the contact details of the user.

## Usage

1.  Compile the Java files.
2.  Run the `Main` class.
