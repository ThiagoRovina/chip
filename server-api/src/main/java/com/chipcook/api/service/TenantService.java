package com.chipcook.api.service;

import com.chipcook.api.domain.Tenant;
import com.chipcook.api.dto.TenantResponse;
import com.chipcook.api.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TenantService {

    private final TenantRepository tenantRepository;


    public Optional<Tenant> findByTenantId(String tenantId) {
        return tenantRepository.findByTenantIdAndActiveTrue(tenantId);
    }

    public boolean isValidTenant(String tenantId) {
        if (tenantId == null || tenantId.isEmpty()) {
            return false;
        }
        return findByTenantId(tenantId).isPresent();
    }

    public List<TenantResponse> findAll() {
        return tenantRepository.findAll().stream()
                .map(Tenant::toResponse)
                .toList();
    }

    public TenantResponse findById(UUID id) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tenant não encontrado: " + id));
        return Tenant.toResponse(tenant);
    }

    public TenantResponse deactivate(UUID id) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tenant não encontrado: " + id));
        tenant.setActive(false);
        tenant = tenantRepository.save(tenant);
        return Tenant.toResponse(tenant);
    }

    public TenantResponse activate(UUID id) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tenant não encontrado: " + id));
        tenant.setActive(true);
        tenant = tenantRepository.save(tenant);
        return Tenant.toResponse(tenant);
    }
}
