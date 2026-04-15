package com.bddk.geocourse.framework.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Keep the backend compatible with frontend requests sent to /api/* while
 * preserving the original controller mappings such as /auth/* and /school/*.
 */
@Component
public class ApiPrefixForwardFilter extends OncePerRequestFilter {

    private static final String API_PREFIX = "/api";

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = pathWithinApplication(request);
        return !path.startsWith(API_PREFIX + "/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = pathWithinApplication(request);
        String forwardPath = path.substring(API_PREFIX.length());
        request.getRequestDispatcher(forwardPath).forward(request, response);
    }

    private String pathWithinApplication(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        String requestUri = request.getRequestURI();
        if (contextPath == null || contextPath.isEmpty()) {
            return requestUri;
        }
        return requestUri.substring(contextPath.length());
    }
}
