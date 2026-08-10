package com.example.authserver.users;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * A UserDetails implementation that carries the domain attributes
 * this Authorization Server needs to put into the JWT:
 *
 *   subjectId      -- becomes the "sub" claim (C001, EM01, AUD01, ...)
 *   username       -- the login name (alice, edward, ...) -> "preferred_username"
 *   fullName       -- display name -> "name"
 *   role           -- single role like "account_holder", "teller", "auditor"
 *   allowedScopes  -- the maximum scope set this user is entitled to
 *
 * The token customizer pulls these directly off the principal at JWT
 * encoding time. Storing them here (rather than in a parallel map)
 * keeps the user's identity and the user's authorization data
 * in one place.
 */
public class BankUser implements UserDetails {

    private final String subjectId;
    private final String username;
    private final String password;
    private final String fullName;
    private final String role;
    private final Set<String> allowedScopes;

    public BankUser(String subjectId,
                    String username,
                    String password,
                    String fullName,
                    String role,
                    Set<String> allowedScopes) {
        this.subjectId = subjectId;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
        this.allowedScopes = Set.copyOf(allowedScopes);
    }

    public String getSubjectId() {
        return subjectId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getRole() {
        return role;
    }

    public Set<String> getAllowedScopes() {
        return allowedScopes;
    }

    // UserDetails contract below.
    // Spring Security uses ROLE_ as a prefix convention so that hasRole('TELLER')
    // matches an authority named ROLE_TELLER. We follow that convention here.

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
    }

    @Override public String getPassword() { return password; }
    @Override public String getUsername() { return username; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}