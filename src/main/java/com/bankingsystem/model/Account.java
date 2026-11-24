
package com.bankingsystem.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Abstraction: Base class for all account types.
public abstract class Account implements InterestBearing {

    private final String accountNumber;
    private double balance;
    private double openingDeposit;
    private final String branch;
    private final Customer customer;
    private final List<Transaction> transactions;

    private YearMonth lastInterestMonth;

    public Account(String accountNumber, double initialBalance,
                   String branch, Customer customer) {

        if (accountNumber == null || accountNumber.isBlank()) {
            throw new IllegalArgumentException("Account number is required.");
        }

        if (branch == null || branch.isBlank()) {
            throw new IllegalArgumentException("Branch is required.");
        }

        if (customer == null) {
            throw new IllegalArgumentException(
                    "An account must belong to a customer."
            );
        }

        if (!Double.isFinite(initialBalance) || initialBalance < 0) {
            throw new IllegalArgumentException(
                    "Initial balance cannot be negative or invalid."
            );
        }

        this.accountNumber = accountNumber.trim();
        this.balance = roundMoney(initialBalance);
        this.openingDeposit = this.balance;
        this.branch = branch.trim();
        this.customer = customer;
        this.transactions = new ArrayList<>();

        if (this.balance > 0) {
            transactions.add(new Transaction(
                    "OPENING DEPOSIT",
                    this.balance,
                    "Initial deposit"
            ));
        }
    }

    // Method Overloading: Deposits can be made with or without a description.
    public void deposit(double amount) {
        deposit(amount, "Deposit");
    }

    public void deposit(double amount, String description) {

        double validAmount = validatePositiveAmount(amount);

        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException(
                    "Deposit description is required."
            );
        }

        double newBalance = balance + validAmount;

        if (!Double.isFinite(newBalance)) {
            throw new IllegalArgumentException(
                    "The resulting balance is too large."
            );
        }

        balance = roundMoney(newBalance);

        transactions.add(new Transaction(
                "DEPOSIT", validAmount, description.trim()
        ));
    }

    protected final void withdrawFunds(double amount, String description) {

        double validAmount = validatePositiveAmount(amount);

        if (validAmount > balance) {
            throw new IllegalArgumentException("Insufficient funds.");
        }

        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException(
                    "Withdrawal description is required."
            );
        }

        balance = roundMoney(balance - validAmount);

        transactions.add(new Transaction(
                "WITHDRAWAL", validAmount, description.trim()
        ));
    }

    // Prevents interest from being applied twice in the same month.
    protected final double addMonthlyInterest(double rate) {

        YearMonth currentMonth = YearMonth.now();

        if (currentMonth.equals(lastInterestMonth)) {
            return 0.0;
        }

        if (!Double.isFinite(rate) || rate < 0) {
            throw new IllegalArgumentException("Interest rate is invalid.");
        }

        double rawInterest = balance * rate;

        if (!Double.isFinite(rawInterest)) {
            throw new IllegalArgumentException(
                    "Calculated interest is too large."
            );
        }

        double interest = roundMoney(rawInterest);
        double newBalance = balance + interest;

        if (!Double.isFinite(newBalance)) {
            throw new IllegalArgumentException(
                    "The resulting balance is too large."
            );
        }

        if (interest > 0) {

            balance = roundMoney(newBalance);

            transactions.add(new Transaction(
                    "INTEREST", interest, "Monthly interest"
            ));
        }

        lastInterestMonth = currentMonth;

        return interest;
    }

    private static double validatePositiveAmount(double amount) {

        if (!Double.isFinite(amount) || amount <= 0) {
            throw new IllegalArgumentException(
                    "Amount must be greater than zero."
            );
        }

        double roundedAmount = roundMoney(amount);

        if (roundedAmount <= 0) {
            throw new IllegalArgumentException(
                    "Amount must be at least BWP0.01."
            );
        }

        return roundedAmount;
    }

    private static double roundMoney(double amount) {

        return BigDecimal.valueOf(amount)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    // Used by the database layer when restoring an account.
    public final void restorePersistedData(
            double savedBalance,
            double savedOpeningDeposit,
            YearMonth savedInterestMonth,
            List<Transaction> savedTransactions) {

        if (!Double.isFinite(savedBalance) || savedBalance < 0
                || !Double.isFinite(savedOpeningDeposit)
                || savedOpeningDeposit < 0) {

            throw new IllegalArgumentException(
                    "Saved account amounts are invalid."
            );
        }

        if (savedInterestMonth != null
                && savedInterestMonth.isAfter(YearMonth.now())) {

            throw new IllegalArgumentException(
                    "Interest month cannot be in the future."
            );
        }

        if (savedTransactions == null) {
            throw new IllegalArgumentException(
                    "Transaction history cannot be null."
            );
        }

        this.balance = roundMoney(savedBalance);
        this.openingDeposit = roundMoney(savedOpeningDeposit);
        this.lastInterestMonth = savedInterestMonth;

        transactions.clear();
        transactions.addAll(savedTransactions);
    }

    public abstract void withdraw(double amount);

    @Override
    public abstract double calculateMonthlyInterest();

    public String getAccountNumber() {
        return accountNumber;
    }

    public double getBalance() {
        return balance;
    }

    public double getOpeningDeposit() {
        return openingDeposit;
    }

    public String getBranch() {
        return branch;
    }

    public Customer getCustomer() {
        return customer;
    }

    public YearMonth getLastInterestMonth() {
        return lastInterestMonth;
    }

    public List<Transaction> getTransactions() {
        return Collections.unmodifiableList(transactions);
    }
}
