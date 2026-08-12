package com.example.bankbff.entity;

import com.example.bankbff.dto.AccountDto;

public class AccountDtoContext {
    private final AccountDto accountDto;

    public AccountDtoContext(AccountDto accountDto) {
        this.accountDto = accountDto;
    }

    public AccountDto getAccountDto() {
        return accountDto;
    }
}

