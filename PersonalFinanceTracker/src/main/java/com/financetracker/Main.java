package com.financetracker;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {

    private static final String DATA_FILE = "data/transactions.csv";
    private static final FinanceTracker tracker = new FinanceTracker();
    private static final FileManager fileManager = new FileManager(DATA_FILE);
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        try {
            fileManager.loadInto(tracker);
        } catch (IOException e) {
            System.out.println("Could not load existing data: " + e.getMessage());
        }

        System.out.println("=== Personal Finance Tracker ===");
        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> addTransaction(TransactionType.INCOME);
                case "2" -> addTransaction(TransactionType.EXPENSE);
                case "3" -> listTransactions();
                case "4" -> showSummary();
                case "5" -> deleteTransaction();
                case "6" -> running = false;
                default -> System.out.println("Invalid option, please try again.");
            }
        }

        saveAndExit();
    }

    private static void printMenu() {
        System.out.println("\n1. Add income");
        System.out.println("2. Add expense");
        System.out.println("3. View all transactions");
        System.out.println("4. View summary (balance + spending by category)");
        System.out.println("5. Delete a transaction");
        System.out.println("6. Save and exit");
        System.out.print("Choose an option: ");
    }

    private static void addTransaction(TransactionType type) {
        System.out.print("Description: ");
        String description = scanner.nextLine();

        System.out.print("Category (e.g. Food, Rent, Salary): ");
        String category = scanner.nextLine();

        System.out.print("Amount: ");
        double amount;
        try {
            amount = Double.parseDouble(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("That doesn't look like a valid number. Transaction cancelled.");
            return;
        }

        System.out.print("Date (YYYY-MM-DD), or press Enter for today: ");
        String dateInput = scanner.nextLine().trim();
        LocalDate date;
        try {
            date = dateInput.isEmpty() ? LocalDate.now() : LocalDate.parse(dateInput);
        } catch (DateTimeParseException e) {
            System.out.println("That date format wasn't recognized. Using today's date instead.");
            date = LocalDate.now();
        }

        try {
            Transaction t = tracker.addTransaction(date, description, category, amount, type);
            System.out.println("Added: " + t);
        } catch (InvalidTransactionException e) {
            System.out.println("Could not add transaction: " + e.getMessage());
        }
    }

    private static void listTransactions() {
        List<Transaction> all = tracker.getAllTransactions();
        if (all.isEmpty()) {
            System.out.println("No transactions recorded yet.");
            return;
        }
        System.out.println("\n--- All Transactions ---");
        for (Transaction t : all) {
            System.out.println(t);
        }
    }

    private static void showSummary() {
        System.out.println("\n--- Summary ---");
        System.out.printf("Total income:   $%,.2f%n", tracker.getTotalIncome());
        System.out.printf("Total expenses: $%,.2f%n", tracker.getTotalExpenses());
        System.out.printf("Balance:        $%,.2f%n", tracker.getBalance());

        Map<String, Double> byCategory = tracker.getExpensesByCategory();
        if (!byCategory.isEmpty()) {
            System.out.println("\nSpending by category:");
            for (Map.Entry<String, Double> entry : byCategory.entrySet()) {
                System.out.printf("  %-15s $%,.2f%n", entry.getKey(), entry.getValue());
            }
        }
    }

    private static void deleteTransaction() {
        listTransactions();
        if (tracker.size() == 0) {
            return;
        }
        System.out.print("Enter the ID of the transaction to delete: ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());
            if (tracker.deleteTransaction(id)) {
                System.out.println("Transaction #" + id + " deleted.");
            } else {
                System.out.println("No transaction found with that ID.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid numeric ID.");
        }
    }

    private static void saveAndExit() {
        try {
            fileManager.save(tracker.getAllTransactions());
            System.out.println("Data saved to " + DATA_FILE + ". Goodbye!");
        } catch (IOException e) {
            System.out.println("Failed to save data: " + e.getMessage());
        }
    }
}
