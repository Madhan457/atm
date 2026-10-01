package com.example.bankmanagement.dto;

public class AuthResponseDTO {

    private boolean success;
    private String message;
    private CustomerResponseDTO customer;

    public AuthResponseDTO() {
    }

    public AuthResponseDTO(boolean success, String message, CustomerResponseDTO customer) {
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

    public CustomerResponseDTO getCustomer() {
        return customer;
    }

    public void setCustomer(CustomerResponseDTO customer) {
        this.customer = customer;
    }
}
