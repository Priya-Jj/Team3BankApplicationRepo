package com.example.bankbff.dto;

import java.math.BigDecimal;

/**
 * Account as returned from bankapi.
 *
 * Field names match the JSON exactly: id, customerId, accountType, balance, status.
 */
public record AccountDto(
        String accountId,
        String accountNumber,
        String customerId,
        String accountType,
        BigDecimal balance,
        String accountStatus
) {}