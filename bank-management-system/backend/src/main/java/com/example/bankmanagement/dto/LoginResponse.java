package com.example.bankmanagement.dto;

public class LoginResponse {

    private boolean success;
    private String message;
    private CustomerResponse customer;

    public LoginResponse() {
    }

    public LoginResponse(boolean success, String message, CustomerResponse customer) {
        this.success = success;
        this.message = message;
        this.customer = customer;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public CustomerResponse getCustomer() {
        return customer;
    }

    public void setCustomer(CustomerResponse customer) {
        this.customer = customer;
    }
}
