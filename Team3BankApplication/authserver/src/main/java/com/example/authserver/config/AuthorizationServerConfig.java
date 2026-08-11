package com.example.authserver.config;

import com.example.authserver.users.BankUser;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;

@Configuration
@EnableWebSecurity
public class AuthorizationServerConfig {

    // -------------------------------------------------------------------------
    // Scope sets, declared once at the top so the user definitions below
    // and the client registration both reference the same constants.
    // Keeping these in one place is the difference between a typo being
    // caught at compile time and a typo being caught by a confused student
    // staring at a 403 from the Resource Server.
    // -------------------------------------------------------------------------



    private static final Set<String> ACCOUNT_HOLDER_SCOPES = Set.of(
            "account.read",
            "account.write",
            "transaction.read",
            "transaction.create",
            "customer.read"
    );

    private static final Set<String> TELLER_SCOPES = Set.of(
            "account.read",
            "account.write",
            "account.create",
            "transaction.read",
            "transaction.create",
            "customer.read",
            "customer.write"
    );

    // Union of every scope any user can hold. The bank-spa client is
    // registered with this superset; the token customizer narrows the
    // scope claim per user at issue time.
    private static final Set<String> ALL_BANK_SCOPES = Set.of(
            "account.read",
            "account.write",
            "account.create",
            "transaction.read",
            "transaction.create",
            "customer.read",
            "customer.write"
    );

    // -------------------------------------------------------------------------
    // Filter Chain 1: Authorization Server protocol endpoints
    //
    // This chain handles the OAuth2 protocol endpoints:
    //   /oauth2/authorize  -- the authorization endpoint (redirects to login)
    //   /oauth2/token      -- the token endpoint (issues JWTs)
    //   /oauth2/jwks       -- the public key endpoint (Resource Servers fetch from here)
    //   /.well-known/...   -- discovery endpoints
    //
    // @Order(1) ensures this chain is evaluated before the login form chain below.
    // -------------------------------------------------------------------------
    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http)
            throws Exception {

        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);

        http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
                // Enable OpenID Connect 1.0. This adds the /userinfo endpoint
                // and id_token support alongside the standard access token.
                .oidc(Customizer.withDefaults());

        http
                // When an unauthenticated browser request arrives at the
                // authorization endpoint, redirect to the login form.
                .exceptionHandling(ex -> ex
                        .defaultAuthenticationEntryPointFor(
                                new LoginUrlAuthenticationEntryPoint("/login"),
                                new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
                        )
                )
                // Accept Bearer tokens for the /userinfo endpoint.
                .oauth2ResourceServer(rs -> rs.jwt(Customizer.withDefaults()));

        return http.build();
    }

    // -------------------------------------------------------------------------
    // Filter Chain 2: Default security for the login form
    //
    // @Order(2) means this chain is checked after the Authorization Server chain.
    // It provides the login form that users see when authenticating interactively.
    // -------------------------------------------------------------------------
    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http)
            throws Exception {
        http
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .formLogin(Customizer.withDefaults())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID"));
        return http.build();
    }

    // -------------------------------------------------------------------------
    // Password Encoder
    //
    // BCrypt is the production standard for password hashing. Even in a lab,
    // hashing the password is one line of code and removes a footgun from
    // the system: nobody will see the literal "password" string in logs,
    // stack traces, or memory dumps.
    // -------------------------------------------------------------------------
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    // -------------------------------------------------------------------------
    // Authentication Manager
    //
    // Explicitly configure the AuthenticationManager with the UserDetailsService
    // and PasswordEncoder to ensure form login works correctly.
    // -------------------------------------------------------------------------
    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return new org.springframework.security.authentication.ProviderManager(authProvider);
    }

    // -------------------------------------------------------------------------
    // Users
    //
    // Two roles and authentication types:
    //   - Customers: authenticate with customer_number (e.g., 487-978493)
    //   - Staff: authenticate with staff username (e.g., teller1)
    // All share the password "password" for lab convenience only.
    //
    // The role string drives two things:
    //   1. The Spring Security authority (ROLE_ACCOUNT_HOLDER, ROLE_TELLER)
    //      used by hasRole(...) in @PreAuthorize on the Resource Server.
    //   2. A "roles" claim added to the JWT by the token customizer.
    //
    // The allowedScopes field is the user's *maximum* scope set. The token
    // customizer intersects this with the scopes requested by the client at
    // authorize time, so a token never carries a scope its bearer was not
    // entitled to (principle of least privilege per token).
    //
    // IMPORTANT: we do NOT use InMemoryUserDetailsManager here, even though
    // it looks like the natural choice. InMemoryUserDetailsManager silently
    // wraps every UserDetails you give it and reconstructs a plain
    // org.springframework.security.core.userdetails.User on lookup -- your
    // BankUser subtype is stripped. The token customizer's
    // `instanceof BankUser` check would then always be false, and the JWT
    // would come out with sub=username, no roles, no name,
    // no preferred_username, and an unfiltered scope claim. The tiny lambda
    // UserDetailsService below hands the BankUser back unchanged.
    // -------------------------------------------------------------------------
    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder encoder) {
        String pwd = encoder.encode("password");   // LAB ONLY -- same password for all users

        Map<String, BankUser> users = Map.of(
                // Customers authenticate with customer_number (e.g., 487-978493)
                "487-978493",  new BankUser("487-978493",  "487-978493",  pwd, "Alice Nguyen",
                        "account_holder", ACCOUNT_HOLDER_SCOPES),
                "487-978494",  new BankUser("487-978494",  "487-978494",  pwd, "Bob Patel",
                        "account_holder", ACCOUNT_HOLDER_SCOPES),
                "487-978495",  new BankUser("487-978495",  "487-978495",  pwd, "Carla Romero",
                        "account_holder", ACCOUNT_HOLDER_SCOPES),
                // Staff authenticate with staff username (e.g., teller1)
                "teller1", new BankUser("teller1", "teller1", pwd, "Edward Teller",
                        "teller",         TELLER_SCOPES),
                "teller2", new BankUser("teller2", "teller2", pwd, "Teller Two",
                        "teller",         TELLER_SCOPES)
        );

        return username -> {
            BankUser user = users.get(username);
            if (user == null) {
                throw new UsernameNotFoundException("No such user: " + username);
            }
            return user;
        };
    }

    // -------------------------------------------------------------------------
    // Registered Clients
    //
    // A registered client is an application that has permission to request
    // tokens from this Authorization Server.
    //
    // bank-spa: a public client using Authorization Code + PKCE.
    //   Represents the React banking app you will build in a later lab.
    //   PKCE provides the security guarantee that a client secret would give.
    //   Registered with the union of all bank scopes; the token customizer
    //   narrows them per user.
    //
    // bank-service: a confidential client using Client Credentials.
    //   Represents a backend service (batch job, reconciliation worker)
    //   authenticating with its own identity. No user is involved; the
    //   service authenticates directly.
    // -------------------------------------------------------------------------
    @Bean
    public RegisteredClientRepository registeredClientRepository() {

        RegisteredClient.Builder spaBuilder = RegisteredClient
                .withId(UUID.randomUUID().toString())
                .clientId("bank-spa")
                // Public clients have no secret. PKCE takes its place.
                .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                // Direct access (Lab 4.6 standalone testing)
                .redirectUri("http://localhost:8080/login/oauth2/code/bank-auth")
                .redirectUri("http://localhost:5173/login/oauth2/code/bank-auth")
                .postLogoutRedirectUri("http://localhost:5173/")
                .scope(OidcScopes.OPENID)
                .scope(OidcScopes.PROFILE);

        // Add every bank scope so any user can request what they're entitled to.
        ALL_BANK_SCOPES.forEach(spaBuilder::scope);

        RegisteredClient bankSpa = spaBuilder
                .clientSettings(ClientSettings.builder()
                        // Require PKCE. The Authorization Server rejects any
                        // authorization request that does not include a code_challenge.
                        .requireProofKey(true)
                        // Show the consent screen so students can see scope approval.
                        .requireAuthorizationConsent(true)
                        .build())
                .tokenSettings(TokenSettings.builder()
                        // 5-minute access token lifetime. Intentionally short so the
                        // stale permissions scenario in later exercises is observable
                        // within a single lab session.
                        .accessTokenTimeToLive(Duration.ofMinutes(5))
                        .refreshTokenTimeToLive(Duration.ofDays(1))
                        .build())
                .build();

        RegisteredClient bankService = RegisteredClient
                .withId(UUID.randomUUID().toString())
                .clientId("bank-service")
                // {noop} tells Spring not to hash this value.
                // Development only; never use {noop} in production.
                .clientSecret("{noop}bank-service-secret")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .scope("account.read")
                .scope("transaction.read")
                .scope("transaction.create")
                .tokenSettings(TokenSettings.builder()
                        // Service tokens can have longer lifetimes because the
                        // service can re-authenticate silently at any time.
                        .accessTokenTimeToLive(Duration.ofHours(1))
                        .build())
                .build();

        RegisteredClient bankClientBff = RegisteredClient
                .withId(UUID.randomUUID().toString())
                .clientId("bank-client-bff")
                // {noop} tells Spring not to hash this value.
                // Development only; never use {noop} in production.
                .clientSecret("{noop}bank-client-bff-secret")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                // Authorization Code flow for user-facing login,
                // plus refresh tokens so the BFF can refresh access tokens silently.
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                // Where the auth server is allowed to redirect the user after login.
                // Spring Security on the BFF auto-exposes this URL pattern.
                .redirectUri("http://localhost:8080/login/oauth2/code/bank-auth")
                // Through Vite proxy (Lab 4.7 React integration)  <-- NEW
                .redirectUri("http://localhost:5173/login/oauth2/code/bank-auth")
                .postLogoutRedirectUri("http://localhost:8080/")
                .postLogoutRedirectUri("http://localhost:5173/")
                .scope(OidcScopes.OPENID)
                .scope(OidcScopes.PROFILE)
                // The full set of bank scopes. The token customizer narrows
                // these per user at issue time: customers get the account_holder subset;
                // staff members get the teller subset. Registering with the superset
                // lets any user log in via the BFF and receive a token appropriate to their role.
                .scope("account.read")
                .scope("account.write")
                .scope("account.create")
                .scope("transaction.read")
                .scope("transaction.create")
                .scope("customer.read")
                .scope("customer.write")
                .clientSettings(ClientSettings.builder()
                        // Skip the consent screen for this lab to keep the
                        // login flow short. In a real customer-facing app
                        // you would typically require consent.
                        .requireAuthorizationConsent(false)
                        .build())
                .tokenSettings(TokenSettings.builder()
                        // Longer-lived than bank-spa's 5-minute token because the
                        // BFF holds the token server-side; XSS cannot steal it,
                        // so a longer lifetime is acceptable.
                        .accessTokenTimeToLive(Duration.ofMinutes(60))
                        .refreshTokenTimeToLive(Duration.ofDays(1))
                        .build())
                .build();

        return new InMemoryRegisteredClientRepository(bankSpa, bankService, bankClientBff);
    }

    // -------------------------------------------------------------------------
    // Token Customizer
    //
    // Three jobs:
    //
    //   1. Replace "sub" with the stable domain identifier (customer or
    //      employee ID) instead of the login name. This is what the Resource
    //      Server's @PreAuthorize expressions key off via authentication.getName().
    //
    //   2. Add OIDC standard claims: "preferred_username" (login name) and
    //      "name" (full name). These are display-oriented and never used
    //      for authorization.
    //
    //   3. Add a "roles" claim derived from the user's role, and rewrite the
    //      "scope" claim to be the intersection of (requested scopes) and
    //      (user's allowedScopes). This enforces least privilege per token:
    //      a token never carries a scope its bearer is not entitled to,
    //      regardless of what the client requested.
    //
    // The customizer only runs for user-bearing tokens (Authorization Code
    // flow). Client Credentials tokens have no user principal; the condition
    // below is false and none of these claims are added. This is intentional
    // and is something later exercises ask you to observe directly.
    // -------------------------------------------------------------------------
    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer() {
        return context -> {
            var principal = context.getPrincipal();

            if (principal != null
                    && principal.getPrincipal() instanceof BankUser user) {

                // 1. Stable subject identifier.
                context.getClaims().subject(user.getSubjectId());

                // 2. OIDC display claims.
                context.getClaims().claim("preferred_username", user.getUsername());
                context.getClaims().claim("name", user.getFullName());

                // 3a. Roles claim (single-element list so future multi-role
                //     support is a one-line change).
                context.getClaims().claim("roles", List.of(user.getRole()));

                // 3b. Narrow the scope claim to (requested ∩ allowed).
                Set<String> requested = new HashSet<>(context.getAuthorizedScopes());
                Set<String> granted = requested.stream()
                        .filter(user.getAllowedScopes()::contains)
                        .collect(Collectors.toSet());

                // OIDC scopes (openid, profile) are not in allowedScopes but
                // should pass through if the client requested them.
                if (requested.contains(OidcScopes.OPENID)) granted.add(OidcScopes.OPENID);
                if (requested.contains(OidcScopes.PROFILE)) granted.add(OidcScopes.PROFILE);

                context.getClaims().claim("scope", String.join(" ", granted));
            }
        };
    }

    // -------------------------------------------------------------------------
    // RSA Key Pair
    //
    // The Authorization Server signs JWTs with the private key.
    // Resource Servers verify JWTs using the public key, fetched from:
    //   http://127.0.0.1:9000/oauth2/jwks
    //
    // A new key pair is generated in memory on each startup. Any token issued
    // before a restart is immediately invalid because the new public key will
    // not match the old signature. This is expected in development and is
    // something the checkpoints ask you to reason about.
    //
    // In production the key pair is generated once, stored securely, and
    // rotated deliberately with advance notice to all Resource Servers.
    // -------------------------------------------------------------------------
    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        KeyPair keyPair = generateRsaKey();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                // The kid (Key ID) appears in every JWT header.
                // Resource Servers use it to select the correct verification
                // key from the JWKS when multiple keys are present.
                .keyID(UUID.randomUUID().toString())
                .build();

        return new ImmutableJWKSet<>(new JWKSet(rsaKey));
    }

    private static KeyPair generateRsaKey() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to generate RSA key pair", ex);
        }
    }

    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    // -------------------------------------------------------------------------
    // Authorization Server Settings
    //
    // The issuer URI is embedded in the "iss" claim of every token this
    // server issues. Resource Servers are configured with this URI and reject
    // any token where "iss" does not match. This prevents a token issued for
    // one system from being replayed against a different system.
    //
    // We use the default builder here. The issuer is configured in
    // application.yml via spring.security.oauth2.authorizationserver.issuer.
    // Putting it there (rather than hardcoding it in Java) means it is set
    // in one place, can be overridden per environment without recompiling,
    // and cannot accidentally diverge from the property file.
    // -------------------------------------------------------------------------
    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder().build();
    }
}