package com.chipcook.api.resource;

import com.chipcook.api.domain.Tenant;
import com.chipcook.api.domain.TenantContext;
import com.chipcook.api.dto.TenantResponse;
import com.chipcook.api.service.TenantService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/tenant")
public class TenantController {

    private final TenantService tenantService;

    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @GetMapping("/current")
    public ResponseEntity<TenantResponse> getCurrentTenant(
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantIdHeader) {
        String tenantId = TenantContext.getTenantId() != null ? TenantContext.getTenantId() : tenantIdHeader;
        if (tenantId == null || tenantId.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        Optional<Tenant> tenantOpt = tenantService.findByTenantId(tenantId);
        
        if (tenantOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(Tenant.toResponse(tenantOpt.get()));
    }
}
