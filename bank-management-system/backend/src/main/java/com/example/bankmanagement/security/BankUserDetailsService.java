package com.example.bankmanagement.security;

import com.example.bankmanagement.entity.Customer;
import com.example.bankmanagement.entity.Officer;
import com.example.bankmanagement.repository.CustomerRepository;
import com.example.bankmanagement.repository.OfficerRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class BankUserDetailsService implements UserDetailsService {

    private final CustomerRepository customerRepository;
    private final OfficerRepository officerRepository;

    public BankUserDetailsService(CustomerRepository customerRepository, OfficerRepository officerRepository) {
        this.customerRepository = customerRepository;
        this.officerRepository = officerRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Officer officer = officerRepository.findByUsername(username).orElse(null);
        if (officer != null) {
            return User.withUsername(officer.getUsername())
                    .password(officer.getPasswordHash())
                    .roles("OFFICER")
                    .build();
        }

        Customer customer = customerRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid username or password."));
        return User.withUsername(customer.getUsername())
                .password(customer.getPasswordHash())
                .roles("CUSTOMER")
                .build();
    }
}