package com.example.bankapi.model;

import java.math.BigDecimal;
import java.time.Instant;

public record Transaction(
        String id,           // e.g. "T001"
        String accountId,    // FK to Account.id
        String type,         // "DEPOSIT", "WITHDRAWAL", "TRANSFER"
        BigDecimal amount,
        Instant timestamp
) {}
