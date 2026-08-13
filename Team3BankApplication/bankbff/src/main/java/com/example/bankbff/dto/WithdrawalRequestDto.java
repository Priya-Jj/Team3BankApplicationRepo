package com.example.bankbff.dto;

import java.math.BigDecimal;

public record WithdrawalRequestDto(
        BigDecimal amount
) {}
