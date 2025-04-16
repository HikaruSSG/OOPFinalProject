import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Clipboard;
import java.awt.Toolkit;

/**
 * GUI class for the Banking System.
 * This class creates the graphical user interface for the banking application,
 * handling user interactions and displaying bank functionalities.
 */
public class GUI extends JFrame {

    private Bank bank;
    private User loggedInUser;
    private JLabel userDetailsLabel;

    /**
     * Constructor for GUI class.
     * Initializes the GUI components, sets up the main frame, and creates panels for different functionalities.
     * @param bank The Bank object to interact with.
     */
    public GUI(Bank bank) {
        this.bank = bank;
        setTitle("Banking System"); // Sets the title of the JFrame
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Sets default close operation
        setSize(400, 300); // Sets the size of the JFrame
        setResizable(false); // Disable window resizing
        setLayout(new BorderLayout()); // Sets the layout manager

        // User Details Label
        userDetailsLabel = new JLabel("Not logged in"); // Initializes user details label
        userDetailsLabel.setHorizontalAlignment(SwingConstants.CENTER); // Centers the text horizontally
        add(userDetailsLabel, BorderLayout.NORTH); // Adds label to the north region

        // Main Panel (Card Layout)
        JPanel mainPanel = new JPanel(new CardLayout()); // Main panel using CardLayout
        add(mainPanel, BorderLayout.CENTER); // Adds main panel to the center

        // Card Layout
        CardLayout cardLayout = (CardLayout) mainPanel.getLayout(); // Gets the CardLayout

        // Login Panel
        JPanel loginPanel = createLoginForm(mainPanel, cardLayout); // Creates login panel
        mainPanel.add(loginPanel, "login"); // Adds login panel to main panel

        // Registration Panel
        JPanel registrationPanel = createUserRegistrationForm(mainPanel, cardLayout); // Creates registration panel
        mainPanel.add(registrationPanel, "register"); // Adds registration panel to main panel

        // Main Menu Panel
        JPanel mainMenuPanel = createMainMenu(mainPanel, cardLayout); // Creates main menu panel
        System.out.println("Main menu panel created: " + (mainMenuPanel != null));
        mainPanel.add(mainMenuPanel, "mainMenu"); // Adds main menu panel to main panel
        System.out.println("Main menu panel added to mainPanel");

        // Initial Panel
        JPanel initialPanel = new JPanel(); // Initial panel
        JButton loginButton = new JButton("Login"); // Login button
        loginButton.setPreferredSize(new Dimension(90, 25));
        JButton registerButton = new JButton("Register"); // Register button
        registerButton.setPreferredSize(new Dimension(90, 25));
        initialPanel.add(loginButton); // Adds login button to initial panel
        initialPanel.add(registerButton); // Adds register button to initial panel
        mainPanel.add(initialPanel, "initial"); // Adds initial panel to main panel

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(mainPanel, "login"); // Shows login panel on button click
            }
        });

        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(mainPanel, "register"); // Shows registration panel on button click
            }
        });

        cardLayout.show(mainPanel, "initial"); // Shows initial panel at start

        setVisible(true); // Sets the GUI to be visible
    }

    private JPanel createLoginForm(JPanel mainPanel, CardLayout cardLayout) {
        JPanel loginPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel bankNumberLabel = new JLabel("Bank Number:");
        JTextField bankNumberField = new JTextField();
        bankNumberField.setPreferredSize(new Dimension(150, 25));
        JLabel pinNumberLabel = new JLabel("Pin Number:");
        JPasswordField pinNumberField = new JPasswordField();
        pinNumberField.setPreferredSize(new Dimension(150, 25));
        JButton loginButton = new JButton("Login");
        loginButton.setPreferredSize(new Dimension(90, 25));
        JButton backButton = new JButton("Back");
        backButton.setPreferredSize(new Dimension(90, 25));

        gbc.gridx = 0;
        gbc.gridy = 0;
        loginPanel.add(bankNumberLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        loginPanel.add(bankNumberField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        loginPanel.add(pinNumberLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        loginPanel.add(pinNumberField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        loginPanel.add(loginButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        loginPanel.add(backButton, gbc);

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String bankNumber = bankNumberField.getText();
                String pinNumber = new String(pinNumberField.getPassword());
                loggedInUser = bank.login(bankNumber, pinNumber);
                if (loggedInUser != null) {
                    displayUserDetails();
                    if (loggedInUser.isAdmin()) {
                        JPanel adminMenuPanel = createAdminMenu(mainPanel, cardLayout);
                        mainPanel.add(adminMenuPanel, "adminMenu");
                        cardLayout.show(mainPanel, "adminMenu");
                    } else {
                        cardLayout.show(mainPanel, "mainMenu");
                    }
                } else {
                    JOptionPane.showMessageDialog(GUI.this, "Invalid credentials.");
                }
            }
        });

        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(mainPanel, "initial");
            }
        });

        return loginPanel;
    }

    private JPanel createUserRegistrationForm(JPanel mainPanel, CardLayout cardLayout) {
        JPanel registrationPanel = new JPanel(new GridLayout(6, 2));
        JLabel userNameLabel = new JLabel("User Name:");
        JTextField userNameField = new JTextField();
        userNameField.setPreferredSize(new Dimension(150, 25));
        JLabel pinNumberLabel = new JLabel("Pin Number:");
        JPasswordField pinNumberField = new JPasswordField();
        pinNumberField.setPreferredSize(new Dimension(150, 25));
        JLabel accountTypeLabel = new JLabel("Account Type:");
        String[] accountTypes = {"saving", "checking"};
        JComboBox<String> accountTypeComboBox = new JComboBox<>(accountTypes);
        accountTypeComboBox.setPreferredSize(new Dimension(150, 25));
        JLabel isAdminLabel = new JLabel("Admin:");
        JCheckBox isAdminCheckBox = new JCheckBox();
        boolean adminExists = bank.adminExists();
        if (adminExists) {
            isAdminCheckBox.setVisible(false);
            isAdminLabel.setVisible(false);
        }
        JLabel bankNumberLabel = new JLabel("Bank Number:");
        JLabel bankNumberValueLabel = new JLabel("");
        JButton registerButton = new JButton("Register");
        registerButton.setPreferredSize(new Dimension(90, 25));
        JButton backButton = new JButton("Back");
        backButton.setPreferredSize(new Dimension(90, 25));

        registrationPanel.add(userNameLabel);
        registrationPanel.add(userNameField);
        registrationPanel.add(pinNumberLabel);
        registrationPanel.add(pinNumberField);
        registrationPanel.add(accountTypeLabel);
        registrationPanel.add(accountTypeComboBox);
        registrationPanel.add(isAdminLabel);
        registrationPanel.add(isAdminCheckBox);
        registrationPanel.add(bankNumberLabel);
        registrationPanel.add(bankNumberValueLabel);
        registrationPanel.add(registerButton);
        registrationPanel.add(backButton);

        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String userName = userNameField.getText();
                String pinNumber = new String(pinNumberField.getPassword());
                String accountType = (String) accountTypeComboBox.getSelectedItem();
                boolean isAdmin = isAdminCheckBox.isSelected();
                String bankNumber = bank.registerUser(userName, pinNumber, accountType, isAdmin);

                if (bankNumber != null) {
                    bankNumberValueLabel.setText(bankNumber);
                    // Create a custom panel for the dialog
                    JPanel messagePanel = new JPanel(new BorderLayout(5, 5));
                messagePanel.add(new JLabel("User registered successfully. Your bank number is: " + bankNumber), BorderLayout.CENTER);

                JButton copyButton = new JButton("Copy Bank Number");
                copyButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        StringSelection stringSelection = new StringSelection(bankNumber);
                        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                        clipboard.setContents(stringSelection, null);
                        // Optionally provide feedback that copy was successful
                        // ((JButton)e.getSource()).setText("Copied!");
                        // ((JButton)e.getSource()).setEnabled(false);
                        // Optionally provide feedback that copy was successful
                        // ((JButton)e.getSource()).setText("Copied!");
                        // ((JButton)e.getSource()).setEnabled(false);
                    }
                });
                messagePanel.add(copyButton, BorderLayout.SOUTH);

                JOptionPane.showMessageDialog(GUI.this, messagePanel, "Registration Successful", JOptionPane.INFORMATION_MESSAGE);
                cardLayout.show(mainPanel, "login");
                } else {
                     JOptionPane.showMessageDialog(GUI.this, "Registration failed. Please try again.", "Registration Error", JOptionPane.ERROR_MESSAGE);
                     // Clear fields maybe?
                     userNameField.setText("");
                     pinNumberField.setText("");
                }
            }
        });

        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(mainPanel, "initial");
            }
        });

        return registrationPanel;
    }

    private JPanel createMainMenu(JPanel mainPanel, CardLayout cardLayout) {
        System.out.println("Creating main menu panel...");
        JPanel mainMenuPanel = new JPanel(new BorderLayout());
        JPanel buttonPanel = new JPanel(new GridLayout(3, 2)); // Changed to GridLayout
        JButton depositButton = new JButton("Deposit");
        depositButton.setPreferredSize(new Dimension(150, 25));
        JButton withdrawButton = new JButton("Withdraw");
        withdrawButton.setPreferredSize(new Dimension(150, 25));
        //JButton transactionHistoryButton = new JButton("Transaction History (Text)");
        //transactionHistoryButton.setPreferredSize(new Dimension(150, 25));
        JButton transactionHistoryButtonGUI = new JButton("Transaction History");
        transactionHistoryButtonGUI.setPreferredSize(new Dimension(150, 25));
        JButton logoutButton = new JButton("Logout");
        logoutButton.setPreferredSize(new Dimension(150, 25));

        buttonPanel.add(depositButton);
        buttonPanel.add(withdrawButton);
        //buttonPanel.add(transactionHistoryButton);
        buttonPanel.add(transactionHistoryButtonGUI);
        buttonPanel.add(logoutButton);

        JTextArea transactionHistoryTextArea = new JTextArea();
        transactionHistoryTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(transactionHistoryTextArea);

        transactionHistoryButtonGUI.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (loggedInUser != null) {
                    List<Transaction> transactionHistory = bank.getTransactionHistory(loggedInUser.getBankNumber());

                    // Prepare data for the table
                    String[] columnNames = {"Timestamp", "Type", "Amount", "Description"};
                    Object[][] data = new Object[transactionHistory.size()][4];
                    for (int i = 0; i < transactionHistory.size(); i++) {
                        Transaction transaction = transactionHistory.get(i);
                        data[i][0] = transaction.getTimestamp();
                        data[i][1] = transaction.getType();
                        data[i][2] = transaction.getAmount();
                        data[i][3] = transaction.getDescription();
                    }

                    // Create the table
                    JTable transactionTable = new JTable(data, columnNames);
                    JScrollPane scrollPane = new JScrollPane(transactionTable);

                    // Display the table in a JOptionPane
                    JOptionPane.showMessageDialog(GUI.this, scrollPane, "Transaction History", JOptionPane.PLAIN_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(GUI.this, "Please log in to view transaction history.", "Authentication Required", JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        mainMenuPanel.add(buttonPanel, BorderLayout.NORTH);
        mainMenuPanel.add(scrollPane, BorderLayout.CENTER);

      /*  transactionHistoryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                List<Transaction> transactionHistory = bank.getTransactionHistory(loggedInUser.getBankNumber());

                // Prepare data for the table
                String[] columnNames = {"Timestamp", "Type", "Amount", "Description"};
                Object[][] data = new Object[transactionHistory.size()][4];
                for (int i = 0; i < transactionHistory.size(); i++) {
                    Transaction transaction = transactionHistory.get(i);
                    data[i][0] = transaction.getTimestamp();
                    data[i][1] = transaction.getType();
                    data[i][2] = transaction.getAmount();
                    data[i][3] = transaction.getDescription();
                }

                // Create the table
                JTable transactionTable = new JTable(data, columnNames);
                JScrollPane scrollPane = new JScrollPane(transactionTable);

                // Display the table in a JOptionPane
                JOptionPane.showMessageDialog(GUI.this, scrollPane, "Transaction History", JOptionPane.PLAIN_MESSAGE);
            }
        });*/

        depositButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String amountString = JOptionPane.showInputDialog(GUI.this, "Enter amount to deposit:");
                if (amountString != null && !amountString.isEmpty()) {
                    try {
                        double amount = Double.parseDouble(amountString);
                        if (amount <= 0) {
                             JOptionPane.showMessageDialog(GUI.this, "Deposit amount must be positive.", "Invalid Amount", JOptionPane.WARNING_MESSAGE);
                             return;
                        }
                        boolean success = bank.deposit(loggedInUser.getBankNumber(), amount);
                        if (success) {
                            displayUserDetails();
                            JOptionPane.showMessageDialog(GUI.this, "Deposit successful.");
                        } else {
                            JOptionPane.showMessageDialog(GUI.this, "Deposit failed. Please try again.", "Deposit Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(GUI.this, "Invalid amount format.", "Input Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        withdrawButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String amountString = JOptionPane.showInputDialog(GUI.this, "Enter amount to withdraw:");
                if (amountString != null && !amountString.isEmpty()) {
                    try {
                        double amount = Double.parseDouble(amountString);
                         if (amount <= 0) {
                             JOptionPane.showMessageDialog(GUI.this, "Withdrawal amount must be positive.", "Invalid Amount", JOptionPane.WARNING_MESSAGE);
                             return;
                         }
                        boolean success = bank.withdraw(loggedInUser.getBankNumber(), amount);
                        if (success) {
                            displayUserDetails();
                            JOptionPane.showMessageDialog(GUI.this, "Withdrawal successful.");
                        } else {
                            // Bank class handles the "Insufficient balance" message,
                            // but we might need a more specific error from the bank method later.
                            // For now, a generic failure message if success is false.
                             JOptionPane.showMessageDialog(GUI.this, "Withdrawal failed. Check balance or try again.", "Withdrawal Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(GUI.this, "Invalid amount format.", "Input Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loggedInUser = null;
                userDetailsLabel.setText("Not logged in");
                cardLayout.show(mainPanel, "initial");
            }
        });

        return mainMenuPanel;
    }

    private JPanel createAdminMenu(JPanel mainPanel, CardLayout cardLayout) {
        JPanel adminMenuPanel = new JPanel();
        JButton depositButton = new JButton("Deposit");
        depositButton.setPreferredSize(new Dimension(150, 25));
        JButton withdrawButton = new JButton("Withdraw");
        withdrawButton.setPreferredSize(new Dimension(150, 25));
        JButton manageUsersButton = new JButton("Manage Users");
        manageUsersButton.setPreferredSize(new Dimension(150, 25));
        JButton logoutButton = new JButton("Logout");
        logoutButton.setPreferredSize(new Dimension(150, 25));

        adminMenuPanel.add(depositButton);
        adminMenuPanel.add(withdrawButton);
        adminMenuPanel.add(manageUsersButton);
        adminMenuPanel.add(logoutButton);

        depositButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String amountString = JOptionPane.showInputDialog(GUI.this, "Enter amount to deposit:");
                if (amountString != null && !amountString.isEmpty()) {
                    try {
                        double amount = Double.parseDouble(amountString);
                         if (amount <= 0) {
                             JOptionPane.showMessageDialog(GUI.this, "Deposit amount must be positive.", "Invalid Amount", JOptionPane.WARNING_MESSAGE);
                             return;
                         }
                        boolean success = bank.deposit(loggedInUser.getBankNumber(), amount);
                         if (success) {
                            displayUserDetails();
                            JOptionPane.showMessageDialog(GUI.this, "Deposit successful.");
                        } else {
                            JOptionPane.showMessageDialog(GUI.this, "Deposit failed. Please try again.", "Deposit Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(GUI.this, "Invalid amount format.", "Input Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        withdrawButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String amountString = JOptionPane.showInputDialog(GUI.this, "Enter amount to withdraw:");
                if (amountString != null && !amountString.isEmpty()) {
                    try {
                        double amount = Double.parseDouble(amountString);
                         if (amount <= 0) {
                             JOptionPane.showMessageDialog(GUI.this, "Withdrawal amount must be positive.", "Invalid Amount", JOptionPane.WARNING_MESSAGE);
                             return;
                         }
                        boolean success = bank.withdraw(loggedInUser.getBankNumber(), amount);
                         if (success) {
                            displayUserDetails();
                            JOptionPane.showMessageDialog(GUI.this, "Withdrawal successful.");
                        } else {
                             JOptionPane.showMessageDialog(GUI.this, "Withdrawal failed. Check balance or try again.", "Withdrawal Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(GUI.this, "Invalid amount format.", "Input Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        manageUsersButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JPanel manageUsersPanel = createManageUsersPanel(mainPanel, cardLayout);
                mainPanel.add(manageUsersPanel, "manageUsers");
                cardLayout.show(mainPanel, "manageUsers");
            }
        });

        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loggedInUser = null;
                userDetailsLabel.setText("Not logged in");
                cardLayout.show(mainPanel, "initial");
            }
        });

        return adminMenuPanel;
    }

    private JPanel createManageUsersPanel(JPanel mainPanel, CardLayout cardLayout) {
        JPanel manageUsersPanel = new JPanel(new BorderLayout());

        // Add Bank Number column
        String[] columnNames = {"User Name", "Bank Number", "Account Type", "Is Admin"};
        List<User> userList = bank.getUsers(); // Get user list once
        Object[][] data = new Object[userList.size()][4];
        for (int i = 0; i < userList.size(); i++) {
            User user = userList.get(i);
            data[i][0] = user.getUserName();
            data[i][1] = user.getBankNumber(); // Add bank number
            data[i][2] = user.getAccountType();
            data[i][3] = user.isAdmin(); // Add admin status
        }

        // Make table non-editable
        JTable usersTable = new JTable(data, columnNames) {
             @Override
            public boolean isCellEditable(int row, int column) {
               //all cells false
               return false;
            }
        };
        JScrollPane scrollPane = new JScrollPane(usersTable);
        manageUsersPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton changeAccountTypeButton = new JButton("Change Account Type");
        changeAccountTypeButton.setPreferredSize(new Dimension(150, 25));
        JButton grantAdminButton = new JButton("Grant Admin");
        grantAdminButton.setPreferredSize(new Dimension(150, 25));
        JButton backButton = new JButton("Back");
        backButton.setPreferredSize(new Dimension(150, 25));
        buttonPanel.add(changeAccountTypeButton);
        buttonPanel.add(grantAdminButton);
        buttonPanel.add(backButton);
        manageUsersPanel.add(buttonPanel, BorderLayout.SOUTH);

        changeAccountTypeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = usersTable.getSelectedRow();
                if (selectedRow != -1) {
                    // Get bankNumber from the selected row (column index 1)
                    String bankNumber = (String) usersTable.getValueAt(selectedRow, 1);
                    String currentUserName = (String) usersTable.getValueAt(selectedRow, 0); // For display

                    JPanel accountTypePanel = new JPanel(new GridLayout(0, 1));
                    JRadioButton savingRadioButton = new JRadioButton("saving");
                    JRadioButton checkingRadioButton = new JRadioButton("checking");
                    ButtonGroup accountTypeGroup = new ButtonGroup();
                    accountTypeGroup.add(savingRadioButton);
                    accountTypeGroup.add(checkingRadioButton);
                    accountTypePanel.add(savingRadioButton);
                    accountTypePanel.add(checkingRadioButton);
                    // Pre-select current type (optional but helpful)
                    String currentType = (String) usersTable.getValueAt(selectedRow, 2);
                    if ("saving".equalsIgnoreCase(currentType)) savingRadioButton.setSelected(true);
                    else if ("checking".equalsIgnoreCase(currentType)) checkingRadioButton.setSelected(true);

                    int result = JOptionPane.showConfirmDialog(GUI.this, accountTypePanel, "Select new account type for " + currentUserName, JOptionPane.OK_CANCEL_OPTION);
                    if (result == JOptionPane.OK_OPTION) {
                        String newAccountType = null;
                        if (savingRadioButton.isSelected()) {
                            newAccountType = "saving";
                        } else if (checkingRadioButton.isSelected()) {
                            newAccountType = "checking";
                        }

                        if (newAccountType != null) {
                            // Use bankNumber to change account type
                            boolean success = bank.changeAccountType(bankNumber, newAccountType);
                            if (success) {
                                JOptionPane.showMessageDialog(GUI.this, "Account type changed successfully for " + currentUserName + ".");
                                // Go back to admin menu instead of trying to refresh in place
                                cardLayout.show(mainPanel, "adminMenu");
                            } else {
                                JOptionPane.showMessageDialog(GUI.this, "Failed to change account type.", "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        } else {
                            JOptionPane.showMessageDialog(GUI.this, "No account type selected.", "Warning", JOptionPane.WARNING_MESSAGE);
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(GUI.this, "Please select a user.");
                }
            }
        });

        grantAdminButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = usersTable.getSelectedRow();
                if (selectedRow != -1) {
                    // Get bankNumber from the selected row (column index 1)
                    String bankNumber = (String) usersTable.getValueAt(selectedRow, 1);
                    String currentUserName = (String) usersTable.getValueAt(selectedRow, 0); // For display
                    // Safely check the boolean value from the table model
                    Object isAdminObj = usersTable.getValueAt(selectedRow, 3);
                    boolean isAdmin = Boolean.TRUE.equals(isAdminObj); // Handles null and checks for true

                    if (isAdmin) {
                         JOptionPane.showMessageDialog(GUI.this, currentUserName + " is already an admin.", "Info", JOptionPane.INFORMATION_MESSAGE);
                         return;
                    }

                    int confirm = JOptionPane.showConfirmDialog(GUI.this,
                        "Grant admin privileges to " + currentUserName + "?",
                        "Confirm Admin Grant",
                        JOptionPane.YES_NO_OPTION);

                    if (confirm == JOptionPane.YES_OPTION) {
                        // Use bankNumber to grant admin
                        boolean success = bank.grantAdmin(bankNumber);
                        if (success) {
                            JOptionPane.showMessageDialog(GUI.this, "Admin privilege granted successfully to " + currentUserName + ".");
                             // Go back to admin menu instead of trying to refresh in place
                            cardLayout.show(mainPanel, "adminMenu");
                        } else {
                             JOptionPane.showMessageDialog(GUI.this, "Failed to grant admin privilege.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(GUI.this, "Please select a user from the table.", "Selection Required", JOptionPane.WARNING_MESSAGE);
                }
            }
        });


        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(mainPanel, "adminMenu");
            }
        });

        return manageUsersPanel;
    }

    private void displayUserDetails() {
        if (loggedInUser != null) {
            String details = "User: " + loggedInUser.getUserName() +
                    ", Balance: " + loggedInUser.getBalance() +
                    ", Account Type: " + loggedInUser.getAccountType();
            userDetailsLabel.setText(details);
        } else {
            userDetailsLabel.setText("Not logged in");
        }
    }
}
