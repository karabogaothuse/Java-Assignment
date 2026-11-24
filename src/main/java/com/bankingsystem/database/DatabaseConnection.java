
package com.bankingsystem.database;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

// Handles the connection to the SQLite database.
public final class DatabaseConnection {

    private static final String DATABASE_FOLDER = "data";
    private static final String DATABASE_URL =
            "jdbc:sqlite:data/banking.db";

    private DatabaseConnection() {
    }

    public static Connection getConnection() throws SQLException {

        try {
            Files.createDirectories(Path.of(DATABASE_FOLDER));
        } catch (IOException e) {
            throw new SQLException(
                    "Could not create the database folder.", e
            );
        }

        Connection connection = DriverManager.getConnection(DATABASE_URL);

        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
            statement.execute("PRAGMA busy_timeout = 5000");
        }

        return connection;
    }
}
