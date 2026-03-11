package com.chipcook.api.repository;

import com.chipcook.api.domain.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, UUID> {
    Optional<Tenant> findByTenantIdAndActiveTrue(String tenantId);

    Optional<Tenant> findBySubdomainAndActiveTrue(String subdomain);

    boolean existsByTenantId(String tenantId);

    boolean existsBySubdomain(String subdomain);
}
