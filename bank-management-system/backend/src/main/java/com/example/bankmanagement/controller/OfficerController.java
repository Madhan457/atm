package com.example.bankmanagement.controller;

import com.example.bankmanagement.dto.CustomerResponse;
import com.example.bankmanagement.dto.TransactionResponse;
import com.example.bankmanagement.service.OfficerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/officers")
public class OfficerController {

    private final OfficerService officerService;

    public OfficerController(OfficerService officerService) {
        this.officerService = officerService;
    }

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard() {
        return officerService.dashboard();
    }

    @GetMapping("/customers")
    public List<CustomerResponse> customers(@RequestParam(required = false) String status,
                                            @RequestParam(required = false) String search) {
        return officerService.customers(status, search);
    }

    @GetMapping("/customers/pending")
    public List<CustomerResponse> pendingCustomers() {
        return officerService.customers("PENDING", null);
    }

    @GetMapping("/customers/{id}")
    public CustomerResponse customer(@PathVariable Long id) {
        return officerService.customer(id);
    }

    @PutMapping("/customers/{id}/{action}")
    public CustomerResponse updateStatus(@PathVariable Long id, @PathVariable String action) {
        return officerService.updateStatus(id, action.toLowerCase());
    }

    @GetMapping("/transactions")
    public List<TransactionResponse> transactions(@RequestParam(required = false) String type,
                                                  @RequestParam(required = false) String account,
                                                  @RequestParam(required = false) String date) {
        return officerService.transactions(type, account, date);
    }

    @GetMapping("/reports")
    public Map<String, Object> reports() {
        return officerService.dashboard();
    }
}