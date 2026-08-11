package com.example.bankapi.dto;

import java.time.Instant;

public class TransfersDto {
    private String transferId;
    private String debitTxnId;
    private String creditTxnId;
    private Instant createdDate;

    public TransfersDto() {
    }

    public TransfersDto(String transferId, String debitTxnId, String creditTxnId, Instant createdDate) {
        this.transferId = transferId;
        this.debitTxnId = debitTxnId;
        this.creditTxnId = creditTxnId;
        this.createdDate = createdDate;
    }

    public String getTransferId() {
        return transferId;
    }

    public void setTransferId(String transferId) {
        this.transferId = transferId;
    }

    public String getDebitTxnId() {
        return debitTxnId;
    }

    public void setDebitTxnId(String debitTxnId) {
        this.debitTxnId = debitTxnId;
    }

    public String getCreditTxnId() {
        return creditTxnId;
    }

    public void setCreditTxnId(String creditTxnId) {
        this.creditTxnId = creditTxnId;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }
}

