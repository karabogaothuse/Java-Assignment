
package com.bankingsystem.database;

import com.bankingsystem.model.Transaction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// DAO: Handles transaction database operations.
public class TransactionDAO {

    public void create(String accountNumber,
                       Transaction transaction) throws SQLException {

        if (accountNumber == null || accountNumber.isBlank()
                || transaction == null) {

            throw new IllegalArgumentException(
                    "Account number and transaction are required."
            );
        }

        String sql = """
                INSERT INTO transactions (
                    account_number, transaction_type,
                    amount, description, transaction_date
                )
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, accountNumber);
            statement.setString(2, transaction.getTransactionType());
            statement.setDouble(3, transaction.getAmount());
            statement.setString(4, transaction.getDescription());
            statement.setString(5, transaction.getTransactionDate().toString());

            statement.executeUpdate();
        }
    }

    public List<Transaction> findByAccountNumber(String accountNumber)
            throws SQLException {

        List<Transaction> transactions = new ArrayList<>();

        String sql = """
                SELECT * FROM transactions
                WHERE account_number = ?
                ORDER BY transaction_date, transaction_id
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, accountNumber);

            try (ResultSet result = statement.executeQuery()) {

                while (result.next()) {

                    LocalDateTime transactionDate =
                            LocalDateTime.parse(
                                    result.getString("transaction_date")
                            );

                    transactions.add(new Transaction(
                            result.getString("transaction_type"),
                            result.getDouble("amount"),
                            result.getString("description"),
                            transactionDate
                    ));
                }
            }
        }

        return transactions;
    }

    public boolean updateDescription(long transactionId,
                                     String description) throws SQLException {

        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException(
                    "Transaction description is required."
            );
        }

        String sql = """
                UPDATE transactions
                SET description = ?
                WHERE transaction_id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, description.trim());
            statement.setLong(2, transactionId);

            return statement.executeUpdate() == 1;
        }
    }

    public boolean delete(long transactionId) throws SQLException {

        String sql = """
                DELETE FROM transactions
                WHERE transaction_id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, transactionId);

            return statement.executeUpdate() == 1;
        }
    }
}
