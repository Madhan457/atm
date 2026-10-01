package com.example.bankmanagement.controller;

import com.example.bankmanagement.dto.CustomerResponse;
import com.example.bankmanagement.dto.ChangePasswordRequest;
import com.example.bankmanagement.dto.RegisterRequest;
import com.example.bankmanagement.dto.UpdateCustomerRequest;
import com.example.bankmanagement.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

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
    public ResponseEntity<Map<String, Object>> getProfile(@AuthenticationPrincipal UserDetails principal) {
        CustomerResponse profile = customerService.getCustomerProfile(principal.getUsername());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("customer", profile);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/profile")
    public ResponseEntity<Map<String, Object>> updateProfile(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody UpdateCustomerRequest updateRequest) {

        CustomerResponse updatedCustomer = customerService.updateCustomerProfile(principal.getUsername(), updateRequest);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Profile updated successfully.");
        response.put("customer", updatedCustomer);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/account")
    public ResponseEntity<Map<String, Object>> deleteAccount(@AuthenticationPrincipal UserDetails principal) {
        customerService.deleteCustomerAccount(principal.getUsername());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Account deleted successfully.");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/password")
    public ResponseEntity<Map<String, Object>> changePassword(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody ChangePasswordRequest request) {
        customerService.changePassword(principal.getUsername(), request);
        return ResponseEntity.ok(Map.of("success", true, "message", "Password changed successfully."));
    }
}
