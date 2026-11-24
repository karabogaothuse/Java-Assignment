
package com.bankingsystem.model;

// Inheritance: ChequeAccount inherits from Account.
public class ChequeAccount extends Account {

    // Encapsulation: Workplace details are stored privately.
    private final String companyName;
    private final String companyAddress;

    ChequeAccount(String accountNumber, double initialBalance,
                  String branch, Customer customer,
                  String companyName, String companyAddress) {

        super(accountNumber, initialBalance, branch,
                validateEligibility(customer, companyName, companyAddress));

        this.companyName = companyName.trim();
        this.companyAddress = companyAddress.trim();
    }

    private static Customer validateEligibility(Customer customer,
                                                String companyName,
                                                String companyAddress) {

        if (customer == null || !customer.isEmployed()) {
            throw new IllegalArgumentException(
                    "Only employed customers can open Cheque Accounts."
            );
        }

        if (companyName == null || companyName.isBlank()
                || companyAddress == null || companyAddress.isBlank()) {

            throw new IllegalArgumentException(
                    "Company name and company address are required."
            );
        }

        return customer;
    }

    // Method Overriding: Cheque Accounts allow withdrawals.
    @Override
    public void withdraw(double amount) {
        withdrawFunds(amount, "Cheque withdrawal");
    }

    @Override
    public double calculateMonthlyInterest() {
        return 0.0; // No interest rate specified in the assignment.
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getCompanyAddress() {
        return companyAddress;
    }
}
