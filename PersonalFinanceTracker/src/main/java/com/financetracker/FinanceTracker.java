package com.financetracker;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class FinanceTracker {

    private final List<Transaction> transactions = new ArrayList<>();
    private int nextId = 1;

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

    void addExistingTransaction(Transaction t) {
        transactions.add(t);
        if (t.getId() >= nextId) {
            nextId = t.getId() + 1;
        }
    }

    public boolean deleteTransaction(int id) {
        return transactions.removeIf(t -> t.getId() == id);
    }

    public List<Transaction> getAllTransactions() {
        return Collections.unmodifiableList(transactions);
    }

    public double getBalance() {
        return transactions.stream().mapToDouble(Transaction::getSignedAmount).sum();
    }

    public double getTotalIncome() {
        return transactions.stream()
                .filter(t -> t.getType() == TransactionType.INCOME)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    public double getTotalExpenses() {
        return transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

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

    public List<Transaction> getTransactionsInRange(LocalDate start, LocalDate end) {
        return transactions.stream()
                .filter(t -> !t.getDate().isBefore(start) && !t.getDate().isAfter(end))
                .collect(Collectors.toList());
    }

    public int size() {
        return transactions.size();
    }
}
