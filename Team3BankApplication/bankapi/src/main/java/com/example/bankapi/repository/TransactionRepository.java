package com.example.bankapi.repository;

import com.example.bankapi.entity.Accounts;
import com.example.bankapi.entity.Transaction;
import com.example.bankapi.entity.TxnStatus;
import com.example.bankapi.entity.TxnType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {

    /**
     * Find all transactions for a specific account.
     */
    List<Transaction> findByAccount(Accounts account);

    /**
     * Find all transactions for a specific account ordered by date descending.
     */
    List<Transaction> findByAccountOrderByTxnDateDesc(Accounts account);

    /**
     * Find all transactions of a specific type (DEPOSIT, WITHDRAWAL, TRANSFER_IN, TRANSFER_OUT, PAYMENT).
     */
    List<Transaction> findByTxnType(TxnType txnType);

    /**
     * Find all transactions with a specific status (COMPLETED or FAILED).
     */
    List<Transaction> findByStatus(TxnStatus status);

    /**
     * Find all completed transactions for a specific account.
     */
    List<Transaction> findByAccountAndStatus(Accounts account, TxnStatus status);

    /**
     * Find transactions by account ID and status.
     */
    List<Transaction> findByAccountIdAndStatus(Long accountId, TxnStatus status);

    /**
     * Find all transactions within a date range.
     */
    @Query("SELECT t FROM Transaction t WHERE t.txnDate BETWEEN :startDate AND :endDate ORDER BY t.txnDate DESC")
    List<Transaction> findTransactionsByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Find all transactions for a specific account within a date range.
     */
    @Query("SELECT t FROM Transaction t WHERE t.account.id = :accountId AND t.txnDate BETWEEN :startDate AND :endDate ORDER BY t.txnDate DESC")
    List<Transaction> findAccountTransactionsByDateRange(@Param("accountId") Long accountId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Find all failed transactions.
     */
    List<Transaction> findByStatusOrderByTxnDateDesc(TxnStatus status);

    /**
     * Custom query to calculate total transaction amount for an account.
     */
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.account.id = :accountId AND t.status = 'COMPLETED'")
    BigDecimal calculateTotalCompletedTransactionsByAccount(@Param("accountId") Long accountId);

    /**
     * Find all transactions of a specific type and status.
     */
    List<Transaction> findByTxnTypeAndStatus(TxnType txnType, TxnStatus status);

    /**
     * Count completed transactions for an account.
     */
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.account.id = :accountId AND t.status = 'COMPLETED'")
    long countCompletedTransactionsByAccount(@Param("accountId") Long accountId);

}

