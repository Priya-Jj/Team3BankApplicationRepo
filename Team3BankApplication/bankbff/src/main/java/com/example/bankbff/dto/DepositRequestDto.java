package com.example.bankbff.dto;

import java.math.BigDecimal;

public record DepositRequestDto(
        BigDecimal amount
) {}