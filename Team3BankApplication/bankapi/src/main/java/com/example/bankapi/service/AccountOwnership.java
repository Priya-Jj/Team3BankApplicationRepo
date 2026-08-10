package com.example.bankapi.service;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("accountOwnership")
public class AccountOwnership {

    private final AccountService accountService;

    // Constructor injection. AccountService and AccountOwnership refer to each other
    // only through this constructor -- Spring resolves the cycle at startup.
    public AccountOwnership(AccountService accountService) {
        this.accountService = accountService;
    }

    /**
     * Returns true if the given account exists and its customerId equals the
     * authenticated principal's name (which, by Lab 2-1's design, is the
     * customer ID from the sub claim).
     *
     * Called from @PreAuthorize expressions like:
     *   @PreAuthorize("@accountOwnership.isOwner(#accountId, authentication)")
     */
    public boolean isOwner(String accountId, Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return false;
        }
        // TODO 17: Look up the account using accountService.findById(accountId).
        //          Return true if the account exists AND its customerId
        //          equals authentication.getName().
        //          Note: calling findById here will itself trigger its own
        //          @PreAuthorize -- which requires SCOPE_account.read.
        //          Auditors and tellers and the owner all have account.read,
        //          so this works for every caller who legitimately reaches this point.
        return accountService.findById(accountId).map(account -> account.customerId().equals(authentication.getName())).orElse(false);

    }
}