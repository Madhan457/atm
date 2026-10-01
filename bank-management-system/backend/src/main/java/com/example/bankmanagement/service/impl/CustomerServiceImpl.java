package com.example.bankmanagement.service.impl;

import com.example.bankmanagement.dto.CustomerResponse;
import com.example.bankmanagement.dto.RegisterRequest;
import com.example.bankmanagement.dto.UpdateCustomerRequest;
import com.example.bankmanagement.dto.ChangePasswordRequest;
import com.example.bankmanagement.entity.Customer;
import com.example.bankmanagement.exception.CustomerNotFoundException;
import com.example.bankmanagement.exception.DuplicateResourceException;
import com.example.bankmanagement.repository.CustomerRepository;
import com.example.bankmanagement.repository.OfficerRepository;
import com.example.bankmanagement.repository.BankTransactionRepository;
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
    private final OfficerRepository officerRepository;
    private final BankTransactionRepository transactionRepository;
    private final SecureRandom random = new SecureRandom();

    @Autowired
    public CustomerServiceImpl(CustomerRepository customerRepository, PasswordEncoder passwordEncoder,
                               OfficerRepository officerRepository, BankTransactionRepository transactionRepository) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
        this.officerRepository = officerRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    @Transactional
    public CustomerResponse registerCustomer(RegisterRequest registerRequest) {
        if (customerRepository.existsByUsername(registerRequest.getUsername())) {
            throw new DuplicateResourceException("Username '" + registerRequest.getUsername() + "' is already taken.");
        }
        if (officerRepository.existsByUsername(registerRequest.getUsername())) {
            throw new DuplicateResourceException("Username is already taken.");
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
        customer.setBalance(BigDecimal.ZERO.setScale(2));
        customer.setAccountStatus("PENDING");

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
    public CustomerResponse getCustomerProfile(String username) {
        Customer customer = findByUsername(username);
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
    public CustomerResponse updateCustomerProfile(String username, UpdateCustomerRequest updateRequest) {
        Customer customer = findByUsername(username);
        if (!customer.getEmail().equalsIgnoreCase(updateRequest.getEmail())
                && customerRepository.existsByEmail(updateRequest.getEmail())) {
            throw new DuplicateResourceException("Email is already registered to another user.");
        }
        customer.setFullName(updateRequest.getFullName());
        customer.setEmail(updateRequest.getEmail());
        customer.setPhone(updateRequest.getPhone());
        customer.setAddress(updateRequest.getAddress());
        return CustomerResponse.fromEntity(customerRepository.save(customer));
    }

    @Override
    @Transactional
    public void deleteCustomerAccount(Long customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new CustomerNotFoundException("Customer account not found with ID: " + customerId);
        }
        transactionRepository.deleteByCustomer_Id(customerId);
        customerRepository.deleteById(customerId);
    }

    @Override
    @Transactional
    public void deleteCustomerAccount(String username) {
        Customer customer = findByUsername(username);
        transactionRepository.deleteByCustomer_Id(customer.getId());
        customerRepository.delete(customer);
    }

    @Override
    @Transactional
    public void changePassword(String username, ChangePasswordRequest request) {
        Customer customer = findByUsername(username);
        if (!passwordEncoder.matches(request.getCurrentPassword(), customer.getPasswordHash())) {
            throw new com.example.bankmanagement.exception.InvalidCredentialsException("Current password is incorrect.");
        }
        if (passwordEncoder.matches(request.getNewPassword(), customer.getPasswordHash())) {
            throw new com.example.bankmanagement.exception.BankingRuleException("Choose a password you have not used before.");
        }
        customer.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        customerRepository.save(customer);
    }

    private Customer findByUsername(String username) {
        return customerRepository.findByUsername(username)
                .orElseThrow(() -> new CustomerNotFoundException("Customer account not found."));
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
