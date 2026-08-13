package com.example.bankapi.service;

import com.example.bankapi.entity.*;
import com.example.bankapi.repository.AccountRepository;
import com.example.bankapi.repository.TransferRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Transfer service.
 *
 * Holds the mutable account state and processes transfers atomically.
 * In a production application this state would live in a database with
 * proper transactional guarantees; an in-memory list with a ReentrantLock
 * is enough for the lab.
 */
@Service
public class TransferService {
    private final TransactionStatsPublisher statsPublisher;
    private final TransferRepository transferRepository;
    private final AccountRepository accountRepository;

    // Same account IDs as AccountController's static list, but kept here
    // mutably so transfers can change balances. The two lists drift; in
    // a real app there would be a single source of truth (the database).
    private final ReentrantLock lock = new ReentrantLock();

    public TransferService(TransactionStatsPublisher statsPublisher, TransferRepository transferRepository, AccountRepository accountRepository) {
        this.statsPublisher = statsPublisher;
        this.transferRepository = transferRepository;
        this.accountRepository = accountRepository;
    }

    public TransferResponse transfer(TransferRequest request) {
        lock.lock();
        try {
            String fromAccountId = request.fromAccountId();
            String toAccountId   = request.toAccountId();

            if (fromAccountId == null || toAccountId == null || fromAccountId.equals(toAccountId)) {
                return new TransferResponse(null, TransactionStatus.FAILED);
            }

            Accounts from = accountRepository.findById(Long.valueOf(fromAccountId)).orElse(null);
            Accounts to   = accountRepository.findById(Long.valueOf(toAccountId)).orElse(null);

            assert from != null;
            assert to != null;

            if (request.amount().compareTo(from.getBalance()) > 0) {
                return new TransferResponse(null, TransactionStatus.FAILED);
            }

            from.setBalance(from.getBalance().subtract(request.amount()));
            to.setBalance(to.getBalance().add(request.amount()));

            accountRepository.save(from);
            accountRepository.save(to);

//            if (!"ACTIVE".equalsIgnoreCase(from.status()) || !"ACTIVE".equalsIgnoreCase(to.status())) {
//                return new TransferResponse(null, TransactionStatus.FAILED);
//            }

//            if (request.amount().compareTo(from.balance()) > 0) {
//                return new TransferResponse(null, TransactionStatus.FAILED);
//            }

            // Publish anonymized statistics for each leg of the internal transfer.
            try {
                statsPublisher.publish("TRANSFER_OUT", request.amount());
                statsPublisher.publish("TRANSFER_IN", request.amount());
            } catch (Exception ex) {
                // Ensure the transfer succeeds even if Kafka is temporarily unavailable.
                System.err.println("Failed to publish transaction stats: " + ex.getMessage());
            }

            String txnId = "T-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            return new TransferResponse(txnId, TransactionStatus.COMPLETED);
        } finally {
            lock.unlock();
        }
    }
}