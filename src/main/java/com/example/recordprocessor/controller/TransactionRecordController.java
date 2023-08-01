package com.example.recordprocessor.controller;

import com.example.recordprocessor.model.TransactionRecord;
import com.example.recordprocessor.service.TransactionRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api")
public class
TransactionRecordController {

    private final int BATCH_SIZE = 20;

    @Autowired
    private final TransactionRecordService transactionRecordService;

    public TransactionRecordController(TransactionRecordService transactionRecordService) {
        this.transactionRecordService = transactionRecordService;
    }

    @PostMapping("/save")
    private void processFile(@RequestBody String filePath) {

        try (Stream<String> lines = Files.lines(Paths.get(filePath))) {
            List<TransactionRecord> transactions = lines.skip(1) // Skip the first row (header) of the file
                    .map(line -> Arrays.asList(line.split("\\|"))).map(data -> TransactionRecord.builder()
                            .accountNumber(data.get(0)).transactionAmount(data.get(1)).description(data.get(2)).transactionDate(data.get(3)).transactionTime(data.get(4)).customerId(data.get(5)).build()).toList();

            List<TransactionRecord> batchList = new ArrayList<>();
            for (TransactionRecord transaction : transactions) {
                batchList.add(transaction);
                if (batchList.size() >= BATCH_SIZE) {
                    transactionRecordService.saveTransactionsByBatches(batchList);
                    batchList.clear();
                }
            }

            if (!batchList.isEmpty()) {
                transactionRecordService.saveTransactionsByBatches(batchList);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @GetMapping("/retrieve")
    public ResponseEntity<Page<TransactionRecord>> retrieveTransactionRecords(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        Page<TransactionRecord> records = transactionRecordService.getTransactionRecords(page, size);
        if (records.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(records, HttpStatus.OK);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<Page<TransactionRecord>> getTransactionRecords(@RequestParam(required = false) String customerId, @RequestParam(required = false) String accountNumber, @RequestParam(required = false) String description, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        // Fetch records based on the given parameters
        Page<TransactionRecord> records = transactionRecordService.getTransactionRecordsByQuery(customerId, accountNumber, description, page, size);

        if (records.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(records, HttpStatus.OK);
        }
    }


    @Async
    @PatchMapping("/update")
    public CompletableFuture<Void> updateTransactionRecords(@RequestBody List<TransactionRecord> updatedDataRecords) {
        for (TransactionRecord updatedRecord : updatedDataRecords) {
            processTransactionRecord(updatedRecord);
        }
        return CompletableFuture.completedFuture(null);
    }

    private void processTransactionRecord(TransactionRecord updatedRecord) {

        if (updatedRecord.getId() == null) {
            transactionRecordService.saveTransactions(updatedRecord);
        } else {
            TransactionRecord existingTransaction = transactionRecordService.getTransactionsById(updatedRecord.getId());

            if (existingTransaction != null) {
                existingTransaction.setDescription(updatedRecord.getDescription());
                transactionRecordService.saveTransactions(existingTransaction);
            }
        }
    }

    @PatchMapping("/update-data")
    public void updateData(@RequestBody List<TransactionRecord> updatedDataRecords) {

        ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        for (TransactionRecord updatedRecord : updatedDataRecords) {
            threadPool.submit(() -> {
                if (updatedRecord.getId() == null) {
                    transactionRecordService.saveTransactions(updatedRecord);
                } else {
                    TransactionRecord existingTransaction = transactionRecordService.getTransactionsById(updatedRecord.getId());

                    if (existingTransaction != null) {
                        existingTransaction.setDescription(updatedRecord.getDescription());
                        transactionRecordService.saveTransactions(existingTransaction);
                    }
                }
            });
        }
        threadPool.shutdown();
    }
}