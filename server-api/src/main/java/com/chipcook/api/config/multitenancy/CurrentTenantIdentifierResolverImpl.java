package com.chipcook.api.config.multitenancy;

import com.chipcook.api.domain.TenantContext;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.stereotype.Component;

@Component
public class CurrentTenantIdentifierResolverImpl implements CurrentTenantIdentifierResolver {

    @Override
    public String resolveCurrentTenantIdentifier() {
        String tenantId = TenantContext.getTenantId();
        return tenantId != null ? tenantId : "chipcook";
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }
}