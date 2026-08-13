package com.example.bankapi.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;

// @JsonInclude(NON_NULL) tells Jackson not to serialize fields that are null.
// The "fieldErrors" field is only relevant for validation failures;
// it should be absent from the JSON for other error types.
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {

    private final String status;       // e.g. "404 NOT_FOUND"
    private final String errorCode;    // machine-readable code, e.g. "ACCOUNT_NOT_FOUND"
    private final String message;      // human-readable description
    private final Instant timestamp;
    private final List<FieldError> fieldErrors;  // non-null only for 400 validation failures

    // TODO 10: Write the constructor that accepts four parameters
    // (status, errorCode, message, fieldErrors) and assigns them.
    // Set timestamp to Instant.now() inside the constructor.

    public ApiError(String status, String errorCode, String message, List<FieldError> fieldErrors) {
        this.status = status;
        this.errorCode = errorCode;
        this.message = message;
        this.fieldErrors = fieldErrors;
        this.timestamp = Instant.now();
    }

    // Getters -- required for Jackson serialization
    public String getStatus() { return status; }
    public String getErrorCode() { return errorCode; }
    public String getMessage() { return message; }
    public Instant getTimestamp() { return timestamp; }
    public List<FieldError> getFieldErrors() { return fieldErrors; }

    // Nested record for per-field validation errors
    public record FieldError(String field, String message) {}
}