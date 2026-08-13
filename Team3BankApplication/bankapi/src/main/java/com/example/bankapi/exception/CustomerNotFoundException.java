package com.example.bankapi.exception;

public class CustomerNotFoundException extends ResourceNotFoundException {
    public CustomerNotFoundException(String customerNumber) {
        super("Customer ", customerNumber);
    }
}
