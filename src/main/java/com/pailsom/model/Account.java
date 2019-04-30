package com.pailsom.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicReference;

public class Account {
    private final int id;
    private final String name;
    private final String type;
    private final AtomicReference<BigDecimal> amount;
    private final LocalDateTime creationDate;

    public Account(int id, AtomicReference<BigDecimal> amount,String name,String type) {
        this.id = id;
        this.amount = amount;
        this.name = name;
        this.type = type;
        this.creationDate = LocalDateTime.now();
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public AtomicReference<BigDecimal> getAmount() {
        return amount;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }
}
