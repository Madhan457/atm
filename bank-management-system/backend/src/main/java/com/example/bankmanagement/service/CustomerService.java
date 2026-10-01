package com.example.bankmanagement.service;

import com.example.bankmanagement.dto.CustomerResponse;
import com.example.bankmanagement.dto.RegisterRequest;
import com.example.bankmanagement.dto.UpdateCustomerRequest;

public interface CustomerService {

    CustomerResponse registerCustomer(RegisterRequest registerRequest);

    CustomerResponse getCustomerProfile(Long customerId);

    CustomerResponse updateCustomerProfile(Long customerId, UpdateCustomerRequest updateRequest);

    void deleteCustomerAccount(Long customerId);
}
