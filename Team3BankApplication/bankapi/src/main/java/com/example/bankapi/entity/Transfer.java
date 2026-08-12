package com.example.bankapi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "transfers")
public class Transfer {

    @Id
    @Column(name = "transfer_id", length = 36)
    private String id;

    @OneToOne
    @JoinColumn(name = "debit_txn_id", referencedColumnName = "txn_id", nullable = false)
    private Transaction debitTransaction;

    @OneToOne
    @JoinColumn(name = "credit_txn_id", referencedColumnName = "txn_id", nullable = false)
    private Transaction creditTransaction;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    public Transfer() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Transaction getDebitTransaction() {
        return debitTransaction;
    }

    public void setDebitTransaction(Transaction debitTransaction) {
        this.debitTransaction = debitTransaction;
    }

    public Transaction getCreditTransaction() {
        return creditTransaction;
    }

    public void setCreditTransaction(Transaction creditTransaction) {
        this.creditTransaction = creditTransaction;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }
}

