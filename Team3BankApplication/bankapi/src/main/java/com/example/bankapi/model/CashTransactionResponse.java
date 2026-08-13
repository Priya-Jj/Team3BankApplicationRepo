package com.example.bankapi.model;

import com.example.bankapi.entity.TransactionStatus;

public record CashTransactionResponse(
        String transactionId,
        TransactionStatus status
) {}