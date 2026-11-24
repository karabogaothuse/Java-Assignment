
package com.bankingsystem.model;

import java.time.YearMonth;
import java.util.List;
import java.util.Locale;

// Creates new accounts and restores accounts from the database.
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

    // Rebuilds an existing account and its saved information.
    public static Account restoreAccount(
            String accountType,
            String accountNumber,
            double currentBalance,
            double openingDeposit,
            String branch,
            Customer customer,
            String companyName,
            String companyAddress,
            YearMonth lastInterestMonth,
            List<Transaction> transactions) {

        if (accountType == null) {
            throw new IllegalArgumentException("Account type is required.");
        }

        // Use the original opening deposit when rebuilding Investment Accounts.
        Account account;

        switch (accountType.toUpperCase(Locale.ROOT)) {

            case "SAVINGS" ->
                    account = openSavingsAccount(
                            accountNumber, openingDeposit, branch, customer
                    );

            case "INVESTMENT" ->
                    account = openInvestmentAccount(
                            accountNumber, openingDeposit, branch, customer
                    );

            case "CHEQUE" ->
                    account = openChequeAccount(
                            accountNumber, openingDeposit, branch,
                            customer, companyName, companyAddress
                    );

            default -> throw new IllegalArgumentException(
                    "Unknown account type: " + accountType
            );
        }

        account.restorePersistedData(
                currentBalance,
                openingDeposit,
                lastInterestMonth,
                transactions
        );

        return account;
    }
}
