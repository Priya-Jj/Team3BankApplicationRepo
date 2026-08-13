package com.example.bankapi.service;

import com.example.bankapi.dto.AccountsDto;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.List;

@Service
public class DownstreamAccountService {

    private final WebClient downstreamApiClient;
    private final WebClient webClient;

    public DownstreamAccountService(WebClient downstreamApiClient, WebClient webClient) {
        this.downstreamApiClient = downstreamApiClient;
        this.webClient = webClient;
    }

    /**
     * Calls the account API using a Client Credentials token.
     * Spring Security acquires the token from the Authorization Server automatically.
     * The token represents this service's identity -- no user token is forwarded.
     */
    public List<AccountsDto> fetchAllFromDownstream() {
        // TODO 23: Use the WebClient to call GET /api/v1/accounts.
        // Chain: .get().uri("/api/v1/accounts").retrieve()
        //        .bodyToFlux(AccountsDto.class).collectList().block()
        // The OAuth2 filter calls the Authorization Server's token endpoint,
        // caches the token, and adds it to the Authorization header automatically.
        return webClient.get().uri("/api/v1/accounts").retrieve().bodyToFlux(AccountsDto.class).collectList().block();
    }
}