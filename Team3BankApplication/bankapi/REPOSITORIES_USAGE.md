# Spring Data JPA Repositories Documentation

This document provides usage examples for all Spring Data JPA repositories created for the banking application entities.

## Overview

Five repositories have been created under `com.example.bankapi.repository`:
- `CustomerRepository` — CRUD and queries for customers
- `AccountRepository` — CRUD and queries for accounts
- `TransactionRepository` — CRUD and queries for transactions
- `TransferRepository` — CRUD and queries for transfers
- `AccountAuditRepository` — CRUD and queries for account audit records

All repositories extend `JpaRepository<Entity, ID>` from Spring Data JPA, which provides basic CRUD operations plus query methods.

---

## CustomerRepository

**Entity**: `Customer`

### Basic CRUD Operations (inherited from JpaRepository)
```java
// Save a new customer
Customer customer = new Customer();
customer.setCustomerNumber("C001");
customer.setFullName("John Doe");
customer.setEmail("john@example.com");
Customer saved = customerRepository.save(customer);

// Find by ID
Optional<Customer> found = customerRepository.findById(1L);

// Find all customers
List<Customer> allCustomers = customerRepository.findAll();

// Delete
customerRepository.deleteById(1L);
```

### Custom Query Methods

#### Find by customer number
```java
Optional<Customer> customer = customerRepository.findByCustomerNumber("C001");
```

#### Find by email
```java
Optional<Customer> customer = customerRepository.findByEmail("john@example.com");
```

#### Find by name (contains, case-insensitive)
```java
List<Customer> results = customerRepository.findByFullNameContainingIgnoreCase("John");
```

#### Find customers created after a date
```java
LocalDate startDate = LocalDate.of(2024, 1, 1);
List<Customer> newCustomers = customerRepository.findCustomersCreatedAfter(startDate);
```

#### Find all customers ordered by creation date
```java
List<Customer> orderedCustomers = customerRepository.findAllByOrderByCreatedDateDesc();
```

---

## AccountRepository

**Entity**: `Account`

### Basic CRUD Operations
```java
// Save a new account
Account account = new Account();
account.setAccountNumber("ACC12345");
account.setCustomer(customer);
account.setAccountType(AccountType.CHECKING);
account.setAccountStatus(AccountStatus.ACTIVE);
account.setBalance(BigDecimal.valueOf(5000.00));
Account saved = accountRepository.save(account);

// Update balance
account.setBalance(BigDecimal.valueOf(6000.00));
accountRepository.save(account);
```

### Custom Query Methods

#### Find by account number
```java
Optional<Account> account = accountRepository.findByAccountNumber("ACC12345");
```

#### Find all accounts for a customer
```java
List<Account> customerAccounts = accountRepository.findByCustomer(customer);
```

#### Find all accounts by type
```java
List<Account> checkingAccounts = accountRepository.findByAccountType(AccountType.CHECKING);
```

#### Find all active accounts
```java
List<Account> activeAccounts = accountRepository.findByAccountStatus(AccountStatus.ACTIVE);
```

#### Find active accounts for a specific customer
```java
List<Account> customerActiveAccounts = accountRepository
    .findByCustomerAndAccountStatus(customer, AccountStatus.ACTIVE);
```

#### Find accounts by balance range
```java
// Accounts with minimum balance
List<Account> richAccounts = accountRepository
    .findByBalanceGreaterThanEqual(BigDecimal.valueOf(10000));

// Accounts with less than specific balance
List<Account> lowAccounts = accountRepository
    .findByBalanceLessThanEqual(BigDecimal.valueOf(1000));
```

#### Find accounts by customer and type (custom query)
```java
List<Account> savingsAccounts = accountRepository
    .findAccountsByCustomerAndType(customerId, AccountType.SAVINGS);
```

#### Find all active accounts ordered by balance
```java
List<Account> topAccounts = accountRepository.findAllActiveAccountsOrderedByBalance();
```

#### Count active accounts for a customer
```java
long count = accountRepository.countActiveAccountsByCustomer(customerId);
```

---

## TransactionRepository

**Entity**: `Transaction`

### Basic CRUD Operations
```java
// Save a new transaction
Transaction transaction = new Transaction();
transaction.setId(UUID.randomUUID().toString());
transaction.setAccount(account);
transaction.setTxnType(TxnType.DEPOSIT);
transaction.setAmount(BigDecimal.valueOf(500));
transaction.setStatus(TxnStatus.COMPLETED);
transaction.setTxnDate(LocalDateTime.now());
transaction.setDescription("Salary deposit");
Transaction saved = transactionRepository.save(transaction);
```

### Custom Query Methods

#### Find all transactions for an account
```java
List<Transaction> accountTransactions = transactionRepository.findByAccount(account);
```

#### Find transactions ordered by date (newest first)
```java
List<Transaction> recentTransactions = transactionRepository
    .findByAccountOrderByTxnDateDesc(account);
```

#### Find transactions by type
```java
List<Transaction> deposits = transactionRepository.findByTxnType(TxnType.DEPOSIT);
List<Transaction> withdrawals = transactionRepository.findByTxnType(TxnType.WITHDRAWAL);
```

#### Find transactions by status
```java
List<Transaction> completedTxns = transactionRepository.findByStatus(TxnStatus.COMPLETED);
List<Transaction> failedTxns = transactionRepository.findByStatus(TxnStatus.FAILED);
```

#### Find completed transactions for an account
```java
List<Transaction> completed = transactionRepository
    .findByAccountAndStatus(account, TxnStatus.COMPLETED);
```

#### Find transactions by account ID and status
```java
List<Transaction> txns = transactionRepository
    .findByAccountIdAndStatus(accountId, TxnStatus.COMPLETED);
```

#### Find transactions within a date range
```java
LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0);
LocalDateTime end = LocalDateTime.of(2024, 1, 31, 23, 59);
List<Transaction> rangeTransactions = transactionRepository
    .findTransactionsByDateRange(start, end);
```

#### Find account transactions within a date range
```java
List<Transaction> accountTxns = transactionRepository
    .findAccountTransactionsByDateRange(accountId, start, end);
```

#### Find failed transactions (ordered by date)
```java
List<Transaction> failed = transactionRepository
    .findByStatusOrderByTxnDateDesc(TxnStatus.FAILED);
```

#### Calculate total completed transactions for an account
```java
BigDecimal total = transactionRepository
    .calculateTotalCompletedTransactionsByAccount(accountId);
```

#### Find transactions by type and status
```java
List<Transaction> completedDeposits = transactionRepository
    .findByTxnTypeAndStatus(TxnType.DEPOSIT, TxnStatus.COMPLETED);
```

#### Count completed transactions for an account
```java
long count = transactionRepository.countCompletedTransactionsByAccount(accountId);
```

---

## TransferRepository

**Entity**: `Transfer`

### Basic CRUD Operations
```java
// Save a new transfer
Transfer transfer = new Transfer();
transfer.setId(UUID.randomUUID().toString());
transfer.setDebitTransaction(debitTxn);
transfer.setCreditTransaction(creditTxn);
transfer.setCreatedDate(LocalDateTime.now());
Transfer saved = transferRepository.save(transfer);
```

### Custom Query Methods

#### Find transfer by debit transaction
```java
Optional<Transfer> transfer = transferRepository.findByDebitTransaction(debitTxn);
```

#### Find transfer by credit transaction
```java
Optional<Transfer> transfer = transferRepository.findByCreditTransaction(creditTxn);
```

#### Find recent transfers (created after a date)
```java
LocalDateTime date = LocalDateTime.of(2024, 1, 1, 0, 0);
List<Transfer> recentTransfers = transferRepository
    .findByCreatedDateGreaterThanEqualOrderByCreatedDateDesc(date);
```

#### Find transfers within a date range
```java
List<Transfer> rangeTransfers = transferRepository
    .findTransfersByDateRange(startDate, endDate);
```

#### Find all transfers from a specific account (as sender)
```java
List<Transfer> outgoing = transferRepository.findTransfersByDebitAccount(accountId);
```

#### Find all transfers to a specific account (as receiver)
```java
List<Transfer> incoming = transferRepository.findTransfersByCreditAccount(accountId);
```

#### Find all transfers involving a specific account (sender or receiver)
```java
List<Transfer> allTransfers = transferRepository
    .findTransfersByInvolvedAccount(accountId);
```

#### Count transfers created on a specific date
```java
long count = transferRepository.countTransfersByDate(LocalDateTime.now());
```

#### Find transfers between two specific accounts
```java
List<Transfer> transfers = transferRepository
    .findTransfersBetweenAccounts(fromAccountId, toAccountId);
```

---

## AccountAuditRepository

**Entity**: `AccountAudit`

### Basic CRUD Operations
```java
// Save a new audit record
AccountAudit audit = new AccountAudit();
audit.setAccountId(accountId);
audit.setOldBalance(BigDecimal.valueOf(1000));
audit.setNewBalance(BigDecimal.valueOf(1500));
audit.setChangedAt(LocalDateTime.now());
AccountAudit saved = accountAuditRepository.save(audit);
```

### Custom Query Methods

#### Find audit records for an account (ordered by date)
```java
List<AccountAudit> audits = accountAuditRepository
    .findByAccountIdOrderByChangedAtDesc(accountId);
```

#### Find audit records for an account within a date range
```java
List<AccountAudit> rangeAudits = accountAuditRepository
    .findAuditsByAccountAndDateRange(accountId, startDate, endDate);
```

#### Find audits created after a specific date
```java
List<AccountAudit> recentAudits = accountAuditRepository
    .findByChangedAtGreaterThanEqualOrderByChangedAtDesc(LocalDateTime.now());
```

#### Find audits within a date range (all accounts)
```java
List<AccountAudit> allAudits = accountAuditRepository
    .findAuditsByDateRange(startDate, endDate);
```

#### Count audit records for an account
```java
long count = accountAuditRepository.countByAccountId(accountId);
```

#### Find most recent audit for an account
```java
AccountAudit latest = accountAuditRepository.findMostRecentAuditByAccount(accountId);
```

#### Find audits where balance decreased
```java
List<AccountAudit> decreases = accountAuditRepository.findAuditsWithBalanceDecrease();
```

#### Find audits where balance increased
```java
List<AccountAudit> increases = accountAuditRepository.findAuditsWithBalanceIncrease();
```

#### Find audits by specific balance change amount
```java
BigDecimal changeAmount = BigDecimal.valueOf(500);
List<AccountAudit> changes = accountAuditRepository
    .findAuditsByAccountAndChangeAmount(accountId, changeAmount);
```

---

## Integration Example: Using Multiple Repositories

```java
@Service
public class BankingService {
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private TransferRepository transferRepository;
    
    @Autowired
    private AccountAuditRepository auditRepository;
    
    /**
     * Complete workflow: Create customer, add accounts, perform transfer, record audit.
     */
    public void performTransfer(String fromAccountNumber, String toAccountNumber, BigDecimal amount) {
        // Find accounts
        Account fromAccount = accountRepository.findByAccountNumber(fromAccountNumber)
            .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        Account toAccount = accountRepository.findByAccountNumber(toAccountNumber)
            .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        
        // Verify sufficient balance
        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds");
        }
        
        // Create debit transaction
        Transaction debitTxn = new Transaction();
        debitTxn.setId(UUID.randomUUID().toString());
        debitTxn.setAccount(fromAccount);
        debitTxn.setTxnType(TxnType.TRANSFER_OUT);
        debitTxn.setAmount(amount);
        debitTxn.setStatus(TxnStatus.COMPLETED);
        debitTxn.setTxnDate(LocalDateTime.now());
        Transaction savedDebit = transactionRepository.save(debitTxn);
        
        // Create credit transaction
        Transaction creditTxn = new Transaction();
        creditTxn.setId(UUID.randomUUID().toString());
        creditTxn.setAccount(toAccount);
        creditTxn.setTxnType(TxnType.TRANSFER_IN);
        creditTxn.setAmount(amount);
        creditTxn.setStatus(TxnStatus.COMPLETED);
        creditTxn.setTxnDate(LocalDateTime.now());
        Transaction savedCredit = transactionRepository.save(creditTxn);
        
        // Create transfer record
        Transfer transfer = new Transfer();
        transfer.setId(UUID.randomUUID().toString());
        transfer.setDebitTransaction(savedDebit);
        transfer.setCreditTransaction(savedCredit);
        transfer.setCreatedDate(LocalDateTime.now());
        transferRepository.save(transfer);
        
        // Update account balances
        BigDecimal newFromBalance = fromAccount.getBalance().subtract(amount);
        BigDecimal newToBalance = toAccount.getBalance().add(amount);
        
        fromAccount.setBalance(newFromBalance);
        toAccount.setBalance(newToBalance);
        
        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);
        
        // Record audit for both accounts
        auditRepository.save(new AccountAudit()
            .setAccountId(fromAccount.getId())
            .setOldBalance(fromAccount.getBalance().add(amount))
            .setNewBalance(newFromBalance)
            .setChangedAt(LocalDateTime.now()));
        
        auditRepository.save(new AccountAudit()
            .setAccountId(toAccount.getId())
            .setOldBalance(toAccount.getBalance().subtract(amount))
            .setNewBalance(newToBalance)
            .setChangedAt(LocalDateTime.now()));
    }
}
```

---

## Pagination and Sorting Example

All repositories support `Pageable` parameter for pagination:

```java
// Paginate customers
Page<Customer> page = customerRepository.findAll(
    PageRequest.of(0, 10, Sort.by("createdDate").descending())
);

// Paginate accounts
Page<Account> accountPage = accountRepository.findByAccountStatus(
    AccountStatus.ACTIVE,
    PageRequest.of(0, 20)
);
```

---

## Key Features of These Repositories

1. **Method Naming Conventions**: Spring Data JPA automatically generates queries from method names
   - `findBy<FieldName>` → WHERE field = ?
   - `findBy<Field>GreaterThan` → WHERE field > ?
   - `OrderBy<Field>` → ORDER BY field
   - `Containing` → LIKE search
   - `IgnoreCase` → case-insensitive

2. **Custom @Query Annotations**: For complex queries using JPQL/HQL
   - Allows custom SQL-like syntax
   - Use `@Param` for named parameters

3. **Optional<T> Return Type**: Safely handles queries that may return null
   - Use `.orElse()`, `.orElseThrow()`, `.ifPresent()` for handling

4. **List<T> Return Type**: For queries that return multiple results

5. **Aggregation Methods**: `COUNT`, `SUM` via custom queries

---

## Next Steps

- Inject these repositories into services using `@Autowired`
- Use them in REST controllers to handle HTTP requests
- Add transaction management with `@Transactional` for complex operations
- Implement pagination for large result sets
- Add custom specifications using Spring Data JPA's `Specification` interface for advanced filtering


