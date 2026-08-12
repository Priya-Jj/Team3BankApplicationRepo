package com.example.bankapi.repository;

import com.example.bankapi.entity.Accounts;
import com.example.bankapi.entity.AccountStatus;
import com.example.bankapi.entity.AccountType;
import com.example.bankapi.entity.Accounts;
import com.example.bankapi.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Accounts, Long> {

    /**
     * Find an account by its unique account number.
     */
    Optional<Accounts> findByAccountNumber(String accountNumber);

    /**
     * Find all accounts.
     */
    @Query("SELECT a FROM Accounts a")
    List<Accounts> findAll();

    /**
     * Find all accounts belonging to a specific customer.
     */
    List<Accounts> findByCustomer(Customer customer);

    /**
     * Find all accounts of a specific type (CHECKING or SAVINGS).
     */
    List<Accounts> findByAccountType(AccountType accountType);

    /**
     * Find all accounts with a specific status (ACTIVE or INACTIVE).
     */
    List<Accounts> findByAccountStatus(AccountStatus accountStatus);

    /**
     * Find all active accounts for a specific customer.
     */
    List<Accounts> findByCustomerAndAccountStatus(Customer customer, AccountStatus accountStatus);

    /**
     * Find all accounts with balance greater than or equal to a specified amount.
     */
    List<Accounts> findByBalanceGreaterThanEqual(BigDecimal balance);

    /**
     * Find all accounts with balance less than or equal to a specified amount.
     */
    List<Accounts> findByBalanceLessThanEqual(BigDecimal balance);

    /**
     * Custom query to find accounts by customer ID and account type.
     */
    @Query("SELECT a FROM Accounts a WHERE a.customer.id = :customerId AND a.accountType = :accountType")
    List<Accounts> findAccountsByCustomerAndType(@Param("customerId") Long customerId, @Param("accountType") AccountType accountType);

    /**
     * Custom query to find accounts by customer number and account type.
     */
    @Query("SELECT a FROM Accounts a WHERE a.customer.customerNumber = :customerNumber")
    List<Accounts> findAccountsByCustomerNumber(@Param("customerNumber") String customerNumber);

    /**
     * Custom query to find all active accounts ordered by balance in descending order.
     */
    @Query("SELECT a FROM Accounts a WHERE a.accountStatus = 'ACTIVE' ORDER BY a.balance DESC")
    List<Accounts> findAllActiveAccountsOrderedByBalance();

    /**
     * Find accounts by customer ID.
     */
    @Query("SELECT a FROM Accounts a WHERE a.customer.id = :customerId")
    List<Accounts> getByCustomerId(Long customerId);

    /**
     * Custom query to count active accounts for a customer.
     */
    @Query("SELECT COUNT(a) FROM Accounts a WHERE a.customer.id = :customerId AND a.accountStatus = 'ACTIVE'")
    long countActiveAccountsByCustomer(@Param("customerId") Long customerId);

}

