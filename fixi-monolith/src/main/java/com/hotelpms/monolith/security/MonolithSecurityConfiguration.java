package com.hotelpms.monolith.security;

import java.util.HashMap;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

/** Replaces the gateway-to-service HMAC boundary with JWT and double-submit CSRF at the monolith. */
@Configuration(proxyBeanMethods = false)
@EnableMethodSecurity
public class MonolithSecurityConfiguration {

  @Bean
  PasswordEncoder passwordEncoder() {
    final Map<String, PasswordEncoder> encoders = new HashMap<>();
    encoders.put("argon2", new Argon2PasswordEncoder(16, 32, 1, 19 * 1024, 2));
    encoders.put("bcrypt", new BCryptPasswordEncoder(12));
    return new DelegatingPasswordEncoder("argon2", encoders);
  }

  @Bean
  SecurityFilterChain monolithSecurityFilterChain(
      final HttpSecurity http, final MonolithJwtAuthenticationFilter jwtFilter) throws Exception {
    final CookieCsrfTokenRepository csrfRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
    csrfRepository.setCookieName("csrf_token");
    csrfRepository.setHeaderName("X-CSRF-Token");

    http.csrf(
            csrf ->
                csrf.csrfTokenRepository(csrfRepository)
                    .ignoringRequestMatchers(
                        "/api/v1/auth/login",
                        "/api/v1/auth/register",
                        "/api/v1/auth/refresh",
                        "/api/v1/auth/public/**",
                        "/api/public/portal/**",
                        "/api/v1/public/requests/**",
                        "/api/public/hotels/*/reservations"))
        .authorizeHttpRequests(
            authorization ->
                authorization
                    .requestMatchers(HttpMethod.OPTIONS, "/**")
                    .permitAll()
                    .requestMatchers(
                        "/api/v1/auth/login",
                        "/api/v1/auth/register",
                        "/api/v1/auth/refresh",
                        "/api/v1/auth/logout",
                        "/api/v1/auth/change-password",
                        "/api/v1/auth/me",
                        "/api/v1/auth/session-security",
                        "/api/v1/auth/public/**",
                        "/api/public/portal/**",
                        "/api/v1/public/requests/**",
                        "/api/public/hotels/*/reservations",
                        "/actuator/health/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
        .httpBasic(AbstractHttpConfigurer::disable)
        .formLogin(AbstractHttpConfigurer::disable);
    return http.build();
  }
}
