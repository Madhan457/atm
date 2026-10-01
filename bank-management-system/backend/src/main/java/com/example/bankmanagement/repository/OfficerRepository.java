package com.example.bankmanagement.repository;

import com.example.bankmanagement.entity.Officer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OfficerRepository extends JpaRepository<Officer, Long> {
    Optional<Officer> findByUsername(String username);
    boolean existsByUsername(String username);
}