package com.shivaansh.controller;

import com.shivaansh.entity.Transaction;
import com.shivaansh.repository.TransactionRepository;
import com.shivaansh.service.CsvParserService;
import org.springframework.http.ResponseEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins="*")
public class FileUploadController {
    private final CsvParserService csvParserService;
    private final TransactionRepository transactionRepository;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            log.info("Received file upload: {} ({} bytes)", file.getOriginalFilename(), file.getSize());
            List<Transaction> transactions = csvParserService.parseTransactions(file);

            transactionRepository.saveAll(transactions);
            log.info("Successfully processed {} transactions", transactions.size());

            return ResponseEntity.ok()
                    .body("{\"status\":\"success\",\"transactionsProcessed\":" + transactions.size() + "}");


        }  catch (Exception e) {
            log.error("File upload failed", e);
            return ResponseEntity.badRequest()
                    .body("{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Finance Assistant Healthy");
    }
}
