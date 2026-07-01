package com.bank.dto;

public class AccountSummary {

    private final String username;
    private final double openingBalance;
    private final double totalSpending;
    private final double leftAmount;
    private final int transactionCount;

    public AccountSummary(String username, double openingBalance, double totalSpending, double leftAmount, int transactionCount) {
        this.username = username;
        this.openingBalance = openingBalance;
        this.totalSpending = totalSpending;
        this.leftAmount = leftAmount;
        this.transactionCount = transactionCount;
    }

    public String getUsername() {
        return username;
    }

    public double getOpeningBalance() {
        return openingBalance;
    }

    public double getTotalSpending() {
        return totalSpending;
    }

    public double getLeftAmount() {
        return leftAmount;
    }

    public int getTransactionCount() {
        return transactionCount;
    }
}
