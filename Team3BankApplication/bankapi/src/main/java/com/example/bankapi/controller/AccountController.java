package com.example.bankapi.controller;

import com.example.bankapi.dto.AccountsDto;
import com.example.bankapi.model.*;
import com.example.bankapi.entity.TransactionStatus;
import com.example.bankapi.model.DepositRequest;
import com.example.bankapi.service.AccountService;
import com.example.bankapi.service.AuditService;
import com.example.bankapi.service.DownstreamAccountService;
import com.example.bankapi.service.TransferService;
import com.example.bankapi.service.TransactionStatsPublisher;
import com.example.bankapi.entity.Accounts;
import com.example.bankapi.entity.AccountStatus;
import com.example.bankapi.entity.TxnType;
import com.example.bankapi.repository.AccountRepository;
import com.example.bankapi.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import javax.security.auth.Subject;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {
    private final AccountService accountService;
    private final AuditService auditService;
    private final DownstreamAccountService downstreamAccountService;
    private final TransferService transferService;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionStatsPublisher statsPublisher;

    public AccountController(AccountService accountService, AuditService auditService, DownstreamAccountService downstreamAccountService, TransferService transferService, AccountRepository accountRepository,
            TransactionRepository transactionRepository, TransactionStatsPublisher statsPublisher) {
        this.accountService = accountService;
        this.auditService = auditService;
        this.downstreamAccountService = downstreamAccountService;
        this.transferService = transferService;
         this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.statsPublisher = statsPublisher;
    }

    @GetMapping
    public List<AccountsDto> getAll() {
        return accountService.findAll();
    }

    @GetMapping("/{customerNumber}")
    public List<AccountsDto> getByCustomerNumber(@PathVariable String customerNumber) {
        return accountService.getByCustomerNumber(customerNumber);
    }

    // TODO 1: Add a POST endpoint that accepts an Account in the request body
    // and returns 201 Created with the account in the response body.
    // The endpoint does not need to persist the account -- this is a stub.
    // Annotate the parameter with @RequestBody.
    // Use ResponseEntity.status(HttpStatus.CREATED).body(account) as the return value.
    @PostMapping
    public ResponseEntity<AccountsDto> create(@RequestBody AccountsDto account) {
        return ResponseEntity.status(HttpStatus.CREATED).body(account);
    }

    // TODO 6: Complete this endpoint.
// @AuthenticationPrincipal instructs Spring Security to inject the validated Jwt
// from the SecurityContext directly as a method parameter.
// This is cleaner than calling SecurityContextHolder.getContext() manually.
    @GetMapping("/me")
    public Map<String, Object> getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        // TODO 7: Return a Map containing:
        //   "subject"            -- jwt.getSubject()
        //   "issuer"             -- jwt.getIssuer().toString()
        //   "scopes"             -- jwt.getClaimAsString("scope")
        //   "tokenExpiry"        -- jwt.getExpiresAt().toString()
        //   "roles"              -- jwt.getClaimAsStringList("roles"), or an empty list if null
        //   "preferredUsername"  -- jwt.getClaimAsString("preferred_username"), or "not present"
        //   "fullName"           -- jwt.getClaimAsString("name"), or "not present"
        //
        // The service token will NOT have "roles", "preferred_username", or "name"
        // because the token customizer in the Authorization Server only adds those
        // for user-context tokens. This difference is the main thing to observe.
        assert jwt.getExpiresAt() != null;
        return Map.of("subject", Objects.requireNonNull(jwt.getSubject()),
                "issuer", Objects.requireNonNull(jwt.getIssuer()).toString(),
                "scopes", Objects.requireNonNull(jwt.getClaimAsString("scope")),
                "tokenExpiry", Objects.requireNonNull(jwt.getExpiresAt()).toString(),
                "roles", jwt.getClaimAsStringList("roles") == null ? Collections.emptyList() : Objects.requireNonNull(jwt.getClaimAsStringList("roles")),
                "preferredUsername", jwt.getClaimAsString("preferred_username") == null ? "not present" : Objects.requireNonNull(jwt.getClaimAsString("preferred_username")),
                "fullName", jwt.getClaimAsString("name") == null ? "not present" : Objects.requireNonNull(jwt.getClaimAsString("name"))
        );
    }

    // TODO 10: Add this endpoint to AccountController.
// For a regular account holder it returns only accounts whose customerId
// matches their sub. For a teller it returns all accounts.
//
// Read jwt.getSubject() and jwt.getClaimAsStringList("roles").
// Filter ACCOUNTS by customerId for account holders.
// Return the full list for tellers.
//    @GetMapping("/mine")
//    public List<AccountsDto> getMyAccounts(@AuthenticationPrincipal Jwt jwt) {
//        // TODO 11: Read the caller's subject (customer_number or staff username)
//        //          and roles list. If "teller" is in the roles, return
//        //          ACCOUNTS in full. Otherwise filter to accounts where
//        //          customerId equals the subject.
//
//        String sub =jwt.getSubject();
//        List<String> roles = jwt.getClaimAsStringList("roles");
//        assert roles != null;
//        if (roles.stream().anyMatch(role -> role.equals("teller"))) {
//            return transferService.listAccounts();
//        }
//        else  {
//            return transferService.listAccounts().stream().filter(account -> account.customerId().equals(sub)).collect(Collectors.toList());
//        }
//    }

    // TODO 24: Add this endpoint to AccountController.
// It is protected and requires an authenticated caller.
// The inbound request uses the caller's token.
// The outbound call to the downstream service uses the service's own token.
    @GetMapping("/downstream")
    public List<AccountsDto> getFromDownstream() {
        // TODO: call downstreamAccountService.fetchAllFromDownstream() and return the result
        return downstreamAccountService.fetchAllFromDownstream();
    }

//    @PreAuthorize("hasAuthority('SCOPE_account.write') and hasRole('TELLER')")
//    @PostMapping("/{id}/transactions")
//    public ResponseEntity<CashTransactionResponse> recordCashTransaction(
//            @PathVariable String id,
//            @Valid @RequestBody CashTransactionRequest request) {
//        CashTransactionResponse response = transferService.recordCashTransaction(id, request.transactionType(), request.amount());
//        if (response.status() == TransactionStatus.COMPLETED) {
//            return ResponseEntity.status(HttpStatus.CREATED).body(response);
//        }
//        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
//    }

    @PreAuthorize("hasAuthority('SCOPE_account.write') and hasRole('TELLER')")
    @Transactional
    @PostMapping("/{accountId}/deposits")
    public ResponseEntity<Map<String, String>> deposit(
            @PathVariable String accountId,
            @Valid @RequestBody DepositRequest request) {
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
    @PutMapping("/{accountId}/status")
    public ResponseEntity<?> updateAccountStatus(
            @PathVariable Long accountId,
            @RequestBody StatusUpdateRequest request) {
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
            return ResponseEntity.ok(accountService.findById(saved.getId()));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("status", "FAILED", "message", "Invalid status value"));
        }
    }

    @PreAuthorize("hasAuthority('SCOPE_account.write') and hasRole('TELLER')")
    @Transactional
    @PostMapping("/{accountId}/withdrawals")
    public ResponseEntity<Map<String, String>> withdraw(
            @PathVariable Long accountId,
            @Valid @RequestBody WithdrawalRequest request) {
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
}
