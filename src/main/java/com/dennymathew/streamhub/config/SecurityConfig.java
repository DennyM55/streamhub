package com.dennymathew.streamhub.config;

import com.dennymathew.streamhub.security.JwtAuthenticationFilter;
import com.dennymathew.streamhub.security.RestAuthenticationEntryPoint;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.http.HttpMethod;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.NullSecurityContextRepository;
import org.springframework.security.web.savedrequest.NullRequestCache;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.springframework.security.authorization.AuthorizationDecision;

@Configuration
public class SecurityConfig {

    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource(
            @Value("${streamhub.cors.origin}") String origin) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(origin.split(",")));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtAuthenticationFilterRegistration(
            JwtAuthenticationFilter jwtAuthenticationFilter) {

        FilterRegistrationBean<JwtAuthenticationFilter> registration =
                new FilterRegistrationBean<>(jwtAuthenticationFilter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            RestAuthenticationEntryPoint restAuthenticationEntryPoint,
            @Value("${streamhub.admin.key:}") String adminKey,
            @Value("${STREAMHUB_DEMO_ONLY:false}") boolean demoOnly)
            throws Exception {

        return http
                .cors(cors -> {})
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                        .sessionFixation(sessionFixation ->
                                sessionFixation.none())
                )

                .securityContext(security -> security
                        .securityContextRepository(
                                new NullSecurityContextRepository())
                )

                .requestCache(cache -> cache
                        .requestCache(new NullRequestCache())
                )

                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(
                                restAuthenticationEntryPoint)
                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/movies", "/movies/**", "/health").permitAll()
                        .requestMatchers(HttpMethod.POST, "/demo/session").permitAll()
                        .requestMatchers("/movies", "/movies/**").access((authentication, context) -> {
                            String supplied = context.getRequest().getHeader("X-Admin-Key");
                            return new AuthorizationDecision(!adminKey.isBlank() && supplied != null
                                    && MessageDigest.isEqual(adminKey.getBytes(StandardCharsets.UTF_8),
                                                            supplied.getBytes(StandardCharsets.UTF_8)));
                        })
                        .requestMatchers("/users", "/users/login").access((authentication, context) ->
                                new AuthorizationDecision(!demoOnly))
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated()
                )

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                )

                .build();
    }
}
