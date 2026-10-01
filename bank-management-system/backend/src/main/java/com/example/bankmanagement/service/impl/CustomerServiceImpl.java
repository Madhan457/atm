package com.example.bankmanagement.service.impl;

import com.example.bankmanagement.dto.CustomerResponse;
import com.example.bankmanagement.dto.RegisterRequest;
import com.example.bankmanagement.dto.UpdateCustomerRequest;
import com.example.bankmanagement.entity.Customer;
import com.example.bankmanagement.exception.CustomerNotFoundException;
import com.example.bankmanagement.exception.DuplicateResourceException;
import com.example.bankmanagement.repository.CustomerRepository;
import com.example.bankmanagement.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;

@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom random = new SecureRandom();

    @Autowired
    public CustomerServiceImpl(CustomerRepository customerRepository, PasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public CustomerResponse registerCustomer(RegisterRequest registerRequest) {
        if (customerRepository.existsByUsername(registerRequest.getUsername())) {
            throw new DuplicateResourceException("Username '" + registerRequest.getUsername() + "' is already taken.");
        }

        if (customerRepository.existsByEmail(registerRequest.getEmail())) {
            throw new DuplicateResourceException("Email '" + registerRequest.getEmail() + "' is already registered.");
        }

        Customer customer = new Customer();
        customer.setFullName(registerRequest.getFullName());
        customer.setEmail(registerRequest.getEmail());
        customer.setPhone(registerRequest.getPhone());
        customer.setDateOfBirth(registerRequest.getDateOfBirth());
        customer.setAddress(registerRequest.getAddress());
        customer.setUsername(registerRequest.getUsername());
        
        // BCrypt Password Encryption
        customer.setPasswordHash(passwordEncoder.encode(registerRequest.getPassword()));

        // Account Number Generation (Format: AC100000XXXX)
        customer.setAccountNumber(generateUniqueAccountNumber());
        customer.setAccountType(registerRequest.getAccountType() != null ? registerRequest.getAccountType() : "Savings");
        customer.setBalance(new BigDecimal("10000.00"));

        Customer savedCustomer = customerRepository.save(customer);
        return CustomerResponse.fromEntity(savedCustomer);
    }

    @Override
    public CustomerResponse getCustomerProfile(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer account not found with ID: " + customerId));
        return CustomerResponse.fromEntity(customer);
    }

    @Override
    @Transactional
    public CustomerResponse updateCustomerProfile(Long customerId, UpdateCustomerRequest updateRequest) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer account not found with ID: " + customerId));

        if (!customer.getEmail().equalsIgnoreCase(updateRequest.getEmail()) && customerRepository.existsByEmail(updateRequest.getEmail())) {
            throw new DuplicateResourceException("Email '" + updateRequest.getEmail() + "' is already registered to another user.");
        }

        customer.setFullName(updateRequest.getFullName());
        customer.setEmail(updateRequest.getEmail());
        customer.setPhone(updateRequest.getPhone());
        customer.setAddress(updateRequest.getAddress());

        Customer updatedCustomer = customerRepository.save(customer);
        return CustomerResponse.fromEntity(updatedCustomer);
    }

    @Override
    @Transactional
    public void deleteCustomerAccount(Long customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new CustomerNotFoundException("Customer account not found with ID: " + customerId);
        }
        customerRepository.deleteById(customerId);
    }

    private String generateUniqueAccountNumber() {
        String accountNumber;
        do {
            long number = 1000000000L + (long)(random.nextDouble() * 9000000000L);
            accountNumber = "AC" + number;
        } while (customerRepository.existsByAccountNumber(accountNumber));
        return accountNumber;
    }
}
