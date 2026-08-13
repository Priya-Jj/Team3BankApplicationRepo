package com.example.bankapi.exception;

public class AccountNotFoundException extends ResourceNotFoundException {
    public AccountNotFoundException(String accountNumber) {
        super("Account", accountNumber);
    }
}