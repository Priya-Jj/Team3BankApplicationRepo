package com.example.bankbff.dto;

import java.math.BigDecimal;

public record CashTransactionRequestDto(
        String transactionType,
        BigDecimal amount
) {}
