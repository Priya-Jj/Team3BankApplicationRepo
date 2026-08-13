package com.example.bankapi.service;

import com.example.bankapi.dto.AccountsDto;
import com.example.bankapi.entity.AccountStatus;
import com.example.bankapi.entity.AccountType;
import com.example.bankapi.entity.Accounts;
import com.example.bankapi.entity.Customer;
import com.example.bankapi.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AccountService (service logic and DTO mapping).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AccountService - unit tests")
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    private AccountService accountService;

    @BeforeEach
    void setUp() {
        accountService = new AccountService(accountRepository);
    }

    private Customer newCustomer(Long id, String customerNumber) {
        Customer c = new Customer();
        c.setId(id);
        c.setCustomerNumber(customerNumber);
        c.setFullName("Test " + customerNumber);
        c.setEmail(customerNumber + "@example.com");
        return c;
    }

    private Accounts newAccounts(Long id, String accountNumber, Customer customer, AccountType type, AccountStatus status, String balance) {
        Accounts a = new Accounts();
        a.setId(id);
        a.setAccountNumber(accountNumber);
        a.setCustomer(customer);
        a.setAccountType(type);
        a.setAccountStatus(status);
        a.setBalance(new BigDecimal(balance));
        a.setOpenedDate(LocalDateTime.now());
        return a;
    }

    @Test
    void findAll_mapsEntitiesToDto() {
        Customer c1 = newCustomer(10L, "C001");
        Customer c2 = newCustomer(11L, "C002");

        Accounts a1 = newAccounts(1L, "A001", c1, AccountType.CHECKING, AccountStatus.ACTIVE, "1250.00");
        Accounts a2 = newAccounts(2L, "A002", c2, AccountType.SAVINGS, AccountStatus.ACTIVE, "8400.00");

        when(accountRepository.findAll()).thenReturn(List.of(a1, a2));

        List<AccountsDto> result = accountService.findAll();

        assertThat(result).hasSize(2);
        AccountsDto dto1 = result.get(0);
        assertThat(dto1.getAccountId()).isEqualTo(1L);
        assertThat(dto1.getAccountNumber()).isEqualTo("A001");
        assertThat(dto1.getCustomerId()).isEqualTo(10L);
        assertThat(dto1.getBalance()).isEqualByComparingTo(new BigDecimal("1250.00"));
        verify(accountRepository, times(1)).findAll();
    }

    @Test
    void findById_existing_returnsDto() {
        Customer c = newCustomer(10L, "C001");
        Accounts a = newAccounts(5L, "A005", c, AccountType.CHECKING, AccountStatus.ACTIVE, "300.50");

        when(accountRepository.findById(5L)).thenReturn(Optional.of(a));

        AccountsDto dto = accountService.findById(5L);

        assertThat(dto).isNotNull();
        assertThat(dto.getAccountId()).isEqualTo(5L);
        assertThat(dto.getAccountNumber()).isEqualTo("A005");
        assertThat(dto.getCustomerId()).isEqualTo(10L);
        verify(accountRepository, times(1)).findById(5L);
    }

    @Test
    void findById_missing_throwsNoSuchElement() {
        when(accountRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.findById(999L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Account not found");
    }

    @Test
    void getByCustomerNumber_delegatesToRepository_and_maps() {
        Customer c = newCustomer(20L, "C020");
        Accounts a1 = newAccounts(10L, "A010", c, AccountType.CHECKING, AccountStatus.ACTIVE, "1000.00");
        Accounts a2 = newAccounts(11L, "A011", c, AccountType.SAVINGS, AccountStatus.ACTIVE, "2000.00");

        when(accountRepository.findAccountsByCustomerNumber("C020")).thenReturn(List.of(a1, a2));

        List<AccountsDto> dtos = accountService.getByCustomerNumber("C020");

        assertThat(dtos).hasSize(2);
        assertThat(dtos).extracting(AccountsDto::getAccountNumber).containsExactly("A010", "A011");
        verify(accountRepository, times(1)).findAccountsByCustomerNumber("C020");
    }

//    @Test
//    void create_and_update_returnModelAndDoNotTouchRepository() {
//        // create uses internal store (not repository) and returns the Account passed in
//        AccountsDto model = new AccountsDto("NEW-1", "C001", "CHECKING", new BigDecimal("500.00"));
//        Account created = accountService.create(model);
//
//        assertThat(created).isSameAs(model);
//
//        // update similarly returns the Account passed in
//        Account updated = new Account("NEW-1", "C001", "CHECKING", new BigDecimal("600.00"));
//        Account result = accountService.update(updated);
//        assertThat(result).isSameAs(updated);
//
//        // create/update are in-memory operations; repository not invoked
//        verifyNoInteractions(accountRepository);
//    }

    @Test
    void findAllInternal_callsFindAllRepository() {
        Customer c = newCustomer(30L, "C030");
        Accounts a = newAccounts(100L, "A100", c, AccountType.CHECKING, AccountStatus.ACTIVE, "50.00");
        when(accountRepository.findAll()).thenReturn(List.of(a));

        List<AccountsDto> internal = accountService.findAllInternal();

        assertThat(internal).hasSize(1);
        verify(accountRepository, times(1)).findAll();
    }
    // -------------------------------------------------------------------------
    // Additional PreAuthorize tests
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "C001",
            authorities = {"SCOPE_account.read", "ROLE_ACCOUNT_HOLDER"})
    void getByCustomerNumber_asOwner_returnsAccounts() {
        // C001 should be able to list their own accounts
        var result = accountService.getByCustomerNumber("C001");
        assertThat(result).isNotEmpty();
    }

    @Test
    @WithMockUser(username = "C002",
            authorities = {"SCOPE_account.read", "ROLE_ACCOUNT_HOLDER"})
    void getByCustomerNumber_asNonOwner_throwsAccessDeniedException() {
        // C002 (Bob) attempting to list C001 (Alice) accounts should be denied
        assertThatThrownBy(() -> accountService.getByCustomerNumber("C001"))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @WithMockUser(username = "EM01",
            authorities = {"SCOPE_account.read", "ROLE_TELLER"})
    void getByCustomerNumber_asTeller_returnsAccounts() {
        // Tellers may list any customer's accounts
        var result = accountService.getByCustomerNumber("C001");
        assertThat(result).isNotEmpty();
    }

    @Test
    @WithMockUser(username = "C001",
            authorities = {"ROLE_ACCOUNT_HOLDER"}) // Missing SCOPE_account.read
    void findById_withoutScope_throwsAccessDeniedException() {
        // findById requires SCOPE_account.read; this user has only role
        Long accountId_C001 = 1L;
        assertThatThrownBy(() -> accountService.findById(accountId_C001))
                .isInstanceOf(AccessDeniedException.class);
    }

//    @Test
//    @WithMockUser(username = "EM01",
//            authorities = {"SCOPE_account.write", "ROLE_TELLER"})
//    void update_asTellerWithWriteScope_succeeds() {
//        // Teller with write scope should be allowed to update
//        com.example.bankapi.model.Account updated = new com.example.bankapi.model.Account("A001", "C001", "CHECKING", new java.math.BigDecimal("1111.11"));
//        var res = accountService.update(updated);
//        assertThat(res).isNotNull();
//        assertThat(res.id()).isEqualTo("A001");
//    }
}