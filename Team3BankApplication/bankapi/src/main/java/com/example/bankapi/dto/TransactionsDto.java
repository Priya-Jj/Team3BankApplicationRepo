package com.example.bankapi.dto;

import java.math.BigDecimal;
import java.time.Instant;

public class TransactionsDto {
    private String txnId;
    private Long accountId;
    private String txnType;
    private BigDecimal amount;
    private String status;
    private Instant txnDate;
    private String description;

    public TransactionsDto() {
    }

    public TransactionsDto(String txnId, Long accountId, String txnType, BigDecimal amount,
                          String status, Instant txnDate, String description) {
        this.txnId = txnId;
        this.accountId = accountId;
        this.txnType = txnType;
        this.amount = amount;
        this.status = status;
        this.txnDate = txnDate;
        this.description = description;
    }

    public String getTxnId() {
        return txnId;
    }

    public void setTxnId(String txnId) {
        this.txnId = txnId;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public String getTxnType() {
        return txnType;
    }

    public void setTxnType(String txnType) {
        this.txnType = txnType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getTxnDate() {
        return txnDate;
    }

    public void setTxnDate(Instant txnDate) {
        this.txnDate = txnDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

