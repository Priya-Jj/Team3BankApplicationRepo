package com.example.bankapi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class DownstreamClientConfig {

    @Value("${downstream.api.base-url:http://localhost:8080}")
    private String downstreamBaseUrl;

    /**
     * Creates a WebClient that automatically acquires and attaches a Bearer token
     * using Client Credentials before every outbound request.
     *
     * The OAuth2AuthorizedClientManager is auto-configured by Spring Boot
     * from the spring.security.oauth2.client.* properties above.
     * It handles token acquisition, caching, and refresh automatically.
     */
    @Bean
    public WebClient downstreamApiClient(OAuth2AuthorizedClientManager authorizedClientManager) {

        // TODO 22: Create a ServletOAuth2AuthorizedClientExchangeFilterFunction,
        // passing the authorizedClientManager to its constructor.
        // Call setDefaultClientRegistrationId("downstream-api") on the filter.
        // Build and return a WebClient using WebClient.builder()
        // with the baseUrl set to downstreamBaseUrl
        // and the filter applied with .apply(oauth2Filter.oauth2Configuration()).

        var oauth2Filter = new ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);
        oauth2Filter.setDefaultClientRegistrationId("downstream-api");

        return WebClient.builder()
                .baseUrl(downstreamBaseUrl)
                .apply(oauth2Filter.oauth2Configuration())
                .build();
    }
}