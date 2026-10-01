package com.example.bankmanagement.security;

import com.example.bankmanagement.entity.Officer;
import com.example.bankmanagement.repository.CustomerRepository;
import com.example.bankmanagement.repository.OfficerRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class OfficerBootstrap implements ApplicationRunner {

    private final OfficerRepository officerRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final String username;
    private final String password;

    public OfficerBootstrap(OfficerRepository officerRepository, CustomerRepository customerRepository,
                            PasswordEncoder passwordEncoder,
                            @Value("${bank.officer.bootstrap.username:}") String username,
                            @Value("${bank.officer.bootstrap.password:}") String password) {
        this.officerRepository = officerRepository;
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
        this.username = username;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (username.isBlank() || password.isBlank() || officerRepository.count() > 0) return;
        if (customerRepository.existsByUsername(username)) {
            throw new IllegalStateException("Officer bootstrap username is already used by a customer.");
        }
        Officer officer = new Officer();
        officer.setUsername(username);
        officer.setPasswordHash(passwordEncoder.encode(password));
        officerRepository.save(officer);
    }
}