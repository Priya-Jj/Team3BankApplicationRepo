package com.example.bankapi.model;

import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record WithdrawalRequest(
        @Positive BigDecimal amount
) {}