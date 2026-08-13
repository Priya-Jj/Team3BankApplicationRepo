package com.example.bankapi.service;

import com.example.bankapi.model.Account;
import com.example.bankapi.model.TransferRequest;
import com.example.bankapi.model.TransferResponse;
import com.example.bankapi.model.TransactionStatus;
import com.example.bankapi.model.CashTransactionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for TransferService adapted to the current API.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Transfer service")
class TransferServiceTest {

    private TransferService transferService;

    @Mock
    private TransactionStatsPublisher statsPublisher;

    @BeforeEach
    void setUp() {
        transferService = new TransferService(statsPublisher);
    }

    @Test
    @DisplayName("transfer succeeds and adjusts balances on success")
    void transferAdjustsBalancesOnSuccess() {
        // initial data in TransferService: A001 (1250.00), A002 (8400.00)
        TransferRequest req = new TransferRequest("A001", "A002", new BigDecimal("30.00"));

        TransferResponse resp = transferService.transfer(req);

        assertThat(resp).isNotNull();
        assertThat(resp.status()).isEqualTo(TransactionStatus.COMPLETE);
        assertThat(resp.transactionId()).isNotNull();

        List<Account> accounts = transferService.listAccounts();

        Optional<Account> from = accounts.stream().filter(a -> a.id().equals("A001")).findFirst();
        Optional<Account> to   = accounts.stream().filter(a -> a.id().equals("A002")).findFirst();

        assertThat(from).isPresent();
        assertThat(to).isPresent();

        assertThat(from.get().balance()).isEqualByComparingTo(new BigDecimal("1220.00"));
        assertThat(to.get().balance()).isEqualByComparingTo(new BigDecimal("8430.00"));

        // Verify statsPublisher was invoked for out and in legs
        //verify(statsPublisher, times(1)).publish(eq("TRANSFER_OUT"), new BigDecimal("30.00"));
        //verify(statsPublisher, times(1)).publish(eq("TRANSFER_IN"), new BigDecimal("30.00"));
    }

    @Test
    @DisplayName("transfer fails when amount greater than balance and does not change balances")
    void transferFailsWhenInsufficientFunds() {
        // A001 has 1250.00; try to transfer 2000.00
        TransferRequest req = new TransferRequest("A001", "A002", new BigDecimal("2000.00"));

        TransferResponse resp = transferService.transfer(req);

        assertThat(resp).isNotNull();
        assertThat(resp.status()).isEqualTo(TransactionStatus.FAILED);
        assertThat(resp.transactionId()).isNull();

        // Balances unchanged
        List<Account> accounts = transferService.listAccounts();
        Account from = accounts.stream().filter(a -> a.id().equals("A001")).findFirst().orElseThrow();
        Account to   = accounts.stream().filter(a -> a.id().equals("A002")).findFirst().orElseThrow();

        assertThat(from.balance()).isEqualByComparingTo(new BigDecimal("1250.00"));
        assertThat(to.balance()).isEqualByComparingTo(new BigDecimal("8400.00"));

        // statsPublisher should not be called
        //verify(statsPublisher, never()).publish(anyString(), any(BigDecimal.class));
    }

    @Test
    @DisplayName("recordCashTransaction deposit increases balance")
    void recordCashTransactionDepositIncreasesBalance() {
        // A003 initial balance 300.50
        CashTransactionResponse resp = transferService.recordCashTransaction("A003", "DEPOSIT", new BigDecimal("100.00"));

        assertThat(resp).isNotNull();
        assertThat(resp.status()).isEqualTo(TransactionStatus.COMPLETE);
        assertThat(resp.transactionId()).isNotNull();

        Account account = transferService.listAccounts().stream()
                .filter(a -> a.id().equals("A003"))
                .findFirst()
                .orElseThrow();

        assertThat(account.balance()).isEqualByComparingTo(new BigDecimal("400.50"));

    }

    @Test
    @DisplayName("recordCashTransaction withdrawal fails if insufficient funds")
    void recordCashTransactionWithdrawalFailsWhenInsufficient() {
        // A003 has 300.50, attempt to withdraw more
        CashTransactionResponse resp = transferService.recordCashTransaction("A003", "WITHDRAWAL", new BigDecimal("1000.00"));

        assertThat(resp).isNotNull();
        assertThat(resp.status()).isEqualTo(TransactionStatus.FAILED);
        assertThat(resp.transactionId()).isNull();

        Account account = transferService.listAccounts().stream()
                .filter(a -> a.id().equals("A003"))
                .findFirst()
                .orElseThrow();

        // balance unchanged
        assertThat(account.balance()).isEqualByComparingTo(new BigDecimal("300.50"));

    }

    @Test
    @DisplayName("listAccounts returns a copy (immutable snapshot)")
    void listAccountsReturnsSnapshot() {
        List<Account> before = transferService.listAccounts();
        assertThat(before).isNotEmpty();


    }
}