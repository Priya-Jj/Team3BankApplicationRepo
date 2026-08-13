package com.example.bankapi.repository;

import com.example.bankapi.entity.AccountAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AccountAuditRepository extends JpaRepository<AccountAudit, Long> {

    /**
     * Find all audit records in the table, ordered by change date descending.
     */
    @Query("SELECT a FROM AccountAudit a ORDER BY a.changedAt DESC")
    List<AccountAudit> findAllAudits();

    /**
     * Find all audit records for a specific account, ordered by change date descending.
     */
    List<AccountAudit> findByAccountIdOrderByChangedAtDesc(Long accountId);

    /**
     * Find all audit records for a specific account within a date range.
     */
    @Query("SELECT a FROM AccountAudit a WHERE a.accountId = :accountId AND a.changedAt BETWEEN :startDate AND :endDate ORDER BY a.changedAt DESC")
    List<AccountAudit> findAuditsByAccountAndDateRange(@Param("accountId") Long accountId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Find all audit records created on or after a specific date.
     */
    List<AccountAudit> findByChangedAtGreaterThanEqualOrderByChangedAtDesc(LocalDateTime changedAt);

    /**
     * Find all audit records within a date range.
     */
    @Query("SELECT a FROM AccountAudit a WHERE a.changedAt BETWEEN :startDate AND :endDate ORDER BY a.changedAt DESC")
    List<AccountAudit> findAuditsByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Count audit records for a specific account.
     */
    long countByAccountId(Long accountId);

    /**
     * Find the most recent audit record for an account.
     */
    @Query("SELECT a FROM AccountAudit a WHERE a.accountId = :accountId ORDER BY a.changedAt DESC LIMIT 1")
    AccountAudit findMostRecentAuditByAccount(@Param("accountId") Long accountId);

    /**
     * Custom query to find all audit records where balance decreased.
     */
    @Query("SELECT a FROM AccountAudit a WHERE a.newBalance < a.oldBalance ORDER BY a.changedAt DESC")
    List<AccountAudit> findAuditsWithBalanceDecrease();

    /**
     * Custom query to find all audit records where balance increased.
     */
    @Query("SELECT a FROM AccountAudit a WHERE a.newBalance > a.oldBalance ORDER BY a.changedAt DESC")
    List<AccountAudit> findAuditsWithBalanceIncrease();

    /**
     * Find all audit records for a specific account where the balance changed by a specific amount.
     */
    @Query("SELECT a FROM AccountAudit a WHERE a.accountId = :accountId AND (a.newBalance - a.oldBalance) = :changeAmount ORDER BY a.changedAt DESC")
    List<AccountAudit> findAuditsByAccountAndChangeAmount(@Param("accountId") Long accountId, @Param("changeAmount") java.math.BigDecimal changeAmount);

}

