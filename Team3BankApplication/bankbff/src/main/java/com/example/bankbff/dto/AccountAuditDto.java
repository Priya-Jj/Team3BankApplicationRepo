package com.example.bankbff.dto;

import org.springframework.cglib.core.Local;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

public class AccountAuditDto {
    private Long auditId;
    private Long accountId;
    private BigDecimal oldBalance;
    private BigDecimal newBalance;
    private LocalDateTime changedAt;
    private String actionType;

    public AccountAuditDto() {
    }

    public AccountAuditDto(Long auditId, Long accountId, BigDecimal oldBalance,
                           BigDecimal newBalance, LocalDateTime changedAt, String actionType) {
        this.auditId = auditId;
        this.accountId = accountId;
        this.oldBalance = oldBalance;
        this.newBalance = newBalance;
        this.changedAt = changedAt;
        this.actionType = actionType;
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

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }
}

