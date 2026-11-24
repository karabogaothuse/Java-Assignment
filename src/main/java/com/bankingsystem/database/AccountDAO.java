
package com.bankingsystem.database;

import com.bankingsystem.model.Account;
import com.bankingsystem.model.AccountFactory;
import com.bankingsystem.model.ChequeAccount;
import com.bankingsystem.model.Customer;
import com.bankingsystem.model.InvestementAccount;
import com.bankingsystem.model.SavingAccount;
import com.bankingsystem.model.Transaction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

// DAO: Handles account database operations.
public class AccountDAO {

    private final TransactionDAO transactionDAO;

    public AccountDAO() {
        this(new TransactionDAO());
    }

    public AccountDAO(TransactionDAO transactionDAO) {
        this.transactionDAO = transactionDAO;
    }

    public void create(Account account) throws SQLException {

        if (account == null) {
            throw new IllegalArgumentException("Account is required.");
        }

        String sql = """
                INSERT INTO accounts (
                    account_number, customer_id, account_type,
                    balance, opening_deposit, branch,
                    company_name, company_address, last_interest_month
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, account.getAccountNumber());
            statement.setString(2, account.getCustomer().getCustomerId());
            statement.setString(3, getAccountType(account));
            statement.setDouble(4, account.getBalance());
            statement.setDouble(5, account.getOpeningDeposit());
            statement.setString(6, account.getBranch());

            if (account instanceof ChequeAccount chequeAccount) {
                statement.setString(7, chequeAccount.getCompanyName());
                statement.setString(8, chequeAccount.getCompanyAddress());
            } else {
                statement.setNull(7, java.sql.Types.VARCHAR);
                statement.setNull(8, java.sql.Types.VARCHAR);
            }

            if (account.getLastInterestMonth() == null) {
                statement.setNull(9, java.sql.Types.VARCHAR);
            } else {
                statement.setString(9, account.getLastInterestMonth().toString());
            }

            statement.executeUpdate();
        }

        // Save the opening transaction if one was created.
        for (Transaction transaction : account.getTransactions()) {
            transactionDAO.create(account.getAccountNumber(), transaction);
        }
    }

    public Account findByAccountNumber(String accountNumber,
                                       Customer customer) throws SQLException {

        if (customer == null) {
            throw new IllegalArgumentException("Customer is required.");
        }

        String sql = """
                SELECT * FROM accounts
                WHERE account_number = ? AND customer_id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, accountNumber);
            statement.setString(2, customer.getCustomerId());

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {
                    return mapAccount(result, customer);
                }
            }
        }

        return null;
    }

    public List<Account> findByCustomer(Customer customer) throws SQLException {

        if (customer == null) {
            throw new IllegalArgumentException("Customer is required.");
        }

        List<Account> accounts = new ArrayList<>();

        String sql = """
                SELECT * FROM accounts
                WHERE customer_id = ?
                ORDER BY created_at
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, customer.getCustomerId());

            try (ResultSet result = statement.executeQuery()) {

                while (result.next()) {

                    String accountNumber = result.getString("account_number");

                    Account existing = findLoadedAccount(
                            customer, accountNumber
                    );

                    if (existing != null) {
                        accounts.add(existing);
                    } else {
                        accounts.add(mapAccount(result, customer));
                    }
                }
            }
        }

        return accounts;
    }

    public boolean update(Account account) throws SQLException {

        if (account == null) {
            throw new IllegalArgumentException("Account is required.");
        }

        String sql = """
                UPDATE accounts
                SET balance = ?, last_interest_month = ?
                WHERE account_number = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setDouble(1, account.getBalance());

            if (account.getLastInterestMonth() == null) {
                statement.setNull(2, java.sql.Types.VARCHAR);
            } else {
                statement.setString(2, account.getLastInterestMonth().toString());
            }

            statement.setString(3, account.getAccountNumber());

            return statement.executeUpdate() == 1;
        }
    }

    public boolean delete(String accountNumber) throws SQLException {

        String sql = """
                DELETE FROM accounts
                WHERE account_number = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, accountNumber);

            return statement.executeUpdate() == 1;
        }
    }

    private Account mapAccount(ResultSet result,
                               Customer customer) throws SQLException {

        String accountNumber = result.getString("account_number");

        if (!customer.getCustomerId().equals(result.getString("customer_id"))) {
            throw new SQLException("Account belongs to a different customer.");
        }

        String monthText = result.getString("last_interest_month");

        YearMonth lastInterestMonth = monthText == null
                ? null : YearMonth.parse(monthText);

        List<Transaction> transactions =
                transactionDAO.findByAccountNumber(accountNumber);

        return AccountFactory.restoreAccount(
                result.getString("account_type"),
                accountNumber,
                result.getDouble("balance"),
                result.getDouble("opening_deposit"),
                result.getString("branch"),
                customer,
                result.getString("company_name"),
                result.getString("company_address"),
                lastInterestMonth,
                transactions
        );
    }

    private Account findLoadedAccount(Customer customer,
                                      String accountNumber) {

        for (Account account : customer.getAccounts()) {

            if (account.getAccountNumber().equals(accountNumber)) {
                return account;
            }
        }

        return null;
    }

    private String getAccountType(Account account) {

        if (account instanceof SavingAccount) {
            return "SAVINGS";
        }

        if (account instanceof InvestementAccount) {
            return "INVESTMENT";
        }

        if (account instanceof ChequeAccount) {
            return "CHEQUE";
        }

        throw new IllegalArgumentException("Unknown account type.");
    }
}
