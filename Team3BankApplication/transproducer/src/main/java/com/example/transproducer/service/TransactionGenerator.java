package com.example.transproducer.service;

import com.example.transproducer.model.Transaction;
import com.example.transproducer.model.TransactionType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class TransactionGenerator {

    private static final List<String> ACCOUNT_IDS = List.of(
            "A-001", "A-002", "A-003", "A-004", "A-005",
            "A-006", "A-007", "A-008", "A-009", "A-010"
    );

    private static final TransactionType[] TYPES = TransactionType.values();

    public Transaction generate() {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        String accountId = ACCOUNT_IDS.get(random.nextInt(ACCOUNT_IDS.size()));
        TransactionType type = TYPES[random.nextInt(TYPES.length)];
        BigDecimal amount = BigDecimal.valueOf(random.nextDouble(1.00, 500.00))
                .setScale(2, RoundingMode.HALF_UP);

        return new Transaction(
                UUID.randomUUID().toString(),
                accountId,
                amount,
                type,
                Instant.now()
        );
    }
}