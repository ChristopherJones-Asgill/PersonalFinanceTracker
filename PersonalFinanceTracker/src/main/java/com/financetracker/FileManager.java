package com.financetracker;

import java.io.*;
import java.nio.file.*;
import java.util.List;

// Handles saving and loading transactions from a file.
public class FileManager {

    private final Path filePath;

    // Sets the file used to store transaction data.
    public FileManager(String fileName) {
        this.filePath = Paths.get(fileName);
    }

    // Saves all transactions to the CSV file.
    public void save(List<Transaction> transactions) throws IOException {
        Path parent = filePath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
            writer.write("id,date,description,category,amount,type");
            writer.newLine();
            for (Transaction t : transactions) {
                writer.write(t.toCsvLine());
                writer.newLine();
            }
        }
    }

    // Loads saved transactions into the finance tracker.
    public void loadInto(FinanceTracker tracker) throws IOException {
        if (!Files.exists(filePath)) {
            return; 
        }

        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            String line = reader.readLine();
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                try {
                    Transaction t = Transaction.fromCsvLine(line);
                    tracker.addExistingTransaction(t);
                } catch (Exception e) {
                    System.out.println("Skipping corrupted line in data file: " + line);
                }
            }
        }
    }
}
