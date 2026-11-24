
package com.bankingsystem.database;

import com.bankingsystem.model.Customer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// DAO: Handles customer database operations.
public class CustomerDAO {

    public void create(Customer customer,
                       String passwordHash,
                       String passwordSalt) throws SQLException {

        if (customer == null || passwordHash == null
                || passwordHash.isBlank()
                || passwordSalt == null || passwordSalt.isBlank()) {

            throw new IllegalArgumentException(
                    "Customer and password details are required."
            );
        }

        String sql = """
                INSERT INTO customers (
                    customer_id, first_name, surname, address,
                    phone, employed, password_hash, password_salt
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, customer.getCustomerId());
            statement.setString(2, customer.getFirstName());
            statement.setString(3, customer.getSurname());
            statement.setString(4, customer.getAddress());
            statement.setString(5, customer.getPhone());
            statement.setInt(6, customer.isEmployed() ? 1 : 0);
            statement.setString(7, passwordHash);
            statement.setString(8, passwordSalt);

            statement.executeUpdate();
        }
    }

    public Customer findById(String customerId) throws SQLException {

        String sql = """
                SELECT * FROM customers
                WHERE customer_id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, customerId);

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {
                    return mapCustomer(result);
                }
            }
        }

        return null;
    }

    public LoginDetails findLoginDetails(String customerId)
            throws SQLException {

        String sql = """
                SELECT * FROM customers
                WHERE LOWER(customer_id) = LOWER(?)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, customerId);

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {

                    Customer customer = mapCustomer(result);

                    return new LoginDetails(
                            customer,
                            result.getString("password_hash"),
                            result.getString("password_salt")
                    );
                }
            }
        }

        return null;
    }

    public List<Customer> findAll() throws SQLException {

        List<Customer> customers = new ArrayList<>();

        String sql = "SELECT * FROM customers ORDER BY first_name, surname";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                customers.add(mapCustomer(result));
            }
        }

        return customers;
    }

    public boolean updateContactDetails(String customerId,
                                        String address,
                                        String phone) throws SQLException {

        if (address == null || address.isBlank()
                || phone == null || phone.isBlank()) {

            throw new IllegalArgumentException(
                    "Address and phone number are required."
            );
        }

        String sql = """
                UPDATE customers
                SET address = ?, phone = ?
                WHERE customer_id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, address.trim());
            statement.setString(2, phone.trim());
            statement.setString(3, customerId);

            return statement.executeUpdate() == 1;
        }
    }

    public boolean delete(String customerId) throws SQLException {

        String sql = "DELETE FROM customers WHERE customer_id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, customerId);

            return statement.executeUpdate() == 1;
        }
    }

    private Customer mapCustomer(ResultSet result) throws SQLException {

        return new Customer(
                result.getString("customer_id"),
                result.getString("first_name"),
                result.getString("surname"),
                result.getString("address"),
                result.getString("phone"),
                result.getInt("employed") == 1
        );
    }

    public record LoginDetails(
            Customer customer,
            String passwordHash,
            String passwordSalt) {
    }
}
