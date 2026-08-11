package com.example.bankapi.controller;

import com.example.bankapi.model.Account;
import com.example.bankapi.service.AuditService;
import com.example.bankapi.service.DownstreamAccountService;
import com.example.bankapi.service.TransferService;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    private final AuditService auditService;
    private final DownstreamAccountService downstreamAccountService;
    private final TransferService transferService;

    public AccountController(AuditService auditService, DownstreamAccountService downstreamAccountService, TransferService transferService) {
        this.auditService = auditService;
        this.downstreamAccountService = downstreamAccountService;
        this.transferService = transferService;
    }

    @GetMapping
    public List<Account> getAll() {
        return transferService.listAccounts();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Account> getById(@PathVariable String id) {
        auditService.logEvent("READ_ACCOUNT", id);

        return transferService.listAccounts().stream()
                .filter(a -> a.id().equals(id))
                .findFirst()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // TODO 1: Add a POST endpoint that accepts an Account in the request body
    // and returns 201 Created with the account in the response body.
    // The endpoint does not need to persist the account -- this is a stub.
    // Annotate the parameter with @RequestBody.
    // Use ResponseEntity.status(HttpStatus.CREATED).body(account) as the return value.
    @PostMapping
    public ResponseEntity<Account> create(@RequestBody Account account) {
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
    @GetMapping("/mine")
    public List<Account> getMyAccounts(@AuthenticationPrincipal Jwt jwt) {
        // TODO 11: Read the caller's subject (customer_number or staff username)
        //          and roles list. If "teller" is in the roles, return
        //          ACCOUNTS in full. Otherwise filter to accounts where
        //          customerId equals the subject.

        String sub =jwt.getSubject();
        List<String> roles = jwt.getClaimAsStringList("roles");
        assert roles != null;
        if (roles.stream().anyMatch(role -> role.equals("teller"))) {
            return transferService.listAccounts();
        }
        else  {
            return transferService.listAccounts().stream().filter(account -> account.customerId().equals(sub)).collect(Collectors.toList());
        }
    }

    // TODO 24: Add this endpoint to AccountController.
// It is protected and requires an authenticated caller.
// The inbound request uses the caller's token.
// The outbound call to the downstream service uses the service's own token.
    @GetMapping("/downstream")
    public List<Account> getFromDownstream() {
        // TODO: call downstreamAccountService.fetchAllFromDownstream() and return the result
        return downstreamAccountService.fetchAllFromDownstream();
    }
}
