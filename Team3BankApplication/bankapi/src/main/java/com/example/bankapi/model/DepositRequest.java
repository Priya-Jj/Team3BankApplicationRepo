package com.example.bankapi.model;

import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record DepositRequest(
        @Positive BigDecimal amount
) {}
