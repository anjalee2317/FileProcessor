package com.example.recordprocessor.repository;


import com.example.recordprocessor.model.TransactionRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TransactionRecordRepository extends JpaRepository<TransactionRecord, Long> {

    @Query("SELECT t FROM TransactionRecord t WHERE (:customerId IS NULL OR t.customerId = :customerId) " +
            "AND ((:accountNumbers) IS NULL OR t.accountNumber IN (:accountNumbers)) " +
            "AND (:description IS NULL OR LOWER(t.description) LIKE LOWER(CONCAT('%', :description, '%')) )")
    Page<TransactionRecord> findByCustomerIdOrAccountNumberInOrDescriptionContainingIgnoreCase(
            String customerId, List<String> accountNumbers, String description, Pageable pageable);

}