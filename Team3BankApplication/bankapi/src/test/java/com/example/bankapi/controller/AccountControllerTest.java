package com.example.bankapi.controller;

import com.example.bankapi.dto.AccountsDto;
import com.example.bankapi.service.AccountService;
import com.example.bankapi.service.AuditService;
import com.example.bankapi.service.DownstreamAccountService;
import com.example.bankapi.service.TransferService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for AccountController using mocks.
 * Tests focus on controller logic and HTTP response handling.
 */
@ExtendWith(MockitoExtension.class)
class AccountControllerTest {

    private AccountController accountController;

    @Mock
    private AccountService accountService;

    @Mock
    private AuditService auditService;

    @Mock
    private DownstreamAccountService downstreamAccountService;

    @Mock
    private TransferService transferService;

    @Mock
    private com.example.bankapi.repository.AccountRepository accountRepository;

    @Mock
    private com.example.bankapi.repository.TransactionRepository transactionRepository;

    @Mock
    private com.example.bankapi.service.TransactionStatsPublisher statsPublisher;

    @Mock
    private Jwt jwt;

    @BeforeEach
    void setUp() {
        accountController = new AccountController(
                accountService,
                auditService,
                downstreamAccountService,
                transferService,
                accountRepository,
                transactionRepository,
                statsPublisher
        );
    }

    // ===== GET /api/v1/accounts =====

    @Test
    void getByCustomerId_success() {
        // Given
        AccountsDto account1 = new AccountsDto(1L, "A001", 10L, "CHECKING", "ACTIVE",
                BigDecimal.valueOf(1250.00), LocalDateTime.now());
        AccountsDto account2 = new AccountsDto(2L, "A002", 10L, "SAVINGS", "ACTIVE",
                BigDecimal.valueOf(8400.00), LocalDateTime.now());

        when(accountService.getByCustomerNumber("1L"))
                .thenReturn(List.of(account1, account2));

        // When
        List<AccountsDto> result = accountController.getByCustomerNumber("1L");

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(account1, account2);
        verify(accountService).getByCustomerNumber("1L");
    }

    @Test
    void getByCustomerId_emptyList() {
        // Given
        when(accountService.getByCustomerNumber("1L"))
                .thenReturn(List.of());

        // When
        List<AccountsDto> result = accountController.getByCustomerNumber("1L");

        // Then
        assertThat(result).isEmpty();
    }

//    @Test
//    void getMyAccounts_asTeller_returnsAllAccounts() {
//        // Given
//        when(jwt.getSubject()).thenReturn("teller1");
//        when(jwt.getClaimAsStringList("roles")).thenReturn(List.of("teller"));
//
//        Account acc1 = new Account("A001", "487-978493", "CHECKING", BigDecimal.valueOf(1250.00));
//        Account acc2 = new Account("A002", "487-978494", "SAVINGS", BigDecimal.valueOf(8400.00));
//
//        when(transferService.listAccounts()).thenReturn(List.of(acc1, acc2));
//
//        // When
//        List<Account> result = accountController.getMyAccounts(jwt);
//
//        // Then
//        assertThat(result).hasSize(2);
//        assertThat(result).containsExactly(acc1, acc2);
//    }

//    @Test
//    void getMyAccounts_asCustomer_returnsOwnAccountsOnly() {
//        // Given
//        when(jwt.getSubject()).thenReturn("487-978493"); // customer ID
//        when(jwt.getClaimAsStringList("roles")).thenReturn(List.of("account_holder"));
//
//        Account ownAccount = new Account("A001", "487-978493", "CHECKING", BigDecimal.valueOf(1250.00));
//        Account otherAccount = new Account("A003", "487-978494", "SAVINGS", BigDecimal.valueOf(300.50));
//
//        when(transferService.listAccounts()).thenReturn(List.of(ownAccount, otherAccount));
//
//        // When
//        List<Account> result = accountController.getMyAccounts(jwt);
//
//        // Then
//        assertThat(result).hasSize(1);
//        assertThat(result.get(0).customerId()).isEqualTo("487-978493");
//    }

//    @Test
//    void getMyAccounts_asCustomer_filtersCorrectly() {
//        // Given
//        when(jwt.getSubject()).thenReturn("C002");
//        when(jwt.getClaimAsStringList("roles")).thenReturn(List.of("account_holder"));
//
//        Account acc1 = new Account("A001", "C001", "CHECKING", BigDecimal.valueOf(1250.00));
//        Account acc2 = new Account("A002", "C002", "SAVINGS", BigDecimal.valueOf(5000.00));
//        Account acc3 = new Account("A003", "C002", "CHECKING", BigDecimal.valueOf(2500.00));
//        Account acc4 = new Account("A004", "C003", "SAVINGS", BigDecimal.valueOf(15000.00));
//
//        when(transferService.listAccounts()).thenReturn(List.of(acc1, acc2, acc3, acc4));
//
//        // When
//        List<Account> result = accountController.getMyAccounts(jwt);
//
//        // Then
//        assertThat(result).hasSize(2);
//        assertThat(result).allMatch(a -> a.customerId().equals("C002"));
//    }
//
//    // ===== GET /api/v1/accounts/downstream =====
//
//    @Test
//    void getFromDownstream_success() {
//        // Given
//        Account acc1 = new Account("A001", "1", "CHECKING", BigDecimal.valueOf(1250.00));
//        Account acc2 = new Account("A002", "1", "SAVINGS", BigDecimal.valueOf(8400.00));
//
//        when(downstreamAccountService.fetchAllFromDownstream())
//                .thenReturn(List.of(acc1, acc2));
//
//        // When
//        List<Account> result = accountController.getFromDownstream();
//
//        // Then
//        assertThat(result).hasSize(2);
//        assertThat(result).containsExactly(acc1, acc2);
//        verify(downstreamAccountService).fetchAllFromDownstream();
//    }
//
//    @Test
//    void getFromDownstream_empty() {
//        // Given
//        when(downstreamAccountService.fetchAllFromDownstream())
//                .thenReturn(List.of());
//
//        // When
//        List<Account> result = accountController.getFromDownstream();
//
//        // Then
//        assertThat(result).isEmpty();
//    }
}