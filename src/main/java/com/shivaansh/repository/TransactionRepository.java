package com.shivaansh.repository;

import com.shivaansh.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // Existing methods
    List<Transaction> findByDateBetween(LocalDate start, LocalDate end);
    List<Transaction> findByType(String type);
    List<Transaction> findByCategory(String category);

    // New paged methods
    Page<Transaction> findByType(String type, Pageable pageable);
    Page<Transaction> findByCategory(String category, Pageable pageable);
    Page<Transaction> findByTypeAndCategory(String type, String category, Pageable pageable);
}
