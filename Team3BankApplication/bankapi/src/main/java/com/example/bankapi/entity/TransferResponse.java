package com.example.bankapi.entity;

public record TransferResponse(
        String transactionId,
        TransactionStatus status
) {}