package com.example.bankapi.model;

import java.math.BigDecimal;
public record TransactionStat(String type, BigDecimal amount) {
}
