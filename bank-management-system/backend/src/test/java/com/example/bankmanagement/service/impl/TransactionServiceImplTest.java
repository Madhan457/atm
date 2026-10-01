package com.example.bankmanagement.service.impl;

import com.example.bankmanagement.dto.TransactionRequest;
import com.example.bankmanagement.dto.TransactionResponse;
import com.example.bankmanagement.entity.BankTransaction;
import com.example.bankmanagement.entity.Customer;
import com.example.bankmanagement.exception.BankingRuleException;
import com.example.bankmanagement.repository.BankTransactionRepository;
import com.example.bankmanagement.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private BankTransactionRepository transactionRepository;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private Customer sender;

    @BeforeEach
    void setUp() {
        sender = customer(1L, "alice", "AC1000000001", "ACTIVE", "100.00");
        when(customerRepository.findByUsername("alice")).thenReturn(Optional.of(sender));
        when(customerRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(sender));
    }

    @Test
    void depositAddsBalanceAndCreatesLedgerRecord() {
        when(transactionRepository.save(any(BankTransaction.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        TransactionResponse result = transactionService.deposit("alice", request("25.50", null));

        assertEquals(new BigDecimal("125.50"), sender.getBalance());
        assertEquals(new BigDecimal("100.00"), result.balanceBefore());
        assertEquals(new BigDecimal("125.50"), result.balanceAfter());
        assertEquals("DEPOSIT", result.transactionType());
        verify(customerRepository).save(sender);
        verify(transactionRepository).save(any(BankTransaction.class));
    }

    @Test
    void withdrawalRejectsInsufficientFundsWithoutWriting() {
        assertThrows(BankingRuleException.class,
                () -> transactionService.withdraw("alice", request("101.00", null)));

        assertEquals(new BigDecimal("100.00"), sender.getBalance());
        verify(customerRepository, never()).save(any(Customer.class));
        verify(transactionRepository, never()).save(any(BankTransaction.class));
    }

    @Test
    void pendingAccountCannotDeposit() {
        sender.setAccountStatus("PENDING");

        BankingRuleException exception = assertThrows(BankingRuleException.class,
                () -> transactionService.deposit("alice", request("5.00", null)));

        assertEquals("Your account is pending bank officer approval.", exception.getMessage());
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void transferUpdatesBothBalancesAndWritesDebitAndCredit() {
        Customer recipient = customer(2L, "bob", "AC1000000002", "ACTIVE", "40.00");
        when(customerRepository.findByAccountNumber("AC1000000002")).thenReturn(Optional.of(recipient));
        when(customerRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(recipient));
        when(transactionRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        TransactionResponse result = transactionService.transfer("alice", request("30.00", "AC1000000002"));

        assertEquals(new BigDecimal("70.00"), sender.getBalance());
        assertEquals(new BigDecimal("70.00"), recipient.getBalance());
        assertEquals("DEBIT", result.direction());
        ArgumentCaptor<List<BankTransaction>> entriesCaptor = ArgumentCaptor.forClass(List.class);
        verify(transactionRepository).saveAll(entriesCaptor.capture());
        List<BankTransaction> entries = entriesCaptor.getValue();
        assertEquals(2, entries.size());
        assertEquals("DEBIT", entries.get(0).getDirection());
        assertEquals("CREDIT", entries.get(1).getDirection());
        assertEquals(entries.get(0).getReferenceId(), entries.get(1).getReferenceId());
    }

    private TransactionRequest request(String amount, String recipientAccountNumber) {
        TransactionRequest request = new TransactionRequest();
        request.setAmount(new BigDecimal(amount));
        request.setRecipientAccountNumber(recipientAccountNumber);
        request.setDescription("Unit test transaction");
        return request;
    }

    private Customer customer(Long id, String username, String accountNumber, String status, String balance) {
        Customer customer = new Customer();
        customer.setId(id);
        customer.setUsername(username);
        customer.setFullName(username);
        customer.setAccountNumber(accountNumber);
        customer.setAccountStatus(status);
        customer.setEmail(username + "@example.test");
        customer.setPhone("5550000");
        customer.setDateOfBirth(LocalDate.of(1990, 1, 1));
        customer.setBalance(new BigDecimal(balance));
        return customer;
    }
}
