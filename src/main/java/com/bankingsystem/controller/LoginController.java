
package com.bankingsystem.controller;

import com.bankingsystem.model.Customer;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

// Controller: Handles customer registration and login.
public class LoginController {

    private final Map<String, Customer> customers = new HashMap<>();
    private final Map<String, String> passwordHashes = new HashMap<>();
    private final Map<String, String> passwordSalts = new HashMap<>();

    private static final int ITERATIONS = 120000;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_LENGTH = 16;

    private final SecureRandom random = new SecureRandom();

    public Customer registerCustomer(
            String customerId,
            String firstName,
            String surname,
            String address,
            String phone,
            boolean employed,
            String password) {

        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException(
                    "Password must contain at least 8 characters."
            );
        }

        String key = normalizeCustomerId(customerId);

        if (customers.containsKey(key)) {
            throw new IllegalArgumentException(
                    "A customer with this ID already exists."
            );
        }

        Customer customer = new Customer(
                customerId, firstName, surname,
                address, phone, employed
        );

        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);

        String saltText = Base64.getEncoder().encodeToString(salt);
        String passwordHash = Base64.getEncoder().encodeToString(
                hashPassword(password, salt)
        );

        customers.put(key, customer);
        passwordSalts.put(key, saltText);
        passwordHashes.put(key, passwordHash);

        return customer;
    }

    public Customer login(String customerId, String password) {

        if (customerId == null || customerId.isBlank()
                || password == null || password.isBlank()) {
            return null;
        }

        String key = normalizeCustomerId(customerId);

        Customer customer = customers.get(key);

        if (customer == null) {
            return null;
        }

        byte[] salt = Base64.getDecoder().decode(passwordSalts.get(key));

        byte[] storedHash = Base64.getDecoder().decode(
                passwordHashes.get(key)
        );

        byte[] enteredHash = hashPassword(password, salt);

        if (MessageDigest.isEqual(storedHash, enteredHash)) {
            return customer;
        }

        return null;
    }

    private byte[] hashPassword(String password, byte[] salt) {

        char[] passwordCharacters = password.toCharArray();

        PBEKeySpec specification = new PBEKeySpec(
                passwordCharacters, salt, ITERATIONS, KEY_LENGTH
        );

        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance(
                    "PBKDF2WithHmacSHA256"
            );

            return factory.generateSecret(specification).getEncoded();

        } catch (GeneralSecurityException e) {

            throw new IllegalStateException(
                    "Could not process the password.", e
            );

        } finally {
            specification.clearPassword();
            Arrays.fill(passwordCharacters, '\0');
        }
    }

    private String normalizeCustomerId(String customerId) {

        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException("Customer ID is required.");
        }

        return customerId.trim().toLowerCase(Locale.ROOT);
    }
}
