package com.financetracker;

import java.io.*;
import java.nio.file.*;
import java.util.List;

public class FileManager {

    private final Path filePath;

    public FileManager(String fileName) {
        this.filePath = Paths.get(fileName);
    }

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
