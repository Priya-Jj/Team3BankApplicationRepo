package com.example.bankapi.service;

import com.example.bankapi.model.Account;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class AccountServiceSecurityTest {

    @Autowired
    private AccountService accountService;

    // @WithMockUser simulates an authenticated user in the SecurityContext.
    // "SCOPE_account.read" is the authority Spring Security derives from the
    // "account.read" scope in a JWT -- the SCOPE_ prefix is added automatically.
    // The "username" attribute becomes authentication.getName().
    // For our domain that means the customer ID (e.g. "C001"), employee ID, or
    // auditor ID -- because the Authorization Server puts the customer ID into sub.

    @Test
    @WithMockUser(username = "EM01",
            authorities = {"SCOPE_account.read", "ROLE_TELLER"})
    void findAll_asTeller_returnsAllAccounts() {
        var result = accountService.findAll();
        assertThat(result).hasSize(5);
    }

    @Test
    @WithMockUser(username = "C001",
            authorities = {"SCOPE_account.read", "ROLE_ACCOUNT_HOLDER"})
    void findAll_asAccountHolder_throwsAccessDeniedException() {
        // TODO 18: Assert that calling accountService.findAll() throws AccessDeniedException.
        //          Account holders should never be able to list every account in the bank.
        assertThatThrownBy(() -> accountService.findAll())
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @WithMockUser(username = "C001",
            authorities = {"SCOPE_account.read", "ROLE_ACCOUNT_HOLDER"})
    void findById_asOwner_returnsAccount() {
        // alice (C001) owns A001 -- @PostAuthorize allows the return value through.
        var result = accountService.findById("A001");
        assertThat(result).isPresent();
        assertThat(result.get().customerId()).isEqualTo("C001");
    }

    @Test
    @WithMockUser(username = "C001",
            authorities = {"SCOPE_account.read", "ROLE_ACCOUNT_HOLDER"})
    void findById_asNonOwner_throwsAccessDeniedException() {
        // alice (C001) trying to look up A003, which is owned by C002 (bob).
        // The method body runs, returns the account, then @PostAuthorize denies it.
        assertThatThrownBy(() -> accountService.findById("A003"))
                .isInstanceOf(AccessDeniedException.class);
    }

    // TODO 19: Write a test verifying create() succeeds when the caller has BOTH
    //          SCOPE_account.create AND ROLE_TELLER.
    //          @WithMockUser(username = "EM01",
    //                        authorities = {"SCOPE_account.create", "ROLE_TELLER"})
    @Test
    @WithMockUser(username = "EM01",
            authorities = {"SCOPE_account.create", "ROLE_TELLER"})
    void create_asTellerWithCreateScope_succeeds() {
        // TODO: Build a new Account and call create(). Assert the return value is not null.

        assertThat(accountService.create(new Account("1","A006", "CHECKING", new BigDecimal("1000.00")))).isNotNull();
    }

    // TODO 20: Write a test verifying create() throws AccessDeniedException when the
    //          caller is an auditor (has read scopes but no create scope, no teller role).
    @Test
    @WithMockUser(username = "AUD01",
            authorities = {"SCOPE_account.read", "ROLE_AUDITOR"})
    void create_asAuditor_throwsAccessDeniedException() {
        // TODO: Build a new Account and assert that create() throws AccessDeniedException.
        assertThatThrownBy(() -> accountService.create(new Account("1","A006", "CHECKING", new BigDecimal("1000.00"))))
                .isInstanceOf(AccessDeniedException.class);
    }

    // TODO 21: Write a test with NO @WithMockUser (unauthenticated context).
// Call accountService.findAllInternal() and assert that no exception is thrown
// and the result is not empty.
// Despite findAll() having @PreAuthorize requiring TELLER or AUDITOR,
// the annotation is not enforced because the call bypasses the proxy via self-invocation.
// Add a comment explaining: why this happens, what the correct solutions are,
// and what the production security risk is.
    @Test
    void findAllInternal_bypassesMethodSecurity() {
        // TODO: Assert the result is not empty and no exception is thrown.
        assertThatThrownBy(() -> accountService.findAllInternal())
                .doesNotThrowAnyException();
    }
}