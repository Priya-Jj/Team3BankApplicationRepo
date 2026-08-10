package com.example.bankapi.model;

public record Customer(
        String id,           // e.g. "C001" -- matches the sub claim of the corresponding user
        String fullName,
        String email
) {}
