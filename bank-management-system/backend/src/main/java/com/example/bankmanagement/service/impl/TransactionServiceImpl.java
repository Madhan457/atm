package com.example.bankmanagement.service.impl;

import com.example.bankmanagement.dto.TransactionRequest;
import com.example.bankmanagement.dto.TransactionResponse;
import com.example.bankmanagement.entity.BankTransaction;
import com.example.bankmanagement.entity.Customer;
import com.example.bankmanagement.exception.BankingRuleException;
import com.example.bankmanagement.exception.CustomerNotFoundException;
import com.example.bankmanagement.repository.BankTransactionRepository;
import com.example.bankmanagement.repository.CustomerRepository;
import com.example.bankmanagement.service.TransactionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final CustomerRepository customerRepository;
    private final BankTransactionRepository transactionRepository;

    public TransactionServiceImpl(CustomerRepository customerRepository,
                                  BankTransactionRepository transactionRepository) {
        this.customerRepository = customerRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    @Transactional
    public TransactionResponse deposit(String username, TransactionRequest request) {
        Customer customer = lockCustomer(username);
        requireActive(customer);
        BigDecimal amount = validatedAmount(request.getAmount());
        BigDecimal before = customer.getBalance();
        customer.setBalance(before.add(amount));
        customerRepository.save(customer);
        return TransactionResponse.fromEntity(transactionRepository.save(createTransaction(
                customer, "DEPOSIT", amount, before, customer.getBalance(), null,
                customer.getAccountNumber(), "CREDIT", request.getDescription(), UUID.randomUUID().toString())));
    }

    @Override
    @Transactional
    public TransactionResponse withdraw(String username, TransactionRequest request) {
        Customer customer = lockCustomer(username);
        requireActive(customer);
        BigDecimal amount = validatedAmount(request.getAmount());
        BigDecimal before = customer.getBalance();
        if (before.compareTo(amount) < 0) throw new BankingRuleException("Insufficient balance.");
        customer.setBalance(before.subtract(amount));
        customerRepository.save(customer);
        return TransactionResponse.fromEntity(transactionRepository.save(createTransaction(
                customer, "WITHDRAWAL", amount, before, customer.getBalance(), customer.getAccountNumber(),
                null, "DEBIT", request.getDescription(), UUID.randomUUID().toString())));
    }

    @Override
    @Transactional
    public TransactionResponse transfer(String username, TransactionRequest request) {
        if (request.getRecipientAccountNumber() == null || request.getRecipientAccountNumber().isBlank()) {
            throw new BankingRuleException("Recipient account number is required.");
        }
        Customer senderReference = customerRepository.findByUsername(username)
                .orElseThrow(() -> new CustomerNotFoundException("Customer account not found."));
        Customer recipientReference = customerRepository.findByAccountNumber(request.getRecipientAccountNumber())
                .orElseThrow(() -> new CustomerNotFoundException("Recipient account was not found."));
        if (senderReference.getId().equals(recipientReference.getId())) {
            throw new BankingRuleException("You cannot transfer money to your own account.");
        }

        List<Long> ids = List.of(senderReference.getId(), recipientReference.getId()).stream()
                .sorted(Comparator.naturalOrder()).toList();
        Customer first = customerRepository.findByIdForUpdate(ids.get(0))
                .orElseThrow(() -> new CustomerNotFoundException("Account not found."));
        Customer second = customerRepository.findByIdForUpdate(ids.get(1))
                .orElseThrow(() -> new CustomerNotFoundException("Account not found."));
        Customer sender = first.getId().equals(senderReference.getId()) ? first : second;
        Customer recipient = first.getId().equals(recipientReference.getId()) ? first : second;
        requireActive(sender);
        requireActive(recipient);

        BigDecimal amount = validatedAmount(request.getAmount());
        BigDecimal senderBefore = sender.getBalance();
        BigDecimal recipientBefore = recipient.getBalance();
        if (senderBefore.compareTo(amount) < 0) throw new BankingRuleException("Insufficient balance.");
        sender.setBalance(senderBefore.subtract(amount));
        recipient.setBalance(recipientBefore.add(amount));
        customerRepository.save(sender);
        customerRepository.save(recipient);

        String reference = UUID.randomUUID().toString();
        String description = descriptionOrDefault(request.getDescription(), "Account transfer");
        BankTransaction debit = createTransaction(sender, "TRANSFER", amount, senderBefore, sender.getBalance(),
                sender.getAccountNumber(), recipient.getAccountNumber(), "DEBIT", description, reference);
        BankTransaction credit = createTransaction(recipient, "TRANSFER", amount, recipientBefore,
                recipient.getBalance(), sender.getAccountNumber(), recipient.getAccountNumber(), "CREDIT",
                description, reference);
        transactionRepository.saveAll(List.of(debit, credit));
        return TransactionResponse.fromEntity(debit);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> customerHistory(String username, String type) {
        Customer customer = customerRepository.findByUsername(username)
                .orElseThrow(() -> new CustomerNotFoundException("Customer account not found."));
        String normalizedType = type == null || type.isBlank() ? null : type.trim().toUpperCase();
        if (normalizedType != null && !List.of("DEPOSIT", "WITHDRAWAL", "TRANSFER").contains(normalizedType)) {
            throw new BankingRuleException("Unsupported transaction filter.");
        }
        return transactionRepository.findByCustomer_IdOrderByCreatedAtDesc(customer.getId()).stream()
                .filter(transaction -> normalizedType == null || normalizedType.equals(transaction.getTransactionType()))
                .map(TransactionResponse::fromEntity)
                .toList();
    }

    private Customer lockCustomer(String username) {
        Customer reference = customerRepository.findByUsername(username)
                .orElseThrow(() -> new CustomerNotFoundException("Customer account not found."));
        return customerRepository.findByIdForUpdate(reference.getId())
                .orElseThrow(() -> new CustomerNotFoundException("Customer account not found."));
    }

    private void requireActive(Customer customer) {
        if (!"ACTIVE".equals(customer.getAccountStatus())) {
            if ("PENDING".equals(customer.getAccountStatus())) {
                throw new BankingRuleException("Your account is pending bank officer approval.");
            }
            if ("BLOCKED".equals(customer.getAccountStatus())) {
                throw new BankingRuleException("Your account has been blocked. Please contact the bank.");
            }
            throw new BankingRuleException("This account is not available for banking operations.");
        }
    }

    private BigDecimal validatedAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BankingRuleException("Amount must be greater than zero.");
        }
        try {
            return amount.setScale(2, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException exception) {
            throw new BankingRuleException("Amount cannot have more than two decimal places.");
        }
    }

    private BankTransaction createTransaction(Customer customer, String type, BigDecimal amount,
                                              BigDecimal before, BigDecimal after, String sender,
                                              String recipient, String direction, String description,
                                              String reference) {
        BankTransaction transaction = new BankTransaction();
        transaction.setCustomer(customer);
        transaction.setTransactionType(type);
        transaction.setAmount(amount);
        transaction.setBalanceBefore(before);
        transaction.setBalanceAfter(after);
        transaction.setSenderAccount(sender);
        transaction.setRecipientAccount(recipient);
        transaction.setDirection(direction);
        transaction.setStatus("COMPLETED");
        transaction.setDescription(descriptionOrDefault(description, type));
        transaction.setReferenceId(reference);
        return transaction;
    }

    private String descriptionOrDefault(String description, String fallback) {
        return description == null || description.isBlank() ? fallback : description.trim();
    }
}