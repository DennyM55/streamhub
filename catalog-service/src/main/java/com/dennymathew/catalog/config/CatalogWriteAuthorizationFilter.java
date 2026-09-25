package com.dennymathew.catalog.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Set;

/** The gateway authenticates admins; this internal service additionally requires its shared key. */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class CatalogWriteAuthorizationFilter extends OncePerRequestFilter {
    private static final Set<String> READ_METHODS = Set.of("GET", "HEAD", "OPTIONS");
    private final byte[] expectedKey;

    public CatalogWriteAuthorizationFilter(@Value("${streamhub.catalog.key:}") String expectedKey) {
        this.expectedKey = expectedKey.isBlank() ? new byte[0] : expectedKey.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        if (!READ_METHODS.contains(request.getMethod())) {
            String suppliedKey = request.getHeader("X-Catalog-Key");
            if (expectedKey.length == 0 || suppliedKey == null || !MessageDigest.isEqual(
                    expectedKey, suppliedKey.getBytes(StandardCharsets.UTF_8))) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                response.getWriter().write("{\"status\":403,\"message\":\"Catalog write access denied\"}");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
