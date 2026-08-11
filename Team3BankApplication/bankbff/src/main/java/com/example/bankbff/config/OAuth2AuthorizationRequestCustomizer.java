package com.example.bankbff.config;

import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Custom OAuth2 authorization request resolver that adds the "prompt=login"
 * parameter to force the auth server to always show the login screen, even if
 * a previous session exists. This ensures that multiple users can log in
 * sequentially without server-side session cache interference.
 */
@Component
public class OAuth2AuthorizationRequestCustomizer implements OAuth2AuthorizationRequestResolver {

    private final DefaultOAuth2AuthorizationRequestResolver defaultResolver;

    public OAuth2AuthorizationRequestCustomizer(ClientRegistrationRepository clientRegistrationRepository) {
        this.defaultResolver = new DefaultOAuth2AuthorizationRequestResolver(
                clientRegistrationRepository,
                "/oauth2/authorization");
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        OAuth2AuthorizationRequest authorizationRequest = defaultResolver.resolve(request);
        if (authorizationRequest != null) {
            return customizeAuthorizationRequest(authorizationRequest);
        }
        return null;
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        OAuth2AuthorizationRequest authorizationRequest = defaultResolver.resolve(request, clientRegistrationId);
        if (authorizationRequest != null) {
            return customizeAuthorizationRequest(authorizationRequest);
        }
        return null;
    }

    private OAuth2AuthorizationRequest customizeAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest) {
        return OAuth2AuthorizationRequest.from(authorizationRequest)
                .additionalParameters(params -> params.put("prompt", "login"))
                .build();
    }
}
