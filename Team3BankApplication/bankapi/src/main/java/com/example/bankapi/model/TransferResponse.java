package com.example.bankapi.model;

public record TransferResponse(
        String transactionId,
        TransactionStatus status
) {}