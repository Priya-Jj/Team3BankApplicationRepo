package com.example.bankbff.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.AnyRequestMatcher;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

/**
 * BFF security configuration.
 *
 *  - Require authentication for /api/** endpoints.
 *  - Allow /oauth2/** and /login/** unauthenticated so the OAuth flow can complete.
 *  - Return 401 for JSON requests, redirect to login for browser navigation.
 *  - CSRF disabled in this lab. Lab 4.7 will turn it on with a cookie-based token repository.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/oauth2/**", "/login/**").permitAll()
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().authenticated())

                .exceptionHandling(ex -> {
                    // Match an explicit "application/json" Accept header, not a browser's "*/*".
                    MediaTypeRequestMatcher jsonMatcher =
                            new MediaTypeRequestMatcher(MediaType.APPLICATION_JSON);
                    jsonMatcher.setUseEquals(true);
                    ex
                            // JSON clients (the SPA's fetch) get a clean 401.
                            .defaultAuthenticationEntryPointFor(
                                    new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                                    jsonMatcher)
                            // Everything else (a browser) is redirected into the OAuth login flow.
                            .defaultAuthenticationEntryPointFor(
                                    new LoginUrlAuthenticationEntryPoint("/oauth2/authorization/bank-auth"),
                                    AnyRequestMatcher.INSTANCE);
                })

                .oauth2Login(Customizer.withDefaults())

                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true))

                .csrf(AbstractHttpConfigurer::disable);

        return http.build();
    }
}