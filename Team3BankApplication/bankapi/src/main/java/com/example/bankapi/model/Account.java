package com.example.bankapi.model;

import java.math.BigDecimal;

public record Account(
        String id,           // e.g. "A001"
        String customerId,   // e.g. "487-978493" -- matches the customer's sub claim
        String accountType,  // "CHECKING", "SAVINGS"
        BigDecimal balance,
        String status
) {
    public Account(String id, String customerId, String accountType, BigDecimal balance) {
        this(id, customerId, accountType, balance, "ACTIVE");
    }
}