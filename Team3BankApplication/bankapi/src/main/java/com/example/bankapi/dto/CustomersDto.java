package com.example.bankapi.dto;

import java.time.Instant;

public class CustomersDto {
    private Long customerId;
    private String customerNumber;
    private String fullName;
    private String email;
    private Instant createdDate;

    public CustomersDto() {
    }

    public CustomersDto(Long customerId, String customerNumber, String fullName, String email, Instant createdDate) {
        this.customerId = customerId;
        this.customerNumber = customerNumber;
        this.fullName = fullName;
        this.email = email;
        this.createdDate = createdDate;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getCustomerNumber() {
        return customerNumber;
    }

    public void setCustomerNumber(String customerNumber) {
        this.customerNumber = customerNumber;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }
}

