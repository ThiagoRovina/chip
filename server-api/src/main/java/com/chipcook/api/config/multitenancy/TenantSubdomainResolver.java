package com.chipcook.api.config.multitenancy;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;

@Component
public class TenantSubdomainResolver {

    private static final int MAX_TENANT_ID_LENGTH = 63;
    private static final String DEFAULT_TENANT = "chipcook";
    private static final String LOCALHOST = "localhost";
    private static final String LOCALHOST_SUFFIX = ".localhost";

    public String resolveTenant(HttpServletRequest request) {
        String fromOrigin = resolveFromUrlHeader(request.getHeader("Origin"));
        if (fromOrigin != null) {
            return fromOrigin;
        }

        String fromReferer = resolveFromUrlHeader(request.getHeader("Referer"));
        if (fromReferer != null) {
            return fromReferer;
        }

        String fromHost = resolveFromHostHeader(request.getHeader("Host"));
        if (fromHost != null) {
            return fromHost;
        }

        String fromTenantHeader = validateAndNormalize(request.getHeader("X-Tenant-ID"));
        return fromTenantHeader != null ? fromTenantHeader : DEFAULT_TENANT;
    }

    private String resolveFromUrlHeader(String urlHeader) {
        if (urlHeader == null || urlHeader.isBlank()) {
            return null;
        }

        try {
            URI uri = new URI(urlHeader);
            return resolveFromHostHeader(uri.getHost());
        } catch (URISyntaxException e) {
            return null;
        }
    }

    private String resolveFromHostHeader(String hostHeader) {
        if (hostHeader == null || hostHeader.isBlank()) {
            return null;
        }

        String withoutPort = hostHeader.split(":")[0].toLowerCase();
        if (withoutPort.equals(LOCALHOST) || withoutPort.equals("127.0.0.1")) {
            return null;
        }

        if (withoutPort.endsWith(LOCALHOST_SUFFIX)) {
            String prefix = withoutPort.substring(0, withoutPort.length() - LOCALHOST_SUFFIX.length());
            if (prefix.isBlank()) {
                return null;
            }
            String firstLabel = prefix.split("\\.")[0];
            return validateAndNormalize(firstLabel);
        }

        String[] parts = withoutPort.split("\\.");
        if (parts.length < 3) {
            return null;
        }

        return validateAndNormalize(parts[0]);
    }

    private String validateAndNormalize(String tenantId) {
        if (tenantId == null || tenantId.isBlank()) {
            return null;
        }

        String normalized = tenantId.trim().toLowerCase();

        if (!normalized.matches("^[a-z0-9_]+$")) {
            return null;
        }

        if (normalized.length() > MAX_TENANT_ID_LENGTH) {
            return null;
        }

        return normalized;
    }
}
