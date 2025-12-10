package com.shivaansh.service;

import com.shivaansh.entity.Transaction;
import com.shivaansh.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public Page<Transaction> getAllTransactions(String type,
                                                String category,
                                                int page,
                                                int size,
                                                String sortBy,
                                                String direction) {
        log.debug("Fetching transactions with filters: type={}, category={}, page={}, size={}, sortBy={}, direction={}",
                type, category, page, size, sortBy, direction);

        // default sort
        if (sortBy == null || sortBy.isBlank()) {
            sortBy = "date";
        }
        if (direction == null || direction.isBlank()) {
            direction = "desc";
        }

        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        if (type != null && !type.isBlank() && category != null && !category.isBlank()) {
            return transactionRepository.findByTypeAndCategory(type, category, pageable);
        } else if (type != null && !type.isBlank()) {
            return transactionRepository.findByType(type, pageable);
        } else if (category != null && !category.isBlank()) {
            return transactionRepository.findByCategory(category, pageable);
        } else {
            return transactionRepository.findAll(pageable);
        }
    }

    public Transaction getTransactionById(Long id) {
        log.debug("Fetching transaction by ID: {}", id);
        return transactionRepository.findById(id).orElse(null);
    }

    public Transaction saveTransaction(Transaction transaction) {
        log.info("Saving transaction: {}", transaction);
        return transactionRepository.save(transaction);
    }

    public Transaction updateTransaction(Long id, Transaction transactionDetails) {
        log.info("Updating transaction with ID: {}", id);

        Optional<Transaction> existingTransaction = transactionRepository.findById(id);

        if (existingTransaction.isPresent()) {
            Transaction transaction = existingTransaction.get();

            if (transactionDetails.getDate() != null) {
                transaction.setDate(transactionDetails.getDate());
            }
            if (transactionDetails.getDescription() != null) {
                transaction.setDescription(transactionDetails.getDescription());
            }
            if (transactionDetails.getCategory() != null) {
                transaction.setCategory(transactionDetails.getCategory());
            }
            if (transactionDetails.getAmount() != null) {
                transaction.setAmount(transactionDetails.getAmount());
            }
            if (transactionDetails.getType() != null) {
                transaction.setType(transactionDetails.getType());
            }

            return transactionRepository.save(transaction);
        }

        return null;
    }

    public void deleteTransaction(Long id) {
        log.info("Deleting transaction with ID: {}", id);
        transactionRepository.deleteById(id);
    }
}
