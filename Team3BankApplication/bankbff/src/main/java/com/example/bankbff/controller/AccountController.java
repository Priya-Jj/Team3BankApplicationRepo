package com.example.bankbff.controller;

import com.example.bankbff.client.BankingApiClient;
import com.example.bankbff.dto.AccountDto;
import com.example.bankbff.dto.DepositRequestDto;
import com.example.bankbff.dto.CashTransactionRequestDto;
import com.example.bankbff.dto.TransferRequestDto;
import com.example.bankbff.dto.TransferResponseDto;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
/**
 * Account-proxy controller.
 *
 * Forwards browser requests to bankapi. The access token is attached
 * automatically by the WebClient filter.
 */
@RestController
@RequestMapping("/api")
public class AccountController {

    private final BankingApiClient bankingApiClient;

    public AccountController(BankingApiClient bankingApiClient) {
        this.bankingApiClient = bankingApiClient;
    }

    @GetMapping("/accounts")
    public List<AccountDto> accounts() {
        return bankingApiClient.getAccounts();
    }

    @GetMapping("/accounts/{customerNumber}")
    public List<AccountDto> accounts(@PathVariable String customerNumber) {
        return bankingApiClient.getAccountsByCustomerNumber(customerNumber);
    }

    @PostMapping("/transfers")
    public TransferResponseDto transfer(@RequestBody TransferRequestDto request) {
        // TODO 7.3: Return bankingApiClient.postTransfer(request).
        return bankingApiClient.postTransfer(request);
    }

    @PostMapping("/accounts/{accountId}/transactions")
    public TransferResponseDto cashTransaction(@PathVariable String accountId, @RequestBody CashTransactionRequestDto request) {
        return bankingApiClient.postCashTransaction(accountId, request);
    }

    @PostMapping("/accounts/{accountId}/deposits")
    public Map<String, String> deposit(@PathVariable String accountId, @RequestBody DepositRequestDto request) {

        return bankingApiClient.postDeposit(accountId, request);
    }
}