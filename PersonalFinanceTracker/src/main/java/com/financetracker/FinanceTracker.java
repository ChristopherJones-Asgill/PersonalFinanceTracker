package com.financetracker;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

// Stores transactions and calculates financial totals.
public class FinanceTracker {

    private final List<Transaction> transactions = new ArrayList<>();
    private int nextId = 1;

    // Adds a new transaction after checking the input.
    public Transaction addTransaction(LocalDate date, String description, String category,
                                       double amount, TransactionType type)
            throws InvalidTransactionException {
        if (amount <= 0) {
            throw new InvalidTransactionException("Amount must be greater than zero.");
        }
        if (description == null || description.isBlank()) {
            throw new InvalidTransactionException("Description cannot be empty.");
        }
        if (category == null || category.isBlank()) {
            category = "Uncategorized";
        }

        Transaction t = new Transaction(nextId++, date, description.trim(), category.trim(), amount, type);
        transactions.add(t);
        return t;
    }

    // Adds a transaction that was loaded from the saved file.
    void addExistingTransaction(Transaction t) {
        transactions.add(t);
        if (t.getId() >= nextId) {
            nextId = t.getId() + 1;
        }
    }

    // Deletes a transaction with the matching ID.
    public boolean deleteTransaction(int id) {
        return transactions.removeIf(t -> t.getId() == id);
    }

    // Returns all transactions without allowing the list to be changed directly.
    public List<Transaction> getAllTransactions() {
        return Collections.unmodifiableList(transactions);
    }

    // Calculates the current balance.
    public double getBalance() {
        return transactions.stream().mapToDouble(Transaction::getSignedAmount).sum();
    }

    // Calculates total income.
    public double getTotalIncome() {
        return transactions.stream()
                .filter(t -> t.getType() == TransactionType.INCOME)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    // Calculates total expenses.
    public double getTotalExpenses() {
        return transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    // Groups expenses by category from highest to lowest.
    public Map<String, Double> getExpensesByCategory() {
        Map<String, Double> totals = transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .collect(Collectors.groupingBy(Transaction::getCategory,
                        Collectors.summingDouble(Transaction::getAmount)));

        return totals.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (a, b) -> a, LinkedHashMap::new));
    }

    // Returns transactions between the given dates.
    public List<Transaction> getTransactionsInRange(LocalDate start, LocalDate end) {
        return transactions.stream()
                .filter(t -> !t.getDate().isBefore(start) && !t.getDate().isAfter(end))
                .collect(Collectors.toList());
    }

    // Returns the number of transactions.
    public int size() {
        return transactions.size();
    }
}
