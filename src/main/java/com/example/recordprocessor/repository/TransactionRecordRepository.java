package com.example.recordprocessor.repository;


import com.example.recordprocessor.model.TransactionRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransactionRecordRepository extends JpaRepository<TransactionRecord, Long> {

    @Query("SELECT t FROM TransactionRecord t WHERE (:customerId IS NULL OR t.customerId = :customerId) " +
            "AND (:accountNumber IS NULL OR t.accountNumber = :accountNumber) " +
            "AND (:description IS NULL OR LOWER(t.description) LIKE LOWER(CONCAT('%', :description, '%')) )")
    Page<TransactionRecord> findByCustomerIdOrAccountNumberInOrDescriptionContainingIgnoreCase(
            @Param("customerId") String customerId,
            @Param("accountNumber") String accountNumber,
            @Param("description") String description,
            Pageable pageable);

}