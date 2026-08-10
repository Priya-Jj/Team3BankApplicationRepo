package com.example.bankapi.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import java.time.Instant;

@Service
public class AuditService {

    public void logEvent(String action, String resourceId) {
        // TODO 8: Retrieve the Authentication from the SecurityContextHolder.
        // Use SecurityContextHolder.getContext().getAuthentication()
        // Then use pattern matching to check if the principal is a Jwt:
        //   if (auth != null && auth.getPrincipal() instanceof Jwt jwt) { ... }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String subject = "anonymous";

        if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
            // TODO 9: Assign the subject from jwt.getSubject() to the subject variable.
            // Remember: this will be a customer ID (C001), employee ID (EM01),
            // or auditor ID (AUD01) -- never a login name like "alice".
            subject = jwt.getSubject();
        }

        System.out.printf("[AUDIT] %s | action=%s | resource=%s | caller=%s%n",
                Instant.now(), action, resourceId, subject);
    }
}