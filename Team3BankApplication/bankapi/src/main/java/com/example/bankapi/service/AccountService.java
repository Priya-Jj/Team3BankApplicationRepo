package com.example.bankapi.service;

import com.example.bankapi.dto.AccountsDto;
import com.example.bankapi.model.Account;
import com.example.bankapi.repository.AccountRepository;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final Map<String, Account> store = new ConcurrentHashMap<>();

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
        store.put("A001", new Account("A001", "C001", "CHECKING", new BigDecimal("1250.00")));
        store.put("A002", new Account("A002", "C001", "SAVINGS",  new BigDecimal("8400.00")));
        store.put("A003", new Account("A003", "C002", "CHECKING", new BigDecimal("300.50")));
        store.put("A004", new Account("A004", "C003", "CHECKING", new BigDecimal("2100.75")));
        store.put("A005", new Account("A005", "C003", "SAVINGS",  new BigDecimal("15000.00")));
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
    public List<AccountsDto> findAllInternal() {
        return this.findAll(); // proxy not involved -- @PreAuthorize does not run
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