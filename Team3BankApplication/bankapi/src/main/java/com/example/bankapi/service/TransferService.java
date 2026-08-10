package com.example.bankapi.service;

import com.example.bankapi.model.Account;
import com.example.bankapi.model.TransactionStatus;
import com.example.bankapi.model.TransferRequest;
import com.example.bankapi.model.TransferResponse;
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

    // Same account IDs as AccountController's static list, but kept here
    // mutably so transfers can change balances. The two lists drift; in
    // a real app there would be a single source of truth (the database).
    private final List<Account> accounts = new ArrayList<>(List.of(
            new Account("A001", "C001", "CHECKING", new BigDecimal("1250.00")),
            new Account("A002", "C001", "SAVINGS",  new BigDecimal("8400.00")),
            new Account("A003", "C002", "CHECKING", new BigDecimal("300.50")),
            new Account("A004", "C003", "CHECKING", new BigDecimal("2100.75")),
            new Account("A005", "C003", "SAVINGS",  new BigDecimal("15000.00"))
    ));

    private final ReentrantLock lock = new ReentrantLock();

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

            if (request.amount().compareTo(from.balance()) > 0) {
                return new TransferResponse(null, TransactionStatus.FAILED);
            }

            accounts.set(fromIndex, new Account(
                    from.id(), from.customerId(), from.accountType(),
                    from.balance().subtract(request.amount())));
            accounts.set(toIndex, new Account(
                    to.id(), to.customerId(), to.accountType(),
                    to.balance().add(request.amount())));

            String txnId = "T-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            return new TransferResponse(txnId, TransactionStatus.COMPLETE);
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