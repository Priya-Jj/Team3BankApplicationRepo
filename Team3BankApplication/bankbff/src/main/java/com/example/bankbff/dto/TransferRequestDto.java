package com.example.bankbff.dto;

import java.math.BigDecimal;

public record TransferRequestDto(
        String fromAccountId,
        String toAccountId,
        BigDecimal amount
) {}