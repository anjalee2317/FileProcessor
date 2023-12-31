package com.example.recordprocessor.service;

import com.example.recordprocessor.model.TransactionRecord;
import com.example.recordprocessor.repository.TransactionRecordRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TransactionRecordService {

    private final TransactionRecordRepository transactionRecordRepository;

    public TransactionRecordService(TransactionRecordRepository transactionRecordRepository) {
        this.transactionRecordRepository = transactionRecordRepository;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public TransactionRecord saveTransactionRecord(TransactionRecord transactionRecord) {
        return transactionRecordRepository.save(transactionRecord);
    }

    public TransactionRecord getTransactionsById(long id) {
        return transactionRecordRepository.findById(id).orElse(null);
    }

    public Page<TransactionRecord> getTransactionRecordsByQuery(String customerId, List<String> accountNumbers,
                                                                String description, int page, int size) {

        Pageable sortedByAccountNumber =
                PageRequest.of(page, size, Sort.by("accountNumber").ascending());
        return transactionRecordRepository.findByCustomerIdOrAccountNumberInOrDescriptionContainingIgnoreCase(customerId,
                accountNumbers, description, sortedByAccountNumber);
    }

    public Page<TransactionRecord> getTransactionRecords(int page, int size) {
        return transactionRecordRepository.findAll(PageRequest.of(page, size));
    }

    public void saveTransactionsByBatches(List<TransactionRecord> transactionRecordList) {
        transactionRecordRepository.saveAll(transactionRecordList);
    }
}
