package com.example.bankapi.entity;

import java.math.BigDecimal;

public record TransactionStat(String type, BigDecimal amount) {
}

