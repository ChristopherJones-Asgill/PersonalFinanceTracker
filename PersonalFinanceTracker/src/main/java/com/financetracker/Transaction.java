package com.financetracker;

import java.time.LocalDate;

// Represents one income or expense transaction.
public class Transaction {

    private final int id;
    private final LocalDate date;
    private final String description;
    private final String category;
    private final double amount;
    private final TransactionType type;

    // Creates a transaction with its basic information.
    public Transaction(int id, LocalDate date, String description, String category,
                        double amount, TransactionType type) {
        this.id = id;
        this.date = date;
        this.description = description;
        this.category = category;
        this.amount = amount;
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public double getAmount() {
        return amount;
    }

    public TransactionType getType() {
        return type;
    }

    // Returns income as positive and expenses as negative.
    public double getSignedAmount() {
        return type == TransactionType.INCOME ? amount : -amount;
    }

    // Converts the transaction into a line that can be saved in a CSV file.
    public String toCsvLine() {

        String safeDescription = description.replace(",", ";");
        String safeCategory = category.replace(",", ";");
        return String.join(",", String.valueOf(id), date.toString(), safeDescription,
                safeCategory, String.valueOf(amount), type.name());
    }

    // Creates a transaction from one line of the CSV file.
    public static Transaction fromCsvLine(String line) {
        String[] parts = line.split(",", -1);
        int id = Integer.parseInt(parts[0]);
        LocalDate date = LocalDate.parse(parts[1]);
        String description = parts[2];
        String category = parts[3];
        double amount = Double.parseDouble(parts[4]);
        TransactionType type = TransactionType.valueOf(parts[5]);
        return new Transaction(id, date, description, category, amount, type);
    }

    // Formats the transaction for display in the console.
    @Override
    public String toString() {
        String sign = type == TransactionType.INCOME ? "+" : "-";
        return String.format("#%-4d %-12s %-20s %-15s %s$%,.2f",
                id, date, description, category, sign, amount);
    }
}
