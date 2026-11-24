
package com.bankingsystem.boundary;

import com.bankingsystem.controller.AccountController;
import com.bankingsystem.model.Account;
import com.bankingsystem.model.ChequeAccount;
import com.bankingsystem.model.Customer;
import com.bankingsystem.model.InvestementAccount;
import com.bankingsystem.model.SavingAccount;
import com.bankingsystem.model.Transaction;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// Boundary: Displays account information and collects customer input.
public class AccountView extends BorderPane {

    private final AccountController accountController;
    private final Customer customer;
    private final Runnable onLogout;

    private final ComboBox<String> accountTypeBox = new ComboBox<>();

    private final TextField accountNumberField = new TextField();
    private final TextField branchField = new TextField();
    private final TextField initialDepositField = new TextField("0.00");

    private final Label companyNameLabel = new Label("Company Name:");
    private final Label companyAddressLabel = new Label("Company Address:");

    private final TextField companyNameField = new TextField();
    private final TextField companyAddressField = new TextField();

    private final ComboBox<Account> accountSelector = new ComboBox<>();
    private final TextField amountField = new TextField();

    private final Label balanceLabel = new Label("No account selected.");
    private final Label messageLabel = new Label();

    private final ListView<String> transactionList = new ListView<>();

    public AccountView(AccountController accountController,
                       Customer customer, Runnable onLogout) {

        this.accountController = Objects.requireNonNull(accountController);
        this.customer = Objects.requireNonNull(customer);
        this.onLogout = Objects.requireNonNull(onLogout);

        setPadding(new Insets(15));

        createHeader();

        ScrollPane scrollPane = new ScrollPane();

        VBox content = new VBox(15);
        content.setPadding(new Insets(15));

        content.getChildren().addAll(
                createAccountOpeningSection(),
                createAccountManagementSection()
        );

        scrollPane.setContent(content);
        scrollPane.setFitToWidth(true);

        setCenter(scrollPane);

        // Load the customer's existing accounts.
        accountSelector.getItems().setAll(customer.getAccounts());
    }

    private void createHeader() {

        Label title = new Label("Banking System");

        Label welcome = new Label(
                "Welcome, " + customer.getFirstName()
                        + " " + customer.getSurname()
        );

        Button logoutButton = new Button("Log Out");
        logoutButton.setOnAction(event -> onLogout.run());

        HBox header = new HBox(15, title, welcome, logoutButton);
        header.setPadding(new Insets(10));

        HBox.setHgrow(welcome, Priority.ALWAYS);

        setTop(header);
    }

    private TitledPane createAccountOpeningSection() {

        accountTypeBox.getItems().addAll(
                "Savings Account",
                "Investment Account",
                "Cheque Account"
        );

        accountTypeBox.setValue("Savings Account");
        accountTypeBox.setMaxWidth(Double.MAX_VALUE);

        accountTypeBox.setOnAction(event -> updateCompanyFields());

        accountNumberField.setPromptText("Account Number");
        branchField.setPromptText("Branch");
        initialDepositField.setPromptText("Initial Deposit");

        companyNameField.setPromptText("Company Name");
        companyAddressField.setPromptText("Company Address");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(15));

        grid.add(new Label("Account Type:"), 0, 0);
        grid.add(accountTypeBox, 1, 0);

        grid.add(new Label("Account Number:"), 0, 1);
        grid.add(accountNumberField, 1, 1);

        grid.add(new Label("Branch:"), 0, 2);
        grid.add(branchField, 1, 2);

        grid.add(new Label("Initial Deposit (BWP):"), 0, 3);
        grid.add(initialDepositField, 1, 3);

        grid.add(companyNameLabel, 0, 4);
        grid.add(companyNameField, 1, 4);

        grid.add(companyAddressLabel, 0, 5);
        grid.add(companyAddressField, 1, 5);

        Button openButton = new Button("Open Account");
        openButton.setOnAction(event -> openAccount());

        grid.add(openButton, 1, 6);

        updateCompanyFields();

        TitledPane pane = new TitledPane("Open a Bank Account", grid);
        pane.setCollapsible(false);

        return pane;
    }

    private TitledPane createAccountManagementSection() {

        accountSelector.setPromptText("Select an Account");
        accountSelector.setMaxWidth(Double.MAX_VALUE);

        accountSelector.setConverter(new StringConverter<Account>() {

            @Override
            public String toString(Account account) {

                if (account == null) {
                    return "";
                }

                String type;

                if (account instanceof SavingAccount) {
                    type = "Savings Account";
                } else if (account instanceof InvestementAccount) {
                    type = "Investment Account";
                } else if (account instanceof ChequeAccount) {
                    type = "Cheque Account";
                } else {
                    type = "Account";
                }

                return account.getAccountNumber() + " - " + type;
            }

            @Override
            public Account fromString(String value) {
                return null;
            }
        });

        accountSelector.valueProperty().addListener(
                (observable, oldAccount, newAccount) ->
                        refreshAccountDetails(newAccount)
        );

        amountField.setPromptText("Amount (BWP)");

        Button depositButton = new Button("Deposit");
        depositButton.setOnAction(event -> depositMoney());

        Button withdrawButton = new Button("Withdraw");
        withdrawButton.setOnAction(event -> withdrawMoney());

        HBox buttons = new HBox(10, depositButton, withdrawButton);

        transactionList.setPlaceholder(
                new Label("No transactions to display.")
        );

        VBox layout = new VBox(
                10,
                new Label("Select Account:"),
                accountSelector,
                balanceLabel,
                amountField,
                buttons,
                new Label("Transaction History:"),
                transactionList,
                messageLabel
        );

        layout.setPadding(new Insets(15));

        TitledPane pane = new TitledPane("Manage Your Account", layout);
        pane.setCollapsible(false);

        return pane;
    }

    private void updateCompanyFields() {

        boolean isChequeAccount =
                "Cheque Account".equals(accountTypeBox.getValue());

        companyNameLabel.setVisible(isChequeAccount);
        companyNameLabel.setManaged(isChequeAccount);

        companyNameField.setVisible(isChequeAccount);
        companyNameField.setManaged(isChequeAccount);

        companyAddressLabel.setVisible(isChequeAccount);
        companyAddressLabel.setManaged(isChequeAccount);

        companyAddressField.setVisible(isChequeAccount);
        companyAddressField.setManaged(isChequeAccount);
    }

    private void openAccount() {

        try {

            String accountNumber = accountNumberField.getText();
            String branch = branchField.getText();

            double initialDeposit =
                    readAmount(initialDepositField.getText());

            Account newAccount;

            switch (accountTypeBox.getValue()) {

                case "Savings Account" ->
                        newAccount = accountController.openSavingsAccount(
                                accountNumber,
                                initialDeposit,
                                branch,
                                customer
                        );

                case "Investment Account" ->
                        newAccount = accountController.openInvestmentAccount(
                                accountNumber,
                                initialDeposit,
                                branch,
                                customer
                        );

                case "Cheque Account" ->
                        newAccount = accountController.openChequeAccount(
                                accountNumber,
                                initialDeposit,
                                branch,
                                customer,
                                companyNameField.getText(),
                                companyAddressField.getText()
                        );

                default -> throw new IllegalArgumentException(
                        "Please select an account type."
                );
            }

            accountSelector.getItems().setAll(customer.getAccounts());
            accountSelector.setValue(newAccount);

            messageLabel.setText("Account opened successfully.");

            accountNumberField.clear();
            branchField.clear();
            initialDepositField.setText("0.00");
            companyNameField.clear();
            companyAddressField.clear();

        } catch (RuntimeException e) {
            messageLabel.setText(e.getMessage());
        }
    }

    private void depositMoney() {

        try {

            Account account = getSelectedAccount();
            double amount = readAmount(amountField.getText());

            accountController.deposit(account, amount);

            messageLabel.setText("Deposit successful.");

            amountField.clear();
            refreshAccountDetails(account);

        } catch (RuntimeException e) {
            messageLabel.setText(e.getMessage());
        }
    }

    private void withdrawMoney() {

        try {

            Account account = getSelectedAccount();
            double amount = readAmount(amountField.getText());

            accountController.withdraw(account, amount);

            messageLabel.setText("Withdrawal successful.");

            amountField.clear();
            refreshAccountDetails(account);

        } catch (RuntimeException e) {
            messageLabel.setText(e.getMessage());
        }
    }

    private Account getSelectedAccount() {

        Account account = accountSelector.getValue();

        if (account == null) {
            throw new IllegalArgumentException(
                    "Please select an account first."
            );
        }

        return account;
    }

    private double readAmount(String value) {

        try {

            double amount = Double.parseDouble(value.trim());

            if (!Double.isFinite(amount)) {
                throw new IllegalArgumentException(
                        "Enter a valid amount."
                );
            }

            return amount;

        } catch (NumberFormatException e) {

            throw new IllegalArgumentException(
                    "Please enter a valid numerical amount."
            );
        }
    }

    private void refreshAccountDetails(Account account) {

        if (account == null) {

            balanceLabel.setText("No account selected.");
            transactionList.getItems().clear();

            return;
        }

        balanceLabel.setText(
                String.format(
                        "Current Balance: BWP %.2f",
                        account.getBalance()
                )
        );

        List<String> history = new ArrayList<>();

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

        for (Transaction transaction :
                accountController.getTransactionHistory(account)) {

            history.add(
                    transaction.getTransactionDate().format(formatter)
                            + " | " + transaction.getTransactionType()
                            + " | BWP "
                            + String.format("%.2f", transaction.getAmount())
                            + " | " + transaction.getDescription()
            );
        }

        transactionList.getItems().setAll(history);
    }
}
