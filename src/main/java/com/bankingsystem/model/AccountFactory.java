
package com.bankingsystem.model;

// Creates accounts and links them to customers after construction is complete.
public final class AccountFactory {

    private AccountFactory() {
    }

    public static SavingAccount openSavingsAccount(
            String accountNumber, double initialBalance,
            String branch, Customer customer) {

        SavingAccount account = new SavingAccount(
                accountNumber, initialBalance, branch, customer
        );

        customer.addAccount(account);

        return account;
    }

    public static InvestementAccount openInvestmentAccount(
            String accountNumber, double initialBalance,
            String branch, Customer customer) {

        InvestementAccount account = new InvestementAccount(
                accountNumber, initialBalance, branch, customer
        );

        customer.addAccount(account);

        return account;
    }

    public static ChequeAccount openChequeAccount(
            String accountNumber, double initialBalance,
            String branch, Customer customer,
            String companyName, String companyAddress) {

        ChequeAccount account = new ChequeAccount(
                accountNumber, initialBalance, branch,
                customer, companyName, companyAddress
        );

        customer.addAccount(account);

        return account;
    }
}
