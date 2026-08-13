package com.example.bankapi.service;

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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class AccountServiceSecurityTest {

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @BeforeEach
    void setUp() {
        // Clear existing test data
        accountRepository.deleteAll();
        customerRepository.deleteAll();

        // Create test customers
        Customer customer1 = new Customer();
        customer1.setCustomerNumber("C001");
        customer1.setFullName("Alice");
        customer1.setEmail("alice@example.com");
        customer1.setCreatedDate(LocalDate.now());
        Customer savedCustomer1 = customerRepository.save(customer1);

        Customer customer2 = new Customer();
        customer2.setCustomerNumber("C002");
        customer2.setFullName("Bob");
        customer2.setEmail("bob@example.com");
        customer2.setCreatedDate(LocalDate.now());
        Customer savedCustomer2 = customerRepository.save(customer2);

        Customer customer3 = new Customer();
        customer3.setCustomerNumber("C003");
        customer3.setFullName("Charlie");
        customer3.setEmail("charlie@example.com");
        customer3.setCreatedDate(LocalDate.now());
        Customer savedCustomer3 = customerRepository.save(customer3);

        // Create test accounts
        Accounts acc1 = new Accounts();
        acc1.setAccountNumber("A001");
        acc1.setCustomer(savedCustomer1);
        acc1.setAccountType(AccountType.CHECKING);
        acc1.setAccountStatus(AccountStatus.ACTIVE);
        acc1.setBalance(BigDecimal.valueOf(1250.00));
        acc1.setOpenedDate(LocalDateTime.now());
        accountRepository.save(acc1);

        Accounts acc2 = new Accounts();
        acc2.setAccountNumber("A002");
        acc2.setCustomer(savedCustomer1);
        acc2.setAccountType(AccountType.SAVINGS);
        acc2.setAccountStatus(AccountStatus.ACTIVE);
        acc2.setBalance(BigDecimal.valueOf(8400.00));
        acc2.setOpenedDate(LocalDateTime.now());
        accountRepository.save(acc2);

        Accounts acc3 = new Accounts();
        acc3.setAccountNumber("A003");
        acc3.setCustomer(savedCustomer2);
        acc3.setAccountType(AccountType.CHECKING);
        acc3.setAccountStatus(AccountStatus.ACTIVE);
        acc3.setBalance(BigDecimal.valueOf(300.50));
        acc3.setOpenedDate(LocalDateTime.now());
        accountRepository.save(acc3);

        Accounts acc4 = new Accounts();
        acc4.setAccountNumber("A004");
        acc4.setCustomer(savedCustomer3);
        acc4.setAccountType(AccountType.CHECKING);
        acc4.setAccountStatus(AccountStatus.ACTIVE);
        acc4.setBalance(BigDecimal.valueOf(2100.75));
        acc4.setOpenedDate(LocalDateTime.now());
        accountRepository.save(acc4);

        Accounts acc5 = new Accounts();
        acc5.setAccountNumber("A005");
        acc5.setCustomer(savedCustomer3);
        acc5.setAccountType(AccountType.SAVINGS);
        acc5.setAccountStatus(AccountStatus.ACTIVE);
        acc5.setBalance(BigDecimal.valueOf(15000.00));
        acc5.setOpenedDate(LocalDateTime.now());
        accountRepository.save(acc5);
    }

    private Long accountId_C001; // alice
    private Long accountId_C002; // bob for testing non-owner access

    // Store customer IDs for later reference in SpEL expressions
    private Long customerId_C001;
    private Long customerId_C002;

    @BeforeEach
    void setupAccountIds() {
        var customers = customerRepository.findAll();
        customerId_C001 = customers.stream()
                .filter(c -> c.getCustomerNumber().equals("C001"))
                .findFirst()
                .map(Customer::getId)
                .orElseThrow();
        customerId_C002 = customers.stream()
                .filter(c -> c.getCustomerNumber().equals("C002"))
                .findFirst()
                .map(Customer::getId)
                .orElseThrow();

        var accounts = accountRepository.findAll();
        // Get alice's first account (A001)
        accountId_C001 = accounts.stream()
                .filter(a -> a.getCustomer().getId().equals(customerId_C001))
                .findFirst()
                .map(Accounts::getId)
                .orElseThrow();
        // Get bob's account (A003)
        accountId_C002 = accounts.stream()
                .filter(a -> a.getCustomer().getId().equals(customerId_C002))
                .findFirst()
                .map(Accounts::getId)
                .orElseThrow();
    }

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
        //          Accounts holders should never be able to list every account in the bank.
        assertThatThrownBy(() -> accountService.findAll())
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @WithMockUser(username = "EM01",
            authorities = {"SCOPE_account.read", "ROLE_TELLER"})
    void findById_asTeller_returnsAccount() {
        // A teller (EM01) can view any account
        var result = accountService.findById(accountId_C001);
        assertThat(result).isNotNull();
    }

    @Test
    @WithMockUser(username = "C001",
            authorities = {"SCOPE_account.read", "ROLE_ACCOUNT_HOLDER"})
    void findById_asNonOwner_throwsAccessDeniedException() {
        // alice (C001) trying to look up an account owned by C002 (bob).
        // The method body runs, returns the account, then @PostAuthorize denies it.
        assertThatThrownBy(() -> accountService.findById(accountId_C002))
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
        // TODO: Build a new Accounts and call create(). Assert the return value is not null.
//        assertThat(accountService.create(new com.example.bankapi.dto.AccountsDto("1","A006", "CHECKING", new BigDecimal("1000.00"))))).isNotNull();
    }

    // TODO 20: Write a test verifying create() throws AccessDeniedException when the
    //          caller is an auditor (has read scopes but no create scope, no teller role).
    @Test
    @WithMockUser(username = "AUD01",
            authorities = {"SCOPE_account.read", "ROLE_AUDITOR"})
    void create_asAuditor_throwsAccessDeniedException() {
        // TODO: Build a new Accounts and assert that create() throws AccessDeniedException.
//        assertThatThrownBy(() -> accountService.create(new com.example.bankapi.dto.AccountsDto("1","A006", "CHECKING", new BigDecimal("1000.00"))))
//                .isInstanceOf(AccessDeniedException.class);
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
        // This test demonstrates the AOP proxy self-invocation bypass issue.
        // When findAllInternal() calls this.findAll(), it bypasses Spring's AOP proxy,
        // so @PreAuthorize is NOT enforced. This succeeds even though no user is authenticated.
        //
        // Why this happens:
        // - 'this' refers to the actual service object, not the AOP proxy
        // - Spring intercepts method calls through the proxy, but direct object calls do not
        //
        // Correct solutions:
        // 1. Use ObjectFactory<AccountService> and inject via factory method (rare)
        // 2. Move internal method to separate class and inject that (clean)
        // 3. Use a direct repository call instead of calling findAll() (simplest for this case)
        // 4. Use ApplicationContext.getBean() to get the proxy (antipattern)
        //
        // Production security risk:
        // An internal method can bypass security by calling protected methods on self.
        // This can leak sensitive data to unauthorized users. Always be careful with
        // internal method design and prefer dependency injection over self-invocation.
        var result = accountService.findAllInternal();
        assertThat(result).isNotEmpty();
    }
}