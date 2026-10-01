package com.example.bankmanagement.dto;

import com.example.bankmanagement.entity.BankTransaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(
        Long id,
        String transactionType,
        BigDecimal amount,
        BigDecimal balanceBefore,
        BigDecimal balanceAfter,
        String senderAccount,
        String recipientAccount,
        String description,
        String direction,
        String status,
        String referenceId,
        LocalDateTime date,
        Long customerId,
        String customerName,
        String accountNumber) {

    public static TransactionResponse fromEntity(BankTransaction transaction) {
        return new TransactionResponse(
                transaction.getId(), transaction.getTransactionType(), transaction.getAmount(),
                transaction.getBalanceBefore(), transaction.getBalanceAfter(), transaction.getSenderAccount(),
                transaction.getRecipientAccount(), transaction.getDescription(), transaction.getDirection(),
                transaction.getStatus(), transaction.getReferenceId(), transaction.getCreatedAt(),
                transaction.getCustomer().getId(), transaction.getCustomer().getFullName(),
                transaction.getCustomer().getAccountNumber());
    }
}