package com.example.bankmanagement.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class TransactionRequest {

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    @Digits(integer = 13, fraction = 2, message = "Amount supports up to 13 digits and 2 decimal places")
    private BigDecimal amount;

    @Size(max = 100, message = "Recipient account number is too long")
    private String recipientAccountNumber;

    @Size(max = 500, message = "Description must be 500 characters or fewer")
    private String description;

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getRecipientAccountNumber() { return recipientAccountNumber; }
    public void setRecipientAccountNumber(String recipientAccountNumber) { this.recipientAccountNumber = recipientAccountNumber; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}