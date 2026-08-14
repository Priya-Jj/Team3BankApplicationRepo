package com.example.bankapi.controller;

import com.example.bankapi.dto.AccountAuditDto;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {
    private final AccountService accountService;
    private final AuditService auditService;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionStatsPublisher statsPublisher;

    public AccountController(AccountService accountService, AuditService auditService, DownstreamAccountService downstreamAccountService, TransferService transferService, AccountRepository accountRepository,
            TransactionRepository transactionRepository, TransactionStatsPublisher statsPublisher) {
        this.accountService = accountService;
        this.auditService = auditService;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.statsPublisher = statsPublisher;
    }

    @GetMapping
    public List<AccountsDto> getAll() {
        return accountService.findAll();
    }

    @GetMapping("/audits")
    public List<AccountAuditDto> getAudits() {
        return auditService.findAll();
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

    @PostMapping("/{accountId}/deposits")
    public ResponseEntity<Map<String, String>> deposit(
            @PathVariable String accountId,
            @Valid @RequestBody DepositRequest request) {
        return accountService.deposit(accountId, request);
    }

    @PutMapping("/{accountId}/status")
    public ResponseEntity<?> updateAccountStatus(
            @PathVariable Long accountId,
            @RequestBody StatusUpdateRequest request) {
        return accountService.updateAccountStatus(accountId, request);
    }

    @PostMapping("/{accountId}/withdrawals")
    public ResponseEntity<Map<String, String>> withdraw(
            @PathVariable Long accountId,
            @Valid @RequestBody WithdrawalRequest request) {
        return accountService.withdraw(accountId, request);
    }
}
