package com.example.bankbff.dto;

public record TransferResponseDto(
        String transactionId,
        String status
) {}