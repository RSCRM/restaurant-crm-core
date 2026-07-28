package com.restaurant.crm.common.config;

import com.restaurant.crm.common.constant.JwtClaimSetConstant;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import javax.crypto.spec.SecretKeySpec;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    private static final String TOKEN_TYPE_CONTEXT = "CONTEXT";
    private static final String TOKEN_TYPE_CUSTOMER_SESSION = "CUSTOMER_SESSION";
    private static final String ROLE_CUSTOMER_SESSION = "ROLE_CUSTOMER_SESSION";

    private final String[] PUBLIC_POST_ENDPOINT = {
            "/api/v1/orders", // Also allow customers to place orders without token
            "/api/v1/users",
            "/api/v1/auth/login",
            "/api/v1/auth/introspect",
            "/api/v1/auth/register",
            "/api/v1/crm/customers/identify", // Make customer identification public
            // uc-c-02 — QR table ordering (exact match, no wildcard)
            "/api/v1/public/customer/qr/resolve",
            "/api/v1/public/customer/qr/session",
            "/api/v1/public/customer/qr/session/join",
            // uc-c-03 — phone + OTP identification
            "/api/v1/public/customer/otp/request",
            "/api/v1/public/customer/otp/verify"
    };

    private final String[] PUBLIC_GET_ENDPOINT = {
            "/api/v1/orders/*/cooking-status",
            "/api/v1/orders/tables/*/active-order/cooking-status",
            "/api/v1/orders/*/cooking-status/subscribe",
            "/api/v1/orders/*/bill"
    };

    private static final String[] WHITELIST_ENDPOINTS = {
            "/swagger-ui",
            "/swagger-ui/**",
            "/api/v1/api-docs",
            "/api/v1/api-docs/**"
    };

    @NonFinal
    @Value(value = "${security.jwt.signer-key}")
    private String SIGNER_KEY;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity,
                                                    CorsConfigurationSource corsConfigurationSource,
                                                    JwtBlacklistFilter jwtBlacklistFilter) throws Exception {
        return httpSecurity
                //disable session
                .sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.ALWAYS))

                //cors config
                .cors(cors -> cors.configurationSource(corsConfigurationSource))

                //disable csrf
                .csrf(AbstractHttpConfigurer::disable)

                //authorization rules
                .authorizeHttpRequests(request -> request
                        .requestMatchers(HttpMethod.POST, PUBLIC_POST_ENDPOINT).permitAll()
                        .requestMatchers(HttpMethod.GET, PUBLIC_GET_ENDPOINT).permitAll()
                        .requestMatchers(WHITELIST_ENDPOINTS).permitAll()
                        .anyRequest().authenticated()
                )

                //check token blacklist before authentication
                .addFilterBefore(jwtBlacklistFilter, UsernamePasswordAuthenticationFilter.class)

                //config oauth2 resource server
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwtConfigurer -> jwtConfigurer.decoder(jwtDecoder())
                                .jwtAuthenticationConverter(jwtAuthenticationConverter()))
                        .authenticationEntryPoint(new JwtAuthenticationEntryPoint())
                )

                .build();
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Collection<GrantedAuthority> authorities = new HashSet<>();

            String tokenType = jwt.getClaimAsString(JwtClaimSetConstant.CLAIM_TYPE);

            if (TOKEN_TYPE_CUSTOMER_SESSION.equals(tokenType)) {
                // Customer session token (uc-c-02): single authority, no staff roles/permissions.
                authorities.add(new SimpleGrantedAuthority(ROLE_CUSTOMER_SESSION));
            } else if (TOKEN_TYPE_CONTEXT.equals(tokenType)) {
                // Context Token: extract orgRole + permissions
                String orgRole = jwt.getClaimAsString(JwtClaimSetConstant.CLAIM_ORG_ROLE);
                if (orgRole != null) {
                    authorities.add(new SimpleGrantedAuthority(orgRole));
                }

                List<String> permissions = jwt.getClaimAsStringList(JwtClaimSetConstant.CLAIM_PERMISSION);
                if (permissions != null) {
                    permissions.forEach(permission -> authorities.add(new SimpleGrantedAuthority(permission)));
                }
            } else {
                // Identity Token or legacy: extract scope (roles) + permissions
                List<String> roles = jwt.getClaimAsStringList(JwtClaimSetConstant.CLAIM_SCOPE);
                if (roles != null) {
                    roles.forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role)));
                }

                List<String> permissions = jwt.getClaimAsStringList(JwtClaimSetConstant.CLAIM_PERMISSION);
                if (permissions != null) {
                    permissions.forEach(permission -> authorities.add(new SimpleGrantedAuthority(permission)));
                }
            }

            return authorities;
        });
        return jwtAuthenticationConverter;
    }

    @Bean
    JwtDecoder jwtDecoder() {
        SecretKeySpec secretKey = new SecretKeySpec(SIGNER_KEY.getBytes(), "HS512");
        return NimbusJwtDecoder.withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS512)
                .build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
