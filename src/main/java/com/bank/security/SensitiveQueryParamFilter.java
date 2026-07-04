package com.bank.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Set;

@Component
public class SensitiveQueryParamFilter extends OncePerRequestFilter {

    private static final Set<String> BLOCKED_PARAMS = Set.of(
            "username", "password", "passwd", "pwd", "userid", "user_id"
    );

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        if ("GET".equalsIgnoreCase(request.getMethod()) && containsBlockedQueryParam(request)) {
            String cleanUrl = buildCleanUrl(request);
            response.sendRedirect(cleanUrl);
            return;
        }
        filterChain.doFilter(request, response);
    }

    private boolean containsBlockedQueryParam(HttpServletRequest request) {
        if (request.getQueryString() == null || request.getQueryString().isBlank()) {
            return false;
        }

        for (String paramName : request.getParameterMap().keySet()) {
            if (BLOCKED_PARAMS.contains(paramName.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private String buildCleanUrl(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        if (requestUri == null || requestUri.isBlank()) {
            requestUri = "/";
        }
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath(requestUri);
        request.getParameterMap().forEach((name, values) -> {
            if (!BLOCKED_PARAMS.contains(name.toLowerCase())) {
                for (String value : values) {
                    builder.queryParam(name, value);
                }
            }
        });
        return builder.build().toUriString();
    }
}
