package com.example.bankapi.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record CashTransactionRequest(
        @NotBlank String transactionType,
        @Positive BigDecimal amount
) {}
