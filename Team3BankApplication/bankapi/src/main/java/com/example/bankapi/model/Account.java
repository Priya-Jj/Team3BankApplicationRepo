package com.example.bankapi.model;

import java.math.BigDecimal;

public record Account(
        String id,           // e.g. "A001"
        String customerId,   // e.g. "C001" -- matches the customer's sub claim
        String accountType,  // "CHECKING", "SAVINGS"
        BigDecimal balance
) {}