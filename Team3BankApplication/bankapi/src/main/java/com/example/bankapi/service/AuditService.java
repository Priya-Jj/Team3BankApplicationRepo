package com.example.bankapi.service;

import com.example.bankapi.dto.AccountAuditDto;
import com.example.bankapi.dto.AccountsDto;
import com.example.bankapi.entity.AccountAudit;
import com.example.bankapi.repository.AccountAuditRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
public class AuditService {
    private final AccountAuditRepository accountAuditRepository;

    public AuditService(AccountAuditRepository accountAuditRepository) {
        this.accountAuditRepository = accountAuditRepository;
    }

    @PreAuthorize("hasRole('TELLER') or hasRole('AUDITOR')")
    public List<AccountAuditDto> findAll() {
        return accountAuditRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    public void logEvent(String action, String resourceId) {
        // TODO 8: Retrieve the Authentication from the SecurityContextHolder.
        // Use SecurityContextHolder.getContext().getAuthentication()
        // Then use pattern matching to check if the principal is a Jwt:
        //   if (auth != null && auth.getPrincipal() instanceof Jwt jwt) { ... }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String subject = "anonymous";

        if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
            // TODO 9: Assign the subject from jwt.getSubject() to the subject variable.
            // Remember: this will be a customer_number (487-978493) or staff username (teller1)
            // -- never a login name like the old format.
            subject = jwt.getSubject();
        }

        System.out.printf("[AUDIT] %s | action=%s | resource=%s | caller=%s%n",
                Instant.now(), action, resourceId, subject);
    }

    private AccountAuditDto toDto(com.example.bankapi.entity.AccountAudit accountAudit) {
        return new AccountAuditDto(
                accountAudit.getId(),
                accountAudit.getAccountId(),
                accountAudit.getOldBalance(),
                accountAudit.getNewBalance(),
                accountAudit.getChangedAt(),
                accountAudit.getAuditType()
        );
    }
}