package com.example.recordprocessor.service;

import com.example.recordprocessor.model.TransactionRecord;
import com.example.recordprocessor.repository.TransactionRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransactionRecordService {

    private final TransactionRecordRepository transactionRecordRepository;

    @Autowired
    public TransactionRecordService(TransactionRecordRepository transactionRecordRepository) {
        this.transactionRecordRepository = transactionRecordRepository;
    }

    public TransactionRecord saveTransactions(TransactionRecord transactionRecord) {
        return transactionRecordRepository.save(transactionRecord);
    }

    public TransactionRecord getTransactionsById(long id) {
        return transactionRecordRepository.findById(id).orElse(null);
    }

    public Page<TransactionRecord> getTransactionRecordsByQuery(String customerId, String accountNumber,
                                                                String description, int page, int size) {

        return transactionRecordRepository.findByCustomerIdOrAccountNumberInOrDescriptionContainingIgnoreCase(customerId,
                accountNumber, description, PageRequest.of(page, size));
    }

    public Page<TransactionRecord> getTransactionRecords(int page, int size) {
        return transactionRecordRepository.findAll(PageRequest.of(page, size));
    }

    public void saveTransactionsByBatches(List<TransactionRecord> transactionRecordList) {
        transactionRecordRepository.saveAll(transactionRecordList);
    }
}
