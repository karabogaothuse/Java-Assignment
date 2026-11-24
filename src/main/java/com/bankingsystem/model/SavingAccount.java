
package com.bankingsystem.model;

// Inheritance: SavingAccount inherits from Account.
public class SavingAccount extends Account {

    SavingAccount(String accountNumber, double initialBalance,
                  String branch, Customer customer) {

        super(accountNumber, initialBalance, branch, customer);
    }

    // Method Overriding: Savings Accounts do not allow withdrawals.
    @Override
    public void withdraw(double amount) {
        throw new UnsupportedOperationException(
                "Withdrawals are not allowed on Savings Accounts."
        );
    }

    @Override
    public double calculateMonthlyInterest() {
        return addMonthlyInterest(0.0005); // 0.05% monthly interest
    }
}
