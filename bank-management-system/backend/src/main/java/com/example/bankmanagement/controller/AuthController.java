package com.example.bankmanagement.controller;

import com.example.bankmanagement.dto.LoginRequest;
import com.example.bankmanagement.dto.LoginResponse;
import com.example.bankmanagement.service.AuthService;
import com.example.bankmanagement.exception.InvalidCredentialsException;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final AuthService authService;

    public AuthController(AuthenticationManager authenticationManager,
                          SecurityContextRepository securityContextRepository,
                          AuthService authService) {
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
        this.authService = authService;
    }

    @PostMapping({"/login", "/customer/login"})
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest,
                                               HttpServletRequest request,
                                               HttpServletResponse response) {
        Authentication authentication = authenticate(loginRequest);
        requireRole(authentication, "ROLE_CUSTOMER");
        LoginResponse loginResponse = authService.login(authentication.getName());
        if ("ACTIVE".equals(loginResponse.getCustomer().getAccountStatus())) {
            saveAuthentication(authentication, request, response);
        }
        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/officer/login")
    public ResponseEntity<Map<String, Object>> officerLogin(@Valid @RequestBody LoginRequest loginRequest,
                                                             HttpServletRequest request,
                                                             HttpServletResponse response) {
        Authentication authentication = authenticate(loginRequest);
        requireRole(authentication, "ROLE_OFFICER");
        saveAuthentication(authentication, request, response);
        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("message", "Officer login successful.");
        body.put("username", authentication.getName());
        body.put("role", "OFFICER");
        return ResponseEntity.ok(body);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpServletRequest request) {
        SecurityContextHolder.clearContext();
        if (request.getSession(false) != null) request.getSession(false).invalidate();
        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("message", "Logged out successfully.");
        return ResponseEntity.ok(body);
    }

    private Authentication authenticate(LoginRequest loginRequest) {
        try {
            return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsernameOrEmail(), loginRequest.getPassword()));
        } catch (AuthenticationException exception) {
            throw new InvalidCredentialsException("Invalid username or password.");
        }
    }

    private void requireRole(Authentication authentication, String expectedRole) {
        boolean hasRole = authentication.getAuthorities().stream()
                .anyMatch(authority -> expectedRole.equals(authority.getAuthority()));
        if (!hasRole) throw new InvalidCredentialsException("Invalid username or password.");
    }

    private void saveAuthentication(Authentication authentication, HttpServletRequest request,
                                    HttpServletResponse response) {
        if (request.getSession(false) != null) request.getSession(false).invalidate();
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);
    }

}
