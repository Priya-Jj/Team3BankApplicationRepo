package com.example.transconsumer.model;

import java.math.BigDecimal;
import java.time.Instant;

public record Transaction(
        String id,
        String accountId,
        BigDecimal amount,
        TransactionType type,
        Instant timestamp
) {}