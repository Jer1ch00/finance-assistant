package com.shivaansh.service;

import com.shivaansh.entity.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class CsvParserService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public List<Transaction> parseTransactions(MultipartFile file) throws Exception {
        List<Transaction> transactions = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                     .withFirstRecordAsHeader()
                     .withIgnoreHeaderCase()
                     .withTrim())) {

            log.info("CSV Headers: {}", csvParser.getHeaderMap().keySet());

            for (CSVRecord record : csvParser) {
                try {
                    log.debug("Processing row: {}", record);

                    Transaction transaction = new Transaction();

                    // Parse date
                    String dateStr = record.get("date");
                    log.debug("Date string: {}", dateStr);
                    transaction.setDate(LocalDate.parse(dateStr, DATE_FORMATTER));

                    // Parse other fields
                    transaction.setDescription(record.get("description"));
                    transaction.setCategory(record.get("category"));
                    transaction.setAmount(new BigDecimal(record.get("amount")));
                    transaction.setType(record.get("type"));

                    transactions.add(transaction);
                    log.debug("Successfully parsed transaction: {}", transaction);

                } catch (Exception e) {
                    log.warn("Skipping invalid row [{}]: {}", record.getRecordNumber(), e.getMessage(), e);
                }
            }
        }

        log.info("Successfully parsed {} transactions from CSV", transactions.size());
        return transactions;
    }
}
