package com.example.bankapi.service;

import com.example.bankapi.dto.AccountsDto;
import com.example.bankapi.entity.AccountStatus;
import com.example.bankapi.entity.Accounts;
import com.example.bankapi.entity.TransactionStatus;
import com.example.bankapi.entity.TxnType;
import com.example.bankapi.model.DepositRequest;
import com.example.bankapi.model.StatusUpdateRequest;
import com.example.bankapi.model.WithdrawalRequest;
import com.example.bankapi.repository.AccountRepository;
import com.example.bankapi.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionStatsPublisher statsPublisher;

    public AccountService(AccountRepository accountRepository, TransactionRepository transactionRepository, TransactionStatsPublisher statsPublisher) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.statsPublisher = statsPublisher;
    }

    // @PreAuthorize that restricts this to tellers and auditors only.
    //          An account holder should NEVER be able to list every account in the bank.
    //          Hint: "hasRole('TELLER') or hasRole('AUDITOR')"
  @PreAuthorize("hasRole('TELLER') or hasRole('AUDITOR')")
    public List<AccountsDto> findAll() {
        return accountRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    // @PreAuthorize requiring the SCOPE_account.read authority.
    //          Then add @PostAuthorize so the returned account is only visible if:
    //            - the caller is a teller or auditor, OR
    //            - the account's customerId equals authentication.name
    //          Hint: "returnObject.isEmpty() or hasRole('TELLER') or hasRole('AUDITOR')
    //                 or returnObject.get().customerId() == authentication.name"
    //          @PostAuthorize sees the method's return value via 'returnObject'.
    @PreAuthorize("hasAuthority('SCOPE_account.read')")
    @PostAuthorize("hasRole('TELLER') or hasRole('AUDITOR') or returnObject.customerId.toString() == authentication.name")
    public AccountsDto findById(Long id) {

        return accountRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new NoSuchElementException("Account not found: " + id));
    }

    // TODO 14: Add @PreAuthorize that allows callers to look up their own accounts
    //          by customer ID, and allows tellers and auditors to look up any customer's:
    //            "#customerNumber == authentication.name
    //             or hasRole('TELLER') or hasRole('AUDITOR')"
    //          The #customerNumber prefix references the method parameter directly.
    @PreAuthorize("#customerNumber == authentication.name or hasRole('TELLER') or hasRole('AUDITOR')")
    public List<AccountsDto> getByCustomerNumber(String customerNumber) {
        return accountRepository.findAccountsByCustomerNumber(customerNumber).stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * Calls findAll() via 'this' -- which bypasses the AOP proxy.
     * The @PreAuthorize on findAll() is NOT enforced for this call.
     * This is the most common AOP proxy gotcha in Spring applications.
     */
    public List<AccountsDto> findAllInternal() {
        return this.findAll(); // proxy not involved -- @PreAuthorize does not run
    }

    @PreAuthorize("hasAuthority('SCOPE_account.write') and hasRole('TELLER')")
    @Transactional
    public ResponseEntity<Map<String, String>> deposit(
            String accountId,
            DepositRequest request) {

        // Support both numeric DB id and accountNumber string
        var accountOpt = java.util.Optional.<Accounts>empty();
        try {
            // try numeric id
            Long id = Long.parseLong(accountId);
            accountOpt = accountRepository.findById(id);
        } catch (NumberFormatException ex) {
            // not numeric, try account number
            accountOpt = accountRepository.findByAccountNumber(accountId);
        }

        if (accountOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("status", "FAILED", "message", "Account not found"));
        }

        Accounts account = accountOpt.get();
        if (account.getAccountStatus() != AccountStatus.ACTIVE) {
            return ResponseEntity.status(422)
                    .body(Map.of("status", "FAILED", "message", "Account is inactive"));
        }

        BigDecimal currentBalance = account.getBalance();
        if (currentBalance == null) {
            currentBalance = BigDecimal.ZERO;
        }
        account.setBalance(currentBalance.add(request.amount()));
        Accounts saved = accountRepository.save(account);

        com.example.bankapi.entity.Transaction transaction = new com.example.bankapi.entity.Transaction();
        String txnId = "D-" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        transaction.setId(txnId);
        transaction.setAccount(saved);
        transaction.setTxnType(TxnType.DEPOSIT);
        transaction.setAmount(request.amount());
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setTxnDate(java.time.LocalDateTime.now());
        transaction.setDescription("Teller deposit");
        transactionRepository.save(transaction);
        try {
            statsPublisher.publish("DEPOSIT", request.amount());
            statsPublisher.publish("BALANCE", currentBalance.add(request.amount()));
        } catch (Exception e) {
            // don't block the API if stats publishing fails; just log
            System.err.println("Failed to publish transaction stats: " + e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("txnId", transaction.getId(), "status", "COMPLETED"));
    }

    @PreAuthorize("hasAuthority('SCOPE_account.write') and hasRole('TELLER')")
    @Transactional
    public ResponseEntity<?> updateAccountStatus(
            Long accountId,
            StatusUpdateRequest request) {

        var accountOpt = accountRepository.findById(accountId);
        if (accountOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("status", "FAILED", "message", "Account not found"));
        }

        Accounts account = accountOpt.get();
        String statusStr = request.status();
        if (statusStr == null) {
            return ResponseEntity.badRequest().body(Map.of("status", "FAILED", "message", "Missing status"));
        }
        try {
            AccountStatus newStatus = AccountStatus.valueOf(statusStr.toUpperCase());
            account.setAccountStatus(newStatus);
            Accounts saved = accountRepository.save(account);
            // return updated DTO
            return ResponseEntity.ok(findById(saved.getId()));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("status", "FAILED", "message", "Invalid status value"));
        }
    }

    @PreAuthorize("hasAuthority('SCOPE_account.write') and hasRole('TELLER')")
    @Transactional
    public ResponseEntity<Map<String, String>> withdraw(
            Long accountId,
            WithdrawalRequest request) {
        var accountOpt = accountRepository.findById(accountId);
        if (accountOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("status", "FAILED", "message", "Account not found"));
        }

        Accounts account = accountOpt.get();
        if (account.getAccountStatus() != AccountStatus.ACTIVE) {
            return ResponseEntity.status(422)
                    .body(Map.of("status", "FAILED", "message", "Account is inactive"));
        }

        BigDecimal currentBalance = account.getBalance();
        if (currentBalance == null) {
            currentBalance = BigDecimal.ZERO;
        }
        account.setBalance(currentBalance.subtract(request.amount()));
        Accounts saved = accountRepository.save(account);

        com.example.bankapi.entity.Transaction transaction = new com.example.bankapi.entity.Transaction();
        String txnId = "W-" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        transaction.setId(txnId);
        transaction.setAccount(saved);
        transaction.setTxnType(TxnType.WITHDRAWAL);
        transaction.setAmount(request.amount());
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setTxnDate(java.time.LocalDateTime.now());
        transaction.setDescription("Teller withdraw");
        transactionRepository.save(transaction);

        // publish transaction statistics to Kafka (same pattern as transfers)
        try {
            statsPublisher.publish("WITHDRAWN", request.amount());
            statsPublisher.publish("BALANCE", currentBalance.subtract(request.amount()));
        } catch (Exception e) {
            // don't block the API if stats publishing fails; just log
            System.err.println("Failed to publish transaction stats: " + e.getMessage());
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("txnId", transaction.getId(), "status", "COMPLETED"));
    }

    private AccountsDto toDto(com.example.bankapi.entity.Accounts account) {
        return new AccountsDto(
                account.getId(),
                account.getAccountNumber(),
                account.getCustomer().getId(),
                account.getAccountType().toString(),
                account.getAccountStatus().toString(),
                account.getBalance(),
                account.getOpenedDate());
    }
}