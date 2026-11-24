
package com.bankingsystem.model;

// Inheritance: InvestementAccount inherits from Account.
public class InvestementAccount extends Account {

    InvestementAccount(String accountNumber, double initialBalance,
                       String branch, Customer customer) {

        super(accountNumber, validateInitialDeposit(initialBalance), branch, customer);
    }

    private static double validateInitialDeposit(double amount) {

        if (!Double.isFinite(amount) || amount < 500.00) {
            throw new IllegalArgumentException(
                    "Investment Account requires an initial deposit of at least BWP500.00."
            );
        }

        return amount;
    }

    // Method Overriding: Allows withdrawals when sufficient funds are available.
    @Override
    public void withdraw(double amount) {
        withdrawFunds(amount, "Investment withdrawal");
    }

    @Override
    public double calculateMonthlyInterest() {
        return addMonthlyInterest(0.05);
    }
}
