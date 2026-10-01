package com.example.bankmanagement.repository;

import com.example.bankmanagement.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByUsername(String username);

    Optional<Customer> findByEmail(String email);

    Optional<Customer> findByUsernameOrEmail(String username, String email);

    Optional<Customer> findByAccountNumber(String accountNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select customer from Customer customer where customer.id = :id")
    Optional<Customer> findByIdForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select customer from Customer customer where customer.accountNumber = :accountNumber")
    Optional<Customer> findByAccountNumberForUpdate(@Param("accountNumber") String accountNumber);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByAccountNumber(String accountNumber);

    long countByAccountStatus(String accountStatus);

    java.util.List<Customer> findByAccountStatusOrderByCreatedAtDesc(String accountStatus);
}
