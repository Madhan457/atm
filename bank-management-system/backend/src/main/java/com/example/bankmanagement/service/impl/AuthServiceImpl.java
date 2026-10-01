package com.example.bankmanagement.service.impl;

import com.example.bankmanagement.dto.CustomerResponse;
import com.example.bankmanagement.dto.LoginRequest;
import com.example.bankmanagement.dto.LoginResponse;
import com.example.bankmanagement.entity.Customer;
import com.example.bankmanagement.exception.InvalidCredentialsException;
import com.example.bankmanagement.repository.CustomerRepository;
import com.example.bankmanagement.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthServiceImpl(CustomerRepository customerRepository, PasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        String usernameOrEmail = loginRequest.getUsernameOrEmail();

        Customer customer = customerRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid username or password."));

        if (!passwordEncoder.matches(loginRequest.getPassword(), customer.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid username or password.");
        }

        CustomerResponse customerResponse = CustomerResponse.fromEntity(customer);
        return new LoginResponse(true, "Login successful!", customerResponse);
    }

    @Override
    public void logout() {
        // Stateless/session logout logic handled on client side
    }
}
