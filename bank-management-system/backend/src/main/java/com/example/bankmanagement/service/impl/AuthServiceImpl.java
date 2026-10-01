package com.example.bankmanagement.service.impl;

import com.example.bankmanagement.dto.CustomerResponse;
import com.example.bankmanagement.dto.LoginResponse;
import com.example.bankmanagement.entity.Customer;
import com.example.bankmanagement.exception.BankingRuleException;
import com.example.bankmanagement.exception.CustomerNotFoundException;
import com.example.bankmanagement.repository.CustomerRepository;
import com.example.bankmanagement.service.AuthService;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final CustomerRepository customerRepository;

    public AuthServiceImpl(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public LoginResponse login(String authenticatedUsername) {
        Customer customer = customerRepository.findByUsername(authenticatedUsername)
                .orElseThrow(() -> new CustomerNotFoundException("Customer account not found."));
        String status = customer.getAccountStatus();
        if ("REJECTED".equals(status)) {
            throw new BankingRuleException("Your account registration has been rejected.");
        }

        CustomerResponse customerResponse = CustomerResponse.fromEntity(customer);
        String message = switch (status) {
            case "PENDING" -> "Your account is pending bank officer approval.";
            case "BLOCKED" -> "Your account has been blocked. Please contact the bank.";
            default -> "Login successful.";
        };
        return new LoginResponse(true, message, customerResponse);
    }

    @Override
    public void logout() {
        // Stateless/session logout logic handled on client side
    }
}
