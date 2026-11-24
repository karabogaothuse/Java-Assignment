
package com.bankingsystem.controller;

import com.bankingsystem.model.Account;
import com.bankingsystem.model.AccountFactory;
import com.bankingsystem.model.Customer;
import com.bankingsystem.model.SavingAccount;
import com.bankingsystem.model.InvestementAccount;
import com.bankingsystem.model.ChequeAccount;
import com.bankingsystem.model.Transaction;

import java.util.ArrayList;
import java.util.List;

// Controller: Handles account operations between the GUI and model.
public class AccountController {

    public SavingAccount openSavingsAccount(
            String accountNumber, double initialBalance,
            String branch, Customer customer) {

        return AccountFactory.openSavingsAccount(
                accountNumber, initialBalance, branch, customer
        );
    }

    public InvestementAccount openInvestmentAccount(
            String accountNumber, double initialBalance,
            String branch, Customer customer) {

        return AccountFactory.openInvestmentAccount(
                accountNumber, initialBalance, branch, customer
        );
    }

    public ChequeAccount openChequeAccount(
            String accountNumber, double initialBalance,
            String branch, Customer customer,
            String companyName, String companyAddress) {

        return AccountFactory.openChequeAccount(
                accountNumber, initialBalance, branch,
                customer, companyName, companyAddress
        );
    }

    public void deposit(Account account, double amount) {

        checkAccount(account);
        account.deposit(amount);
    }

    public void deposit(Account account, double amount, String description) {

        checkAccount(account);
        account.deposit(amount, description);
    }

    public void withdraw(Account account, double amount) {

        checkAccount(account);

        // Polymorphism: The correct withdrawal method depends on the account type.
        account.withdraw(amount);
    }

    public double applyMonthlyInterest(Account account) {

        checkAccount(account);

        return account.calculateMonthlyInterest();
    }

    public double applyMonthlyInterest(List<Account> accounts) {

        if (accounts == null) {
            throw new IllegalArgumentException("Account list cannot be null.");
        }

        double totalInterest = 0;

        for (Account account : accounts) {
            checkAccount(account);

            totalInterest += account.calculateMonthlyInterest();
        }

        return totalInterest;
    }

    public List<Transaction> getTransactionHistory(Account account) {

        checkAccount(account);

        return new ArrayList<>(account.getTransactions());
    }

    private void checkAccount(Account account) {

        if (account == null) {
            throw new IllegalArgumentException("Account cannot be null.");
        }
    }
}
