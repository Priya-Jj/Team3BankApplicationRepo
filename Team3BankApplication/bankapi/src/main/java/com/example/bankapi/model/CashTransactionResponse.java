package com.example.bankapi.model;

public record CashTransactionResponse(
        String transactionId,
        TransactionStatus status
) {}
