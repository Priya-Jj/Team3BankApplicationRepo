package com.example.bankbff.client;

import com.example.bankbff.dto.AccountDto;
import com.example.bankbff.dto.TransferRequestDto;
import com.example.bankbff.dto.TransferResponseDto;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

/**
 * Client for the bankapi resource server.
 *
 * Uses the OAuth-configured WebClient. The access token is attached
 * automatically by the ServletOAuth2AuthorizedClientExchangeFilterFunction
 * configured in WebClientConfig.
 */
@Component
public class BankingApiClient {

    private final WebClient bankApiWebClient;

    public BankingApiClient(WebClient bankApiWebClient) {
        this.bankApiWebClient = bankApiWebClient;
    }

    public List<AccountDto> getAccounts() {
        // TODO 6.1: GET /api/v1/accounts and deserialize to List<AccountDto>.
        // Use ParameterizedTypeReference<List<AccountDto>>() {} for the body type.
        //
           return bankApiWebClient.get()
                   .uri("/api/v1/accounts")
                   .retrieve()
        .bodyToMono(new ParameterizedTypeReference<List<AccountDto>>() {})
        .block();
    }

    public List<AccountDto> getAccountsByCustomerNumber(String customerNumber) {
        // TODO 6.1: GET /api/v1/accounts and deserialize to List<AccountDto>.
        // Use ParameterizedTypeReference<List<AccountDto>>() {} for the body type.
        //
        return bankApiWebClient.get()
                .uri("/api/v1/accounts/{customerNumber}", customerNumber)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<AccountDto>>() {})
                .block();
    }

    public TransferResponseDto postTransfer(TransferRequestDto request) {
        // TODO 6.2: POST /api/v1/transfers with the given request body and
        // return the response as a TransferResponseDto.
        //
        return bankApiWebClient.post()
        .uri("/api/v1/transfers")
                   .bodyValue(request)
                   .retrieve()
                   .bodyToMono(TransferResponseDto.class)
                   .block();
    }
}