package com.example.bankbff.controller;

import com.example.bankbff.dto.UserInfoDto;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * User-info controller.
 *
 * The SPA calls /api/me on load to determine whether the user is logged in
 * and to display name and role. Returns 401 (handled by the security filter
 * chain) if the user is not authenticated.
 */
@RestController
@RequestMapping("/api")
public class UserController {

    @GetMapping("/me")
    public UserInfoDto me(@AuthenticationPrincipal OidcUser principal) {
        // TODO 7.1: Build a UserInfoDto from the OidcUser principal.
        //
        // OidcUser exposes the claims from the ID token. Use:
        //   principal.getSubject()                                   - the sub claim
        //   principal.getPreferredUsername()                         - login name
        //   principal.getFullName()                                  - the "name" claim
        //   principal.getClaimAsStringList("roles")                  - the roles claim
        //
        // If "roles" is missing, default to an empty list.
        //
           List<String> roles = principal.getClaimAsStringList("roles");
           if (roles == null) roles = List.of();
           return new UserInfoDto(
                   principal.getSubject(),
                   principal.getPreferredUsername(),
                   principal.getFullName(),
                   roles);
    }
}