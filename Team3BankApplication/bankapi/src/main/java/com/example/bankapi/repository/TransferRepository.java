package com.example.bankapi.repository;

import com.example.bankapi.entity.Transfer;
import com.example.bankapi.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransferRepository extends JpaRepository<Transfer, String> {

    /**
     * Find a transfer by its debit transaction.
     */
    Optional<Transfer> findByDebitTransaction(Transaction debitTransaction);

    /**
     * Find a transfer by its credit transaction.
     */
    Optional<Transfer> findByCreditTransaction(Transaction creditTransaction);

    /**
     * Find all transfers created on or after a specific date, ordered by creation date descending.
     */
    List<Transfer> findByCreatedDateGreaterThanEqualOrderByCreatedDateDesc(LocalDateTime createdDate);

    /**
     * Find all transfers within a date range.
     */
    @Query("SELECT t FROM Transfer t WHERE t.createdDate BETWEEN :startDate AND :endDate ORDER BY t.createdDate DESC")
    List<Transfer> findTransfersByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Find all transfers for a specific debit account.
     */
    @Query("SELECT t FROM Transfer t WHERE t.debitTransaction.account.id = :accountId ORDER BY t.createdDate DESC")
    List<Transfer> findTransfersByDebitAccount(@Param("accountId") Long accountId);

    /**
     * Find all transfers for a specific credit account.
     */
    @Query("SELECT t FROM Transfer t WHERE t.creditTransaction.account.id = :accountId ORDER BY t.createdDate DESC")
    List<Transfer> findTransfersByCreditAccount(@Param("accountId") Long accountId);

    /**
     * Find all transfers involving a specific account (either as debit or credit).
     */
    @Query("SELECT t FROM Transfer t WHERE t.debitTransaction.account.id = :accountId OR t.creditTransaction.account.id = :accountId ORDER BY t.createdDate DESC")
    List<Transfer> findTransfersByInvolvedAccount(@Param("accountId") Long accountId);

    /**
     * Count all transfers created on a specific date.
     */
    @Query("SELECT COUNT(t) FROM Transfer t WHERE DATE(t.createdDate) = DATE(:date)")
    long countTransfersByDate(@Param("date") LocalDateTime date);

    /**
     * Find transfers between two specific accounts.
     */
    @Query("SELECT t FROM Transfer t WHERE (t.debitTransaction.account.id = :fromAccountId AND t.creditTransaction.account.id = :toAccountId) OR (t.debitTransaction.account.id = :toAccountId AND t.creditTransaction.account.id = :fromAccountId)")
    List<Transfer> findTransfersBetweenAccounts(@Param("fromAccountId") Long fromAccountId, @Param("toAccountId") Long toAccountId);

}

