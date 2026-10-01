package com.example.bankmanagement.service;

import com.example.bankmanagement.dto.CustomerResponse;
import com.example.bankmanagement.dto.TransactionResponse;

import java.util.List;
import java.util.Map;

public interface OfficerService {
    Map<String, Object> dashboard();
    List<CustomerResponse> customers(String status, String search);
    CustomerResponse customer(Long id);
    CustomerResponse updateStatus(Long id, String action);
    List<TransactionResponse> transactions(String type, String account, String date);
}