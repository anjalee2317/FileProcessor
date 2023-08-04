package com.example.recordprocessor.controller;

import com.example.recordprocessor.model.TransactionRecord;
import com.example.recordprocessor.service.TransactionRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

@Log4j2
@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class TransactionRecordController {

    private final TransactionRecordService transactionRecordService;

    @PostMapping("/process")
    public ResponseEntity<Void> processFile(@RequestBody String filePath) {

        int BATCH_SIZE = 20;
        try (Stream<String> lines = Files.lines(Paths.get(filePath))) {
            List<TransactionRecord> transactions = lines.skip(1) // Skip the first row (header) of the file
                    .map(line -> Arrays.asList(line.split("\\|")))
                    .map(data -> {
                        // Check if data has at least 6 elements to create a TransactionRecord
                        if (data.size() >= 6) {
                            return TransactionRecord.builder()
                                    .accountNumber(data.get(0))
                                    .transactionAmount(data.get(1))
                                    .description(data.get(2))
                                    .transactionDate(data.get(3))
                                    .transactionTime(data.get(4))
                                    .customerId(data.get(5))
                                    .build();
                        } else {
                            log.error("Data does not have enough elements to create a TransactionRecord object: {}", data);
                            throw new IllegalArgumentException("Data does not have enough elements to create a TransactionRecord object");
                        }
                    })
                    .toList();

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
            log.info("Successfully processed the file and saved to database");
        } catch (IOException e) {
            log.error("An unexpected exception occurred during file reading ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            log.error("An unexpected exception occurred during file processing", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/retrieve")
    public ResponseEntity<Page<TransactionRecord>> retrieveTransactionRecords(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        try {
            Page<TransactionRecord> records = transactionRecordService.getTransactionRecords(page, size);

            if (records.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            } else {
                return new ResponseEntity<>(records, HttpStatus.OK);
            }
        } catch (Exception e) {
            log.error("An unexpected exception occurred while retrieving records", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<Page<TransactionRecord>> searchTransactionRecords(@RequestParam(required = false) String customerId, @RequestParam(required = false) List<String> accountNumbers,
                                                                            @RequestParam(required = false) String description, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        try {
            // Fetch records based on the given parameters
            Page<TransactionRecord> records = transactionRecordService.getTransactionRecordsByQuery(
                    customerId, accountNumbers, description, page, size);

            if (records.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            } else {
                return new ResponseEntity<>(records, HttpStatus.OK);
            }
        } catch (Exception e) {
            log.error("An unexpected exception occurred while searching records", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @PatchMapping("/update")
    public ResponseEntity<Void> updateTransactionRecords(@RequestBody List<TransactionRecord> updatedDataRecords) {

        try {
            ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

            for (TransactionRecord updatedRecord : updatedDataRecords) {
                threadPool.submit(() -> {
                    TransactionRecord existingTransaction = transactionRecordService.getTransactionsById(updatedRecord.getId());
                    if (existingTransaction != null) {
                        existingTransaction.setDescription(updatedRecord.getDescription());
                        transactionRecordService.saveTransactionRecord(existingTransaction);
                    }
                });
            }
            threadPool.shutdown();
        } catch (Exception e) {
            log.error("An unexpected exception occurred while updating Transaction Records ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        return ResponseEntity.ok().build();
    }
}