package com.example.bankbff.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configures the WebClient used to call the bankapi resource server.
 *
 * The WebClient is wired with the Spring Security OAuth2 filter, which:
 *   - looks up the current authenticated user's OAuth2AuthorizedClient
 *   - extracts the access token
 *   - attaches it as a Bearer token on every outgoing request
 *   - refreshes the token if it has expired
 *
 * Calling code does not need to know any of this. From its perspective,
 * webClient.get().uri("/api/v1/accounts").retrieve()... is all you write.
 */
@Configuration
public class WebClientConfig {

    @Bean
    public WebClient bankApiWebClient(
            OAuth2AuthorizedClientManager authorizedClientManager,
            @Value("${banking.resource-server.base-url}") String baseUrl) {

        // TODO 5.1: Create a ServletOAuth2AuthorizedClientExchangeFilterFunction
        // using the supplied authorizedClientManager. Call its
        // setDefaultClientRegistrationId("bank-auth") method so it uses the
        // BFF's client registration (the one configured in application.yml).
        //
        var oauth2Filter = new ServletOAuth2AuthorizedClientExchangeFilterFunction(
        authorizedClientManager);
        oauth2Filter.setDefaultClientRegistrationId("bank-auth");

        // TODO 5.2: Build the WebClient. Set the base URL and apply the filter.
        //
        return WebClient.builder()
        .baseUrl(baseUrl)
        .apply(oauth2Filter.oauth2Configuration())
        .build();
    }
}