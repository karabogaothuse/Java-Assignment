
package com.bankingsystem.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

// Creates the database tables when the application starts.
public class DatabaseInitializer {

    public static void initializeDatabase() throws SQLException {

        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement()) {

            // Stores customer information and login credentials.
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS customers (
                        customer_id TEXT PRIMARY KEY,
                        first_name TEXT NOT NULL,
                        surname TEXT NOT NULL,
                        address TEXT NOT NULL,
                        phone TEXT NOT NULL,
                        employed INTEGER NOT NULL CHECK(employed IN (0, 1)),
                        password_hash TEXT NOT NULL,
                        password_salt TEXT NOT NULL,
                        created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
                    )
                    """);

            // Stores the customer's bank accounts.
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS accounts (
                        account_number TEXT PRIMARY KEY,
                        customer_id TEXT NOT NULL,
                        account_type TEXT NOT NULL CHECK (
                            account_type IN ('SAVINGS', 'INVESTMENT', 'CHEQUE')
                        ),
                        balance REAL NOT NULL DEFAULT 0 CHECK(balance >= 0),
                        opening_deposit REAL NOT NULL CHECK(opening_deposit >= 0),
                        branch TEXT NOT NULL,
                        company_name TEXT,
                        company_address TEXT,
                        account_status TEXT NOT NULL DEFAULT 'PendingApproval'
                            CHECK(account_status IN (
                                'PendingApproval',
                                'Active',
                                'InterestAccrued',
                                'Dormant',
                                'Frozen',
                                'Rejected',
                                'Closed'
                            )),
                        last_interest_month TEXT,
                        created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,

                        FOREIGN KEY (customer_id)
                            REFERENCES customers(customer_id)
                            ON DELETE CASCADE,

                        CHECK (
                            account_type != 'INVESTMENT'
                            OR opening_deposit >= 500
                        ),

                        CHECK (
                            account_type != 'CHEQUE'
                            OR (
                                company_name IS NOT NULL
                                AND company_name != ''
                                AND company_address IS NOT NULL
                                AND company_address != ''
                            )
                        )
                    )
                    """);

            // Stores account deposits, withdrawals and interest payments.
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS transactions (
                        transaction_id INTEGER PRIMARY KEY AUTOINCREMENT,
                        account_number TEXT NOT NULL,
                        transaction_type TEXT NOT NULL,
                        amount REAL NOT NULL CHECK(amount > 0),
                        description TEXT NOT NULL,
                        transaction_date TEXT NOT NULL,

                        FOREIGN KEY (account_number)
                            REFERENCES accounts(account_number)
                            ON DELETE CASCADE
                    )
                    """);

            // Prevents an unemployed customer from opening a Cheque Account.
            statement.execute("""
                    CREATE TRIGGER IF NOT EXISTS check_cheque_customer
                    BEFORE INSERT ON accounts
                    WHEN NEW.account_type = 'CHEQUE'
                    AND (
                        SELECT employed
                        FROM customers
                        WHERE customer_id = NEW.customer_id
                    ) != 1
                    BEGIN
                        SELECT RAISE(
                            ABORT,
                            'Only employed customers can open Cheque Accounts.'
                        );
                    END
                    """);

            // Makes transaction history searches faster.
            statement.execute("""
                    CREATE INDEX IF NOT EXISTS idx_transactions_account
                    ON transactions(account_number)
                    """);
        }
    }
}
