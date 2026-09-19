package com.financetracker;

// Custom exception used when transaction input is invalid.
public class InvalidTransactionException extends Exception {
    public InvalidTransactionException(String message) {
        super(message);
    }
}
