package com.example.bankbff.dto;

import java.util.List;

/**
 * Information about the authenticated user, exposed via /api/me.
 *
 * The SPA calls /api/me on load to determine whether the user is logged in
 * and to display the user's name and role. The fields come from the OAuth2
 * authenticated principal and the access token claims.
 *
 *   subject:           the JWT's sub claim. From Lab 2-1 this is the
 *                      customer_number (487-978493) or staff username (teller1).
 *   preferredUsername: the login name (customer_number or staff username).
 *   fullName:          the user's display name from the token.
 *   roles:             the user's roles (account_holder, teller).
 */
public record UserInfoDto(
        String subject,
        String preferredUsername,
        String fullName,
        List<String> roles
) {}