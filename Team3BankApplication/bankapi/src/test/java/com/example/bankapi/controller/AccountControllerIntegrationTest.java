package com.example.bankapi.controller;

import com.example.bankapi.entity.Accounts;
import com.example.bankapi.entity.AccountStatus;
import com.example.bankapi.entity.AccountType;
import com.example.bankapi.entity.Customer;
import com.example.bankapi.repository.AccountRepository;
import com.example.bankapi.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for AccountController with Spring context and security.
 * Tests include database interaction and OAuth2/JWT security.
 */
@SpringBootTest
class AccountControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CustomerRepository customerRepository;

    private Long customerId1;
    private Long customerId2;
    private Long accountId1;

    @BeforeEach
    void setUp() {
        // Clear test data
        accountRepository.deleteAll();
        customerRepository.deleteAll();

        // Create test customers
        Customer customer1 = new Customer();
        customer1.setCustomerNumber("487-978493");
        customer1.setFullName("Alice Customer");
        customer1.setEmail("alice@example.com");
        customer1.setCreatedDate(LocalDate.now());
        Customer saved1 = customerRepository.save(customer1);
        customerId1 = saved1.getId();

        Customer customer2 = new Customer();
        customer2.setCustomerNumber("487-978494");
        customer2.setFullName("Bob Customer");
        customer2.setEmail("bob@example.com");
        customer2.setCreatedDate(LocalDate.now());
        Customer saved2 = customerRepository.save(customer2);
        customerId2 = saved2.getId();

        // Create test accounts
        Accounts acc1 = new Accounts();
        acc1.setAccountNumber("128-9878-001");
        acc1.setCustomer(saved1);
        acc1.setAccountType(AccountType.CHECKING);
        acc1.setAccountStatus(AccountStatus.ACTIVE);
        acc1.setBalance(BigDecimal.valueOf(1250.00));
        acc1.setOpenedDate(LocalDateTime.now());
        Accounts savedAcc1 = accountRepository.save(acc1);
        accountId1 = savedAcc1.getId();

        Accounts acc2 = new Accounts();
        acc2.setAccountNumber("128-9878-002");
        acc2.setCustomer(saved1);
        acc2.setAccountType(AccountType.SAVINGS);
        acc2.setAccountStatus(AccountStatus.ACTIVE);
        acc2.setBalance(BigDecimal.valueOf(8400.00));
        acc2.setOpenedDate(LocalDateTime.now());
        accountRepository.save(acc2);

        Accounts acc3 = new Accounts();
        acc3.setAccountNumber("128-9878-003");
        acc3.setCustomer(saved2);
        acc3.setAccountType(AccountType.CHECKING);
        acc3.setAccountStatus(AccountStatus.ACTIVE);
        acc3.setBalance(BigDecimal.valueOf(300.50));
        acc3.setOpenedDate(LocalDateTime.now());
        accountRepository.save(acc3);
    }

    // ===== GET /api/v1/accounts =====

    @Test
    @WithMockUser(username = "487-978493", authorities = {"SCOPE_account.read", "ROLE_ACCOUNT_HOLDER"})
    void getByCustomerId_withValidAuth_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/accounts")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void getByCustomerId_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/accounts")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    // ===== GET /api/v1/accounts/{id} =====

    @Test
    @WithMockUser(username = "487-978493", authorities = {"SCOPE_account.read", "ROLE_ACCOUNT_HOLDER"})
    void getById_ownAccount_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/accounts/" + accountId1)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("128-9878-001"));
    }

    @Test
    @WithMockUser(username = "teller1", authorities = {"SCOPE_account.read", "ROLE_TELLER"})
    void getById_asTeller_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/accounts/" + accountId1)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("128-9878-001"));
    }

    @Test
    @WithMockUser(username = "487-978494", authorities = {"SCOPE_account.read", "ROLE_ACCOUNT_HOLDER"})
    void getById_otherCustomerAccount_returns403() throws Exception {
        // Customer 487-978494 (Bob) tries to access account of 487-978493 (Alice)
        mockMvc.perform(get("/api/v1/accounts/" + accountId1)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "487-978493", authorities = {"SCOPE_account.read", "ROLE_ACCOUNT_HOLDER"})
    void getById_invalidId_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/accounts/999")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    // ===== POST /api/v1/accounts =====

    @Test
    @WithMockUser(username = "teller1", authorities = {"SCOPE_account.create", "ROLE_TELLER"})
    void create_asTeller_returns201() throws Exception {
        String newAccount = """
                {
                  "id": "ACC_NEW",
                  "customerId": "1",
                  "type": "CHECKING",
                  "balance": "5000.00"
                }
                """;

        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newAccount)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "487-978493", authorities = {"SCOPE_account.read", "ROLE_ACCOUNT_HOLDER"})
    void create_asCustomer_returns403() throws Exception {
        String newAccount = """
                {
                  "id": "ACC_NEW",
                  "customerId": "1",
                  "type": "CHECKING",
                  "balance": "5000.00"
                }
                """;

        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newAccount)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    // ===== GET /api/v1/accounts/me =====

    @Test
    @WithMockUser(username = "487-978493", authorities = {"SCOPE_account.read", "ROLE_ACCOUNT_HOLDER"})
    void getCurrentUser_returns200WithValidClaims() throws Exception {
        mockMvc.perform(get("/api/v1/accounts/me")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subject").value("487-978493"))
                .andExpect(jsonPath("$.scopes").exists())
                .andExpect(jsonPath("$.tokenExpiry").exists());
    }

    // ===== GET /api/v1/accounts/mine =====

    @Test
    @WithMockUser(username = "487-978493", authorities = {"SCOPE_account.read", "ROLE_ACCOUNT_HOLDER"})
    void getMyAccounts_asCustomer_returnsOwnAccounts() throws Exception {
        mockMvc.perform(get("/api/v1/accounts/mine")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @WithMockUser(username = "teller1", authorities = {"SCOPE_account.read", "ROLE_TELLER"})
    void getMyAccounts_asTeller_returnsAllAccounts() throws Exception {
        mockMvc.perform(get("/api/v1/accounts/mine")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }

    // ===== GET /api/v1/accounts/downstream =====

    @Test
    @WithMockUser(username = "teller1", authorities = {"SCOPE_account.read", "ROLE_TELLER"})
    void getFromDownstream_asTeller_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/accounts/downstream")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getFromDownstream_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/accounts/downstream")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    // ===== Scope validation =====

    @Test
    @WithMockUser(username = "487-978493", authorities = {"ROLE_ACCOUNT_HOLDER"}) // Missing SCOPE_account.read
    void getByCustomerId_withoutScope_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/accounts")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "teller1", authorities = {"SCOPE_account.create", "ROLE_TELLER"}) // Missing SCOPE_account.read
    void getById_withoutReadScope_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/accounts/" + accountId1)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}