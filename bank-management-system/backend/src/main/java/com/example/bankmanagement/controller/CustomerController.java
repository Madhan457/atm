package com.example.bankmanagement.controller;

import com.example.bankmanagement.dto.CustomerResponse;
import com.example.bankmanagement.dto.RegisterRequest;
import com.example.bankmanagement.dto.UpdateCustomerRequest;
import com.example.bankmanagement.exception.CustomerNotFoundException;
import com.example.bankmanagement.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/customers")
@CrossOrigin(origins = {"http://localhost:5500", "http://127.0.0.1:5500", "http://localhost:5501", "http://127.0.0.1:5501"})
public class CustomerController {

    private final CustomerService customerService;

    @Autowired
    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterRequest registerRequest) {
        CustomerResponse customer = customerService.registerCustomer(registerRequest);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Customer registered successfully.");
        response.put("customer", customer);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getProfile(
            @RequestHeader(value = "X-Customer-Id", required = false) Long headerCustomerId,
            @RequestParam(value = "customerId", required = false) Long queryCustomerId) {

        Long customerId = resolveCustomerId(headerCustomerId, queryCustomerId);
        CustomerResponse profile = customerService.getCustomerProfile(customerId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("customer", profile);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/profile")
    public ResponseEntity<Map<String, Object>> updateProfile(
            @RequestHeader(value = "X-Customer-Id", required = false) Long headerCustomerId,
            @RequestParam(value = "customerId", required = false) Long queryCustomerId,
            @Valid @RequestBody UpdateCustomerRequest updateRequest) {

        Long customerId = resolveCustomerId(headerCustomerId, queryCustomerId);
        CustomerResponse updatedCustomer = customerService.updateCustomerProfile(customerId, updateRequest);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Profile updated successfully.");
        response.put("customer", updatedCustomer);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/account")
    public ResponseEntity<Map<String, Object>> deleteAccount(
            @RequestHeader(value = "X-Customer-Id", required = false) Long headerCustomerId,
            @RequestParam(value = "customerId", required = false) Long queryCustomerId) {

        Long customerId = resolveCustomerId(headerCustomerId, queryCustomerId);
        customerService.deleteCustomerAccount(customerId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Account deleted successfully.");
        return ResponseEntity.ok(response);
    }

    private Long resolveCustomerId(Long headerId, Long queryId) {
        if (headerId != null) return headerId;
        if (queryId != null) return queryId;
        throw new CustomerNotFoundException("Unauthorized: Customer session token/header (X-Customer-Id) is missing.");
    }
}
