package com.example.bankapi.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record TransferRequest(
        @NotBlank String fromAccountId,
        @NotBlank String toAccountId,
        @Positive BigDecimal amount
) {}