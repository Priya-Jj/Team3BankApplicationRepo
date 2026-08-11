package com.example.bankbff.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Logout success handler that:
 * 1. Clears session cookies
 * 2. Uses OIDC logout to redirect to auth server
 * 3. Auth server logs out and redirects back to home
 */
public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {

    private final OidcClientInitiatedLogoutSuccessHandler oidcLogoutSuccessHandler;

    public CustomLogoutSuccessHandler(ClientRegistrationRepository clientRegistrationRepository) {
        this.oidcLogoutSuccessHandler = new OidcClientInitiatedLogoutSuccessHandler(clientRegistrationRepository);
        this.oidcLogoutSuccessHandler.setPostLogoutRedirectUri("{baseUrl}/");
    }

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
                                Authentication authentication) throws IOException, ServletException {
        // Clear BFF session cookies first
        clearCookie(response, "BFF_SESSION");
        clearCookie(response, "JSESSIONID");

        // Disable caching
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");

        // Use OIDC logout handler to redirect to auth server
        oidcLogoutSuccessHandler.onLogoutSuccess(request, response, authentication);
    }

    private void clearCookie(HttpServletResponse response, String cookieName) {
        Cookie cookie = new Cookie(cookieName, "");
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        response.addCookie(cookie);
    }
}