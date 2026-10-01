package com.example.bankmanagement.controller;

import com.example.bankmanagement.dto.TransactionRequest;
import com.example.bankmanagement.dto.TransactionResponse;
import com.example.bankmanagement.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/deposit")
    public Map<String, Object> deposit(@AuthenticationPrincipal UserDetails principal,
                                       @Valid @RequestBody TransactionRequest request) {
        return result("Deposit completed.", transactionService.deposit(principal.getUsername(), request));
    }

    @PostMapping("/withdraw")
    public Map<String, Object> withdraw(@AuthenticationPrincipal UserDetails principal,
                                        @Valid @RequestBody TransactionRequest request) {
        return result("Withdrawal completed.", transactionService.withdraw(principal.getUsername(), request));
    }

    @PostMapping("/transfer")
    public Map<String, Object> transfer(@AuthenticationPrincipal UserDetails principal,
                                        @Valid @RequestBody TransactionRequest request) {
        return result("Transfer completed.", transactionService.transfer(principal.getUsername(), request));
    }

    @GetMapping({"/customer", "/customer/history"})
    public List<TransactionResponse> customerHistory(@AuthenticationPrincipal UserDetails principal,
                                                      @RequestParam(required = false) String type) {
        return transactionService.customerHistory(principal.getUsername(), type);
    }

    private Map<String, Object> result(String message, TransactionResponse transaction) {
        return Map.of("success", true, "message", message, "transaction", transaction);
    }
}