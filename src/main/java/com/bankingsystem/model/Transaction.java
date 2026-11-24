
package com.bankingsystem.model;

import java.time.LocalDateTime;

// Encapsulation: Transaction details cannot be changed after creation.
public final class Transaction {

    private final String transactionType;
    private final double amount;
    private final String description;
    private final LocalDateTime transactionDate;

    public Transaction(String transactionType, double amount,
                       String description) {

        this(transactionType, amount, description, LocalDateTime.now());
    }

    // Constructor Overloading: Allows an existing transaction date to be restored.
    public Transaction(String transactionType, double amount,
                       String description, LocalDateTime transactionDate) {

        if (transactionType == null || transactionType.isBlank()) {
            throw new IllegalArgumentException(
                    "Transaction type is required."
            );
        }

        if (!Double.isFinite(amount) || amount <= 0) {
            throw new IllegalArgumentException(
                    "Transaction amount must be greater than zero."
            );
        }

        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException(
                    "Transaction description is required."
            );
        }

        if (transactionDate == null) {
            throw new IllegalArgumentException(
                    "Transaction date is required."
            );
        }

        this.transactionType = transactionType;
        this.amount = amount;
        this.description = description;
        this.transactionDate = transactionDate;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public double getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }
}
