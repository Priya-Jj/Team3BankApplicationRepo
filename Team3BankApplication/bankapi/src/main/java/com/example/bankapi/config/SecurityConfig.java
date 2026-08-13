package com.example.bankapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter jwtAuthConverter) throws Exception {

        http
                // TODO 2: Configure authorization rules using authorizeHttpRequests().
                // Rules:
                //   - GET  /health                       -- permitAll()
                //   - GET  /api/v1/accounts/**           -- hasAuthority("SCOPE_account.read")
                //   - POST /api/v1/accounts              -- hasAuthority("SCOPE_account.create")
                //   - anyRequest()                       -- authenticated()
                // Rules are evaluated in order. Specific rules must come before general ones.
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/health").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/accounts/**").hasAuthority("SCOPE_account.read")
                        .requestMatchers(HttpMethod.POST, "/api/v1/accounts").hasAuthority("SCOPE_account.create")
                        .requestMatchers(HttpMethod.POST, "/api/v1/transfers").hasAuthority("SCOPE_transaction.create")
                        .anyRequest().authenticated()
                )



        // TODO 3: Configure the OAuth2 Resource Server to validate JWT Bearer tokens.
        // Use: .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
        // This activates JWT extraction, JWKS key fetching, signature verification,
        // and claims-to-authorities mapping. Spring Security calls the jwks-uri
        // configured in application.yml to fetch the verification keys.

                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter)))

        // TODO 4: Configure session management to STATELESS.
        // Use: .sessionManagement(session -> session
        //          .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        // A REST API using Bearer tokens has no need for HTTP sessions.

                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

        // TODO 5: Disable CSRF protection.
        // Use: .csrf(csrf -> csrf.disable())
        // CSRF attacks rely on browsers automatically sending session cookies.
        // Bearer tokens are not sent automatically, so CSRF does not apply here.

                .csrf(AbstractHttpConfigurer::disable)
        ;

        return http.build();
    }
}
