package com.example.transconsumer.model;

import java.math.BigDecimal;

public record TransactionStatistic(
        String type,
        BigDecimal amount
) {}