package com.example.bankmanagement.service.impl;

import com.example.bankmanagement.dto.CustomerResponse;
import com.example.bankmanagement.dto.TransactionResponse;
import com.example.bankmanagement.entity.Customer;
import com.example.bankmanagement.exception.BankingRuleException;
import com.example.bankmanagement.exception.CustomerNotFoundException;
import com.example.bankmanagement.repository.BankTransactionRepository;
import com.example.bankmanagement.repository.CustomerRepository;
import com.example.bankmanagement.service.OfficerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class OfficerServiceImpl implements OfficerService {

    private final CustomerRepository customerRepository;
    private final BankTransactionRepository transactionRepository;

    public OfficerServiceImpl(CustomerRepository customerRepository,
                              BankTransactionRepository transactionRepository) {
        this.customerRepository = customerRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> dashboard() {
        return Map.of(
                "totalCustomers", customerRepository.count(),
                "pendingAccounts", customerRepository.countByAccountStatus("PENDING"),
                "activeAccounts", customerRepository.countByAccountStatus("ACTIVE"),
                "blockedAccounts", customerRepository.countByAccountStatus("BLOCKED"),
                "rejectedAccounts", customerRepository.countByAccountStatus("REJECTED"),
                "totalTransactions", transactionRepository.countDistinctReferencesByType("DEPOSIT")
                        + transactionRepository.countDistinctReferencesByType("WITHDRAWAL")
                        + transactionRepository.countDistinctReferencesByType("TRANSFER"),
                "totalDeposits", transactionRepository.sumAmountByType("DEPOSIT"),
                "totalWithdrawals", transactionRepository.sumAmountByType("WITHDRAWAL"),
                "totalTransfers", transactionRepository.countDistinctReferencesByType("TRANSFER"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerResponse> customers(String status, String search) {
        List<Customer> customers = status == null || status.isBlank()
                ? customerRepository.findAll()
                : customerRepository.findByAccountStatusOrderByCreatedAtDesc(status.trim().toUpperCase(Locale.ROOT));
        String term = search == null ? "" : search.trim().toLowerCase(Locale.ROOT);
        return customers.stream()
                .filter(customer -> term.isEmpty()
                        || contains(customer.getFullName(), term)
                        || contains(customer.getEmail(), term)
                        || contains(customer.getPhone(), term)
                        || contains(customer.getAccountNumber(), term)
                        || contains(customer.getUsername(), term))
                .map(CustomerResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse customer(Long id) {
        return customerRepository.findById(id).map(CustomerResponse::fromEntity)
                .orElseThrow(() -> new CustomerNotFoundException("Customer account not found."));
    }

    @Override
    @Transactional
    public CustomerResponse updateStatus(Long id, String action) {
        Customer customer = customerRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new CustomerNotFoundException("Customer account not found."));
        String requiredStatus;
        String nextStatus;
        switch (action) {
            case "approve" -> { requiredStatus = "PENDING"; nextStatus = "ACTIVE"; }
            case "reject" -> { requiredStatus = "PENDING"; nextStatus = "REJECTED"; }
            case "block" -> { requiredStatus = "ACTIVE"; nextStatus = "BLOCKED"; }
            case "unblock" -> { requiredStatus = "BLOCKED"; nextStatus = "ACTIVE"; }
            default -> throw new BankingRuleException("Unsupported account action.");
        }
        if (!requiredStatus.equals(customer.getAccountStatus())) {
                throw new BankingRuleException("This account cannot be " + actionLabel(action)
                    + " from its current status.");
        }
        customer.setAccountStatus(nextStatus);
        return CustomerResponse.fromEntity(customerRepository.save(customer));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> transactions(String type, String account, String date) {
        String normalizedType = type == null || type.isBlank() ? "" : type.trim().toUpperCase(Locale.ROOT);
        String normalizedAccount = account == null ? "" : account.trim().toLowerCase(Locale.ROOT);
        LocalDate selectedDate = date == null || date.isBlank() ? null : LocalDate.parse(date);
        return transactionRepository.findAllByOrderByCreatedAtDesc().stream()
                .filter(transaction -> normalizedType.isEmpty() || normalizedType.equals(transaction.getTransactionType()))
                .filter(transaction -> normalizedAccount.isEmpty()
                        || contains(transaction.getCustomer().getAccountNumber(), normalizedAccount)
                        || contains(transaction.getSenderAccount(), normalizedAccount)
                        || contains(transaction.getRecipientAccount(), normalizedAccount))
                .filter(transaction -> selectedDate == null || transaction.getCreatedAt().toLocalDate().equals(selectedDate))
                .map(TransactionResponse::fromEntity)
                .toList();
    }

    private boolean contains(String value, String term) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(term);
    }

    private String actionLabel(String action) {
        return switch (action) {
            case "approve" -> "approved";
            case "reject" -> "rejected";
            case "block" -> "blocked";
            case "unblock" -> "unblocked";
            default -> action;
        };
    }
}