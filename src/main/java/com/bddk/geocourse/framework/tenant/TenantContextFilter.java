package com.bddk.geocourse.framework.tenant;

import com.bddk.geocourse.framework.common.error.ErrorCode;
import com.bddk.geocourse.framework.common.error.ServiceException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class TenantContextFilter extends OncePerRequestFilter {

    private final com.bddk.geocourse.framework.tenant.TenantProperties tenantProperties;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public TenantContextFilter(com.bddk.geocourse.framework.tenant.TenantProperties tenantProperties) {
        this.tenantProperties = tenantProperties;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!tenantProperties.isEnabled()) {
            return true;
        }
        String path = request.getRequestURI();
        return tenantProperties.getIgnorePaths().stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String tenantId = request.getHeader(tenantProperties.getHeaderName());
            if (StringUtils.hasText(tenantId)) {
                com.bddk.geocourse.framework.tenant.TenantContextHolder.setTenantId(Long.parseLong(tenantId));
            } else if (tenantProperties.getDefaultTenantId() != null) {
                com.bddk.geocourse.framework.tenant.TenantContextHolder.setTenantId(tenantProperties.getDefaultTenantId());
            } else {
                throw new ServiceException(ErrorCode.TENANT_REQUIRED);
            }
            filterChain.doFilter(request, response);
        } catch (NumberFormatException ex) {
            throw new ServiceException(ErrorCode.BAD_REQUEST, "租户标识必须为数字");
        } finally {
            com.bddk.geocourse.framework.tenant.TenantContextHolder.clear();
        }
    }

}

