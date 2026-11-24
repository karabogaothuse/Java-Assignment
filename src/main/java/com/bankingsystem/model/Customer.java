
package com.bankingsystem.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Encapsulation: Customer information is kept private.
public class Customer {

    private final String customerId;
    private final String firstName;
    private final String surname;
    private final String address;
    private final String phone;
    private final boolean employed;

    // Composition: A customer maintains their own accounts.
    private final List<Account> accounts;

    // Method Overloading: Allows customers to be created with or without employment details.
    public Customer(String customerId, String firstName, String surname,
                    String address, String phone) {

        this(customerId, firstName, surname, address, phone, false);
    }

    public Customer(String customerId, String firstName, String surname,
                    String address, String phone, boolean employed) {

        if (customerId == null || customerId.isBlank()
                || firstName == null || firstName.isBlank()
                || surname == null || surname.isBlank()
                || address == null || address.isBlank()
                || phone == null || phone.isBlank()) {

            throw new IllegalArgumentException(
                    "All customer details are required."
            );
        }

        this.customerId = customerId.trim();
        this.firstName = firstName.trim();
        this.surname = surname.trim();
        this.address = address.trim();
        this.phone = phone.trim();
        this.employed = employed;

        this.accounts = new ArrayList<>();
    }

    void addAccount(Account account) {

        if (account == null) {
            throw new IllegalArgumentException("Account cannot be null.");
        }

        if (account.getCustomer() != this) {
            throw new IllegalArgumentException(
                    "This account belongs to another customer."
            );
        }

        for (Account existingAccount : accounts) {

            if (existingAccount.getAccountNumber()
                    .equals(account.getAccountNumber())) {

                throw new IllegalArgumentException(
                        "This customer already has this account number."
                );
            }
        }

        accounts.add(account);
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getSurname() {
        return surname;
    }

    public String getAddress() {
        return address;
    }

    public String getPhone() {
        return phone;
    }

    public boolean isEmployed() {
        return employed;
    }

    public List<Account> getAccounts() {
        return Collections.unmodifiableList(accounts);
    }
}
