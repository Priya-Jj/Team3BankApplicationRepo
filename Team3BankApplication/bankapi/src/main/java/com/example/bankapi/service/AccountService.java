package com.example.bankapi.service;

import com.example.bankapi.model.Account;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AccountService {

    private final Map<String, Account> store = new ConcurrentHashMap<>();

    public AccountService() {
        store.put("A001", new Account("A001", "C001", "CHECKING", new BigDecimal("1250.00")));
        store.put("A002", new Account("A002", "C001", "SAVINGS",  new BigDecimal("8400.00")));
        store.put("A003", new Account("A003", "C002", "CHECKING", new BigDecimal("300.50")));
        store.put("A004", new Account("A004", "C003", "CHECKING", new BigDecimal("2100.75")));
        store.put("A005", new Account("A005", "C003", "SAVINGS",  new BigDecimal("15000.00")));
    }

    // TODO 12: Add @PreAuthorize that restricts this to tellers and auditors only.
    //          An account holder should NEVER be able to list every account in the bank.
    //          Hint: "hasRole('TELLER') or hasRole('AUDITOR')"
    @PreAuthorize("hasRole('TELLER') or hasRole('AUDITOR')")
    public List<Account> findAll() {
        return new ArrayList<>(store.values());
    }

    // TODO 13: Add @PreAuthorize requiring the SCOPE_account.read authority.
    //          Then add @PostAuthorize so the returned account is only visible if:
    //            - the caller is a teller or auditor, OR
    //            - the account's customerId equals authentication.name
    //          Hint: "returnObject.isEmpty() or hasRole('TELLER') or hasRole('AUDITOR')
    //                 or returnObject.get().customerId() == authentication.name"
    //          @PostAuthorize sees the method's return value via 'returnObject'.
    @PreAuthorize("hasAuthority('SCOPE_account.read')")
    @PostAuthorize("returnObject.isEmpty() or hasRole('TELLER') or hasRole('AUDITOR') or returnObject.get().customerId() == authentication.name")
    public Optional<Account> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    // TODO 14: Add @PreAuthorize that allows callers to look up their own accounts
    //          by customer ID, and allows tellers and auditors to look up any customer's:
    //            "#customerId == authentication.name
    //             or hasRole('TELLER') or hasRole('AUDITOR')"
    //          The #customerId prefix references the method parameter directly.
    @PreAuthorize("#customerId == authentication.name or hasRole('TELLER') or hasRole('AUDITOR')")
    public List<Account> findByCustomerId(String customerId) {
        return store.values().stream()
                .filter(a -> a.customerId().equals(customerId))
                .toList();
    }

    // TODO 15: Add @PreAuthorize requiring BOTH:
    //            hasAuthority('SCOPE_account.create') AND hasRole('TELLER')
    //          Combine them with 'and' (or '&&' -- both work in SpEL).
    //          An auditor has account.read but no create scope and no teller role,
    //          so this denies them even though they can read everything.
    @PreAuthorize("hasAuthority('SCOPE_account.create') and hasRole('TELLER')")
    public Account create(Account account) {
        store.put(account.id(), account);
        return account;
    }

    // TODO 16: Add @PreAuthorize that allows the account's owner OR a teller to
    //          update an account. Auditors are NOT allowed to update.
    //          Hint: "hasAuthority('SCOPE_account.write')
    //                 and (hasRole('TELLER')
    //                      or @accountOwnership.isOwner(#account.id(), authentication))"
    //          You will create the @accountOwnership bean in Task 4.3.
    @PreAuthorize("hasAuthority('SCOPE_account.write') and (hasRole('TELLER') or @accountOwnership.isOwner(#account.id(), authentication))")
    public Account update(Account account) {
        store.put(account.id(), account);
        return account;
    }

    /**
     * Calls findAll() via 'this' -- which bypasses the AOP proxy.
     * The @PreAuthorize on findAll() is NOT enforced for this call.
     * This is the most common AOP proxy gotcha in Spring applications.
     */
    public List<Account> findAllInternal() {
        return this.findAll(); // proxy not involved -- @PreAuthorize does not run
    }
}