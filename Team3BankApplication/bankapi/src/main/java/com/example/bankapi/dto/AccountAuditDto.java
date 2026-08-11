package com.example.bankapi.dto;

import java.math.BigDecimal;
import java.time.Instant;

public class AccountAuditDto {
    private Long auditId;
    private Long accountId;
    private BigDecimal oldBalance;
    private BigDecimal newBalance;
    private Instant changedAt;

    public AccountAuditDto() {
    }

    public AccountAuditDto(Long auditId, Long accountId, BigDecimal oldBalance,
                          BigDecimal newBalance, Instant changedAt) {
        this.auditId = auditId;
        this.accountId = accountId;
        this.oldBalance = oldBalance;
        this.newBalance = newBalance;
        this.changedAt = changedAt;
    }

    public Long getAuditId() {
        return auditId;
    }

    public void setAuditId(Long auditId) {
        this.auditId = auditId;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public BigDecimal getOldBalance() {
        return oldBalance;
    }

    public void setOldBalance(BigDecimal oldBalance) {
        this.oldBalance = oldBalance;
    }

    public BigDecimal getNewBalance() {
        return newBalance;
    }

    public void setNewBalance(BigDecimal newBalance) {
        this.newBalance = newBalance;
    }

    public Instant getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(Instant changedAt) {
        this.changedAt = changedAt;
    }
}

