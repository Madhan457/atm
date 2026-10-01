package com.example.bankmanagement.service;

import com.example.bankmanagement.dto.LoginRequest;
import com.example.bankmanagement.dto.LoginResponse;

public interface AuthService {

    LoginResponse login(LoginRequest loginRequest);

    void logout();
}
