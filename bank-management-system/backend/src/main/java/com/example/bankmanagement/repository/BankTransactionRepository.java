package com.example.bankmanagement.repository;

import com.example.bankmanagement.entity.BankTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BankTransactionRepository extends JpaRepository<BankTransaction, Long> {
    List<BankTransaction> findByCustomer_IdOrderByCreatedAtDesc(Long customerId);
    List<BankTransaction> findAllByOrderByCreatedAtDesc();
    void deleteByCustomer_Id(Long customerId);
    long countByTransactionType(String transactionType);

    @Query("select count(distinct transaction.referenceId) from BankTransaction transaction where transaction.transactionType = :type")
    long countDistinctReferencesByType(@Param("type") String type);

    @Query("select coalesce(sum(transaction.amount), 0) from BankTransaction transaction where transaction.transactionType = :type")
    java.math.BigDecimal sumAmountByType(@Param("type") String type);
}