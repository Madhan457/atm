package com.example.bankmanagement.service;

import com.example.bankmanagement.dto.CustomerResponse;
import com.example.bankmanagement.dto.RegisterRequest;
import com.example.bankmanagement.dto.UpdateCustomerRequest;
import com.example.bankmanagement.dto.ChangePasswordRequest;

public interface CustomerService {

    CustomerResponse registerCustomer(RegisterRequest registerRequest);

    CustomerResponse getCustomerProfile(Long customerId);

    CustomerResponse getCustomerProfile(String username);

    CustomerResponse updateCustomerProfile(Long customerId, UpdateCustomerRequest updateRequest);

    CustomerResponse updateCustomerProfile(String username, UpdateCustomerRequest updateRequest);

    void deleteCustomerAccount(Long customerId);

    void deleteCustomerAccount(String username);

    void changePassword(String username, ChangePasswordRequest request);
}
