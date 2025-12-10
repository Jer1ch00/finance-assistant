package com.shivaansh.controller;


import com.shivaansh.entity.Transaction;
import com.shivaansh.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins ="*")
public class TransactionController {
    private final TransactionService transactionService;

    @GetMapping
    public ResponseEntity<?> getAllTransactions() {
        try {
            log.info("GET /api/transactions");

            List<Transaction> transactions = transactionService.getAllTransactions();

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("count", transactions.size());
            response.put("transactions", transactions);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching transactions", e);
            return buildErrorResponse("Failed to fetch transactions", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTransactionById(@PathVariable Long id) {
        try {
            log.info("GET /api/transactions/{}", id);

            Transaction transaction = transactionService.getTransactionById(id);

            if (transaction == null) {
                log.warn("Transaction not found: {}", id);
                return buildErrorResponse("Transaction not found", HttpStatus.NOT_FOUND);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("transaction", transaction);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching transaction by ID: {}", id, e);
            return buildErrorResponse("Error fetching transaction", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping
    public ResponseEntity<?> createTransaction(@RequestBody Transaction transaction) {
        try {
            log.info("POST /api/transactions - Creating new transaction: {}", transaction);

            if (transaction.getDate() == null) {
                return buildErrorResponse("Date is required", HttpStatus.BAD_REQUEST);
            }

            if (transaction.getAmount() == null) {
                return buildErrorResponse("Amount is required", HttpStatus.BAD_REQUEST);
            }

            if (transaction.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                return buildErrorResponse("Amount must be greater than 0", HttpStatus.BAD_REQUEST);
            }

            if (transaction.getType() == null || transaction.getType().isEmpty()) {
                return buildErrorResponse("Type is required", HttpStatus.BAD_REQUEST);
            }

            if (!transaction.getType().matches("(?i)INCOME|EXPENSE")) {
                return buildErrorResponse("Type must be INCOME or EXPENSE", HttpStatus.BAD_REQUEST);
            }

            Transaction savedTransaction = transactionService.saveTransaction(transaction);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Transaction created successfully");
            response.put("transaction", savedTransaction);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error creating transaction", e);
            return buildErrorResponse("Error creating transaction: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTransaction(@PathVariable Long id, @RequestBody Transaction transactionDetails) {
        try {
            log.info("PUT /api/transactions/{} - Updating transaction: {}", id, transactionDetails);

            Transaction existingTransaction = transactionService.getTransactionById(id);
            if (existingTransaction == null) {
                log.warn("Transaction not found for update: {}", id);
                return buildErrorResponse("Transaction not found", HttpStatus.NOT_FOUND);
            }

            if (transactionDetails.getAmount() != null &&
                    transactionDetails.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                return buildErrorResponse("Amount must be greater than 0", HttpStatus.BAD_REQUEST);
            }

            if (transactionDetails.getType() != null && !transactionDetails.getType().isEmpty()) {
                if (!transactionDetails.getType().matches("(?i)INCOME|EXPENSE")) {
                    return buildErrorResponse("Type must be INCOME or EXPENSE", HttpStatus.BAD_REQUEST);
                }
            }

            Transaction updatedTransaction = transactionService.updateTransaction(id, transactionDetails);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Transaction updated successfully");
            response.put("transaction", updatedTransaction);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error updating transaction: {}", id, e);
            return buildErrorResponse("Error updating transaction: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTransaction(@PathVariable Long id) {
        try {
            log.info("DELETE /api/transactions/{}", id);

            Transaction existingTransaction = transactionService.getTransactionById(id);
            if (existingTransaction == null) {
                log.warn("Transaction not found for deletion: {}", id);
                return buildErrorResponse("Transaction not found", HttpStatus.NOT_FOUND);
            }

            transactionService.deleteTransaction(id);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Transaction deleted successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error deleting transaction: {}", id, e);
            return buildErrorResponse("Error deleting transaction: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private ResponseEntity<?> buildErrorResponse(String message, HttpStatus status) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", "error");
        errorResponse.put("message", message);
        return ResponseEntity.status(status).body(errorResponse);
    }
}
