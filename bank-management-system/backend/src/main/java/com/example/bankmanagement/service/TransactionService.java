package com.example.bankmanagement.service;

import com.example.bankmanagement.dto.TransactionRequest;
import com.example.bankmanagement.dto.TransactionResponse;

import java.util.List;

public interface TransactionService {
    TransactionResponse deposit(String username, TransactionRequest request);
    TransactionResponse withdraw(String username, TransactionRequest request);
    TransactionResponse transfer(String username, TransactionRequest request);
    List<TransactionResponse> customerHistory(String username, String type);
}