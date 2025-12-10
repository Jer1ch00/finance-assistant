package com.shivaansh.service;


import com.shivaansh.entity.Transaction;
import com.shivaansh.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public List<Transaction> getAllTransactions() {
        log.debug("Fetching all transactions");
        return transactionRepository.findAll();
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
