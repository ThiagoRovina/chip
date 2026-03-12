package com.chipcook.api.service;

import com.chipcook.api.domain.Tenant;
import com.chipcook.api.dto.CreateTenantRequest;
import com.chipcook.api.dto.TenantResponse;
import com.chipcook.api.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Service
@RequiredArgsConstructor
public class TenantProvisioningService {

    private final TenantRepository tenantRepository;
    private final DataSource dataSource;


    @Transactional
    public TenantResponse createTenant(CreateTenantRequest request) {
        if (tenantRepository.existsByTenantId(request.getTenantId())) {
            throw new IllegalArgumentException("Tenant ID já existe: " + request.getTenantId());
        }

        if (tenantRepository.existsBySubdomain(request.getSubdomain())) {
            throw new IllegalArgumentException("Subdomain já existe: " + request.getSubdomain());
        }

        Tenant tenant = new Tenant();
        tenant.setTenantId(request.getTenantId());
        tenant.setName(request.getName());
        tenant.setSubdomain(request.getSubdomain());
        tenant.setActive(true);
        tenant = tenantRepository.save(tenant);

        try {
            createSchema(request.getTenantId());
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao criar schema para o tenant: " + request.getTenantId(), e);
        }

        return Tenant.toResponse(tenant);
    }

    private void createSchema(String tenantId) throws SQLException {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("CREATE SCHEMA IF NOT EXISTS " + tenantId);
        }
    }
}
