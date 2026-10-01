package com.example.bankmanagement.service;

import com.example.bankmanagement.dto.LoginResponse;

public interface AuthService {

    LoginResponse login(String authenticatedUsername);

    void logout();
}
