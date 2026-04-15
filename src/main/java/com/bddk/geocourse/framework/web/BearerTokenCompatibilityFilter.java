package com.bddk.geocourse.framework.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class BearerTokenCompatibilityFilter extends OncePerRequestFilter {

    private static final String SA_TOKEN_HEADER = "satoken";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String existingSaToken = request.getHeader(SA_TOKEN_HEADER);
        String bearerToken = extractBearerToken(request.getHeader(AUTHORIZATION_HEADER));
        if (StringUtils.hasText(existingSaToken) || !StringUtils.hasText(bearerToken)) {
            filterChain.doFilter(request, response);
            return;
        }
        filterChain.doFilter(new BearerTokenRequestWrapper(request, bearerToken), response);
    }

    private String extractBearerToken(String authorization) {
        if (!StringUtils.hasText(authorization)) {
            return null;
        }
        String value = authorization.trim();
        if (!value.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
            return null;
        }
        String token = value.substring(BEARER_PREFIX.length()).trim();
        return StringUtils.hasText(token) ? token : null;
    }

    private static final class BearerTokenRequestWrapper extends HttpServletRequestWrapper {

        private final String tokenValue;

        private BearerTokenRequestWrapper(HttpServletRequest request, String tokenValue) {
            super(request);
            this.tokenValue = tokenValue;
        }

        @Override
        public String getHeader(String name) {
            if (SA_TOKEN_HEADER.equalsIgnoreCase(name)) {
                return tokenValue;
            }
            return super.getHeader(name);
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            if (SA_TOKEN_HEADER.equalsIgnoreCase(name)) {
                return Collections.enumeration(List.of(tokenValue));
            }
            return super.getHeaders(name);
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            Set<String> headerNames = new LinkedHashSet<>();
            Enumeration<String> names = super.getHeaderNames();
            while (names.hasMoreElements()) {
                headerNames.add(names.nextElement());
            }
            headerNames.add(SA_TOKEN_HEADER);
            return Collections.enumeration(headerNames);
        }

        @Override
        public String getParameter(String name) {
            if (SA_TOKEN_HEADER.equalsIgnoreCase(name) && super.getParameter(name) == null) {
                return tokenValue;
            }
            return super.getParameter(name);
        }

        @Override
        public Enumeration<String> getParameterNames() {
            Set<String> parameterNames = new LinkedHashSet<>();
            Enumeration<String> names = super.getParameterNames();
            while (names.hasMoreElements()) {
                parameterNames.add(names.nextElement());
            }
            parameterNames.add(SA_TOKEN_HEADER);
            return Collections.enumeration(parameterNames);
        }

        @Override
        public String[] getParameterValues(String name) {
            if (SA_TOKEN_HEADER.equalsIgnoreCase(name) && super.getParameterValues(name) == null) {
                return new String[] { tokenValue };
            }
            return super.getParameterValues(name);
        }
    }
}
