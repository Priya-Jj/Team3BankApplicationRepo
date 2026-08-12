package com.example.bankapi.service;

import com.example.bankapi.model.Account;
import com.example.bankapi.model.TransactionStatus;
import com.example.bankapi.model.TransferRequest;
import com.example.bankapi.model.TransferResponse;
import com.example.bankapi.model.CashTransactionRequest;
import com.example.bankapi.model.CashTransactionResponse;
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

    // Same account IDs as AccountController's static list, but kept here
    // mutably so transfers can change balances. The two lists drift; in
    // a real app there would be a single source of truth (the database).
    private final List<Account> accounts = new ArrayList<>(List.of(
            new Account("A001", "487-978493", "CHECKING", new BigDecimal("1250.00"), "ACTIVE"),
            new Account("A002", "487-978493", "SAVINGS",  new BigDecimal("8400.00"), "ACTIVE"),
            new Account("A003", "487-978494", "CHECKING", new BigDecimal("300.50"), "ACTIVE"),
            new Account("A004", "487-978495", "CHECKING", new BigDecimal("2100.75"), "ACTIVE"),
            new Account("A005", "487-978495", "SAVINGS",  new BigDecimal("15000.00"), "INACTIVE")
    ));

    private final ReentrantLock lock = new ReentrantLock();

    public TransferService(TransactionStatsPublisher statsPublisher) {
        this.statsPublisher = statsPublisher;
    }

    public List<Account> listAccounts() {
        lock.lock();
        try {
            return List.copyOf(accounts);
        } finally {
            lock.unlock();
        }
    }

    public TransferResponse transfer(TransferRequest request) {
        lock.lock();
        try {
            int fromIndex = indexOf(request.fromAccountId());
            int toIndex   = indexOf(request.toAccountId());

            if (fromIndex == -1 || toIndex == -1) {
                return new TransferResponse(null, TransactionStatus.FAILED);
            }

            Account from = accounts.get(fromIndex);
            Account to   = accounts.get(toIndex);

            if (!from.customerId().equals(to.customerId())) {
                return new TransferResponse(null, TransactionStatus.FAILED);
            }

            if (!"ACTIVE".equalsIgnoreCase(from.status()) || !"ACTIVE".equalsIgnoreCase(to.status())) {
                return new TransferResponse(null, TransactionStatus.FAILED);
            }

            if (request.amount().compareTo(from.balance()) > 0) {
                return new TransferResponse(null, TransactionStatus.FAILED);
            }

            accounts.set(fromIndex, new Account(
                    from.id(), from.customerId(), from.accountType(),
                    from.balance().subtract(request.amount()), from.status()));
            accounts.set(toIndex, new Account(
                    to.id(), to.customerId(), to.accountType(),
                    to.balance().add(request.amount()), to.status()));

            // Publish anonymized statistics for each leg of the internal transfer.
            try {
                statsPublisher.publish("TRANSFER_OUT", request.amount());
                statsPublisher.publish("TRANSFER_IN", request.amount());
            } catch (Exception ex) {
                // Ensure the transfer succeeds even if Kafka is temporarily unavailable.
                System.err.println("Failed to publish transaction stats: " + ex.getMessage());
            }

            String txnId = "T-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            return new TransferResponse(txnId, TransactionStatus.COMPLETE);
        } finally {
            lock.unlock();
        }
    }

    public CashTransactionResponse recordCashTransaction(String accountId, String transactionType, BigDecimal amount) {
        lock.lock();
        try {
            int accountIndex = indexOf(accountId);
            if (accountIndex == -1) {
                return new CashTransactionResponse(null, TransactionStatus.FAILED);
            }

            Account account = accounts.get(accountIndex);
            if (!"ACTIVE".equalsIgnoreCase(account.status())) {
                return new CashTransactionResponse(null, TransactionStatus.FAILED);
            }

            if ("WITHDRAWAL".equalsIgnoreCase(transactionType)) {
                if (amount.compareTo(account.balance()) > 0) {
                    return new CashTransactionResponse(null, TransactionStatus.FAILED);
                }
                accounts.set(accountIndex, new Account(
                        account.id(), account.customerId(), account.accountType(),
                        account.balance().subtract(amount), account.status()));
            } else if ("DEPOSIT".equalsIgnoreCase(transactionType)) {
                accounts.set(accountIndex, new Account(
                        account.id(), account.customerId(), account.accountType(),
                        account.balance().add(amount), account.status()));
            } else {
                return new CashTransactionResponse(null, TransactionStatus.FAILED);
            }

            try {
                statsPublisher.publish(transactionType.toUpperCase(), amount);
            } catch (Exception ex) {
                System.err.println("Failed to publish cash transaction stats: " + ex.getMessage());
            }

            String txnId = "CASH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            return new CashTransactionResponse(txnId, TransactionStatus.COMPLETE);
        } finally {
            lock.unlock();
        }
    }

    private int indexOf(String accountId) {
        for (int i = 0; i < accounts.size(); i++) {
            if (accounts.get(i).id().equals(accountId)) {
                return i;
            }
        }
        return -1;
    }
}