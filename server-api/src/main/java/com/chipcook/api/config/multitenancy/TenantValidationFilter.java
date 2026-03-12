package com.chipcook.api.config.multitenancy;

import com.chipcook.api.service.TenantService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class TenantValidationFilter extends OncePerRequestFilter {

    private final TenantSubdomainResolver resolver;
    private final TenantService tenantService;

    public TenantValidationFilter(TenantSubdomainResolver resolver, TenantService tenantService) {
        this.resolver = resolver;
        this.tenantService = tenantService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path != null && path.startsWith("/api/admin/tenants");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String tenantId = resolver.resolveTenant(request);

        if (tenantId != null && !tenantService.isValidTenant(tenantId)) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"erro\":\"Tenant não encontrado\",\"tenant\":\"" + tenantId + "\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
