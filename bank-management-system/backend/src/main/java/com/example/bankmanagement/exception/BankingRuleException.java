package com.example.bankmanagement.exception;

public class BankingRuleException extends RuntimeException {
    public BankingRuleException(String message) {
        super(message);
    }
}