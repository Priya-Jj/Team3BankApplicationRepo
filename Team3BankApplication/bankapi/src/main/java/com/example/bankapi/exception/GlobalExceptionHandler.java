package com.example.bankapi.exception;

import com.example.bankapi.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

// @RestControllerAdvice = @ControllerAdvice + @ResponseBody.
// Every method in this class can return a plain object and Spring will
// serialize it to JSON, just like @RestController does for normal endpoints.
// This class applies to all controllers in the application by default.
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Handles ResourceNotFoundException (and its subclasses like
    // AccountNotFoundException) -- returns 404
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<com.example.bankapi.exception.ApiError> handleNotFound(ResourceNotFoundException ex) {
        // TODO 11: Build an ApiError with:
        //   status = "404 NOT_FOUND"
        //   errorCode = ex.getResourceType().toUpperCase() + "_NOT_FOUND"
        //              (e.g. "ACCOUNT_NOT_FOUND" for an AccountNotFoundException)
        //   message = ex.getMessage()
        //   fieldErrors = null
        // Return ResponseEntity with status HttpStatus.NOT_FOUND and the ApiError as the body.
        com.example.bankapi.exception.ApiError error = new com.example.bankapi.exception.ApiError("404 NOT_FOUND", ex.getResourceType().toUpperCase() + "_NOT_FOUND", ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    // Handles BusinessRuleException -- returns 422
    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<com.example.bankapi.exception.ApiError> handleBusinessRule(BusinessRuleException ex) {
        // TODO 12: Build an ApiError with:
        //   status = "422 UNPROCESSABLE_ENTITY"
        //   errorCode = ex.getErrorCode()      (e.g. "INSUFFICIENT_FUNDS", "ACCOUNT_NOT_OPEN")
        //   message = ex.getMessage()
        //   fieldErrors = null
        // Return ResponseEntity with status HttpStatus.UNPROCESSABLE_ENTITY.
        com.example.bankapi.exception.ApiError error = new com.example.bankapi.exception.ApiError("422 UNPROCESSABLE_ENTITY", ex.getErrorCode(), ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(error);
    }

    // Handles validation failures from @Valid -- returns 400
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<com.example.bankapi.exception.ApiError> handleValidation(MethodArgumentNotValidException ex) {
        List<com.example.bankapi.exception.ApiError.FieldError> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fe -> new com.example.bankapi.exception.ApiError.FieldError(fe.getField(), fe.getDefaultMessage()))
                .collect(Collectors.toList());

        com.example.bankapi.exception.ApiError error = new com.example.bankapi.exception.ApiError(
                "400 BAD_REQUEST",
                "VALIDATION_FAILURE",
                "One or more fields failed validation",
                fieldErrors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // Catch-all for any unhandled exception -- returns 500
    // Security note: the response body intentionally contains no stack trace,
    // no exception class name, and no internal message.
    // Those details are available in the server logs, not in the HTTP response.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<com.example.bankapi.exception.ApiError> handleUnexpected(Exception ex) {
        // TODO 13: Build an ApiError with:
        //   status = "500 INTERNAL_SERVER_ERROR"
        //   errorCode = "INTERNAL_ERROR"
        //   message = "An unexpected error occurred. Please contact support."
        //   fieldErrors = null
        // Log the real exception (System.err is fine for now -- a real app uses SLF4J).
        // Return ResponseEntity with status HttpStatus.INTERNAL_SERVER_ERROR.
        com.example.bankapi.exception.ApiError error = new com.example.bankapi.exception.ApiError("500 INTERNAL_SERVER_ERROR", "INTERNAL_ERROR", "An unexpected error occurred. Please contact support.", null);
        System.err.println("Unexpected error occurred:");
        ex.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}