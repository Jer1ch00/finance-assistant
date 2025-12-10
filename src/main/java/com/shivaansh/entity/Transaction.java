package com.shivaansh.entity;


import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "transactions")
@Data
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;
    private String description;
    private String category;
    private BigDecimal amount;
    private String type;

    @PrePersist
    public void prePersist() {
        if(this.date == null) {
            this.date=LocalDate.now();
        }
    }
}
