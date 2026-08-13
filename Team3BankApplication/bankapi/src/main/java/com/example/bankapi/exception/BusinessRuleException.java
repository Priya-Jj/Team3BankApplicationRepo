package com.example.bankapi.exception;

// Thrown when a request is syntactically valid but violates a business rule.
// This maps to 422 Unprocessable Entity, not 400 Bad Request.
// 400 means the request could not be understood.
// 422 means the request was understood but the domain rejected it.
public class BusinessRuleException extends RuntimeException {

    private final String errorCode;

    public BusinessRuleException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() { return errorCode; }
}