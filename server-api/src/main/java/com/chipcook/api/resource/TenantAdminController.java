package com.chipcook.api.resource;

import com.chipcook.api.dto.CreateTenantRequest;
import com.chipcook.api.dto.TenantResponse;
import com.chipcook.api.service.TenantProvisioningService;
import com.chipcook.api.service.TenantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/tenants")
@RequiredArgsConstructor
public class TenantAdminController {

    private final TenantProvisioningService provisioningService;
    private final TenantService tenantService;


    @PostMapping
    public ResponseEntity<TenantResponse> createTenant(@Valid @RequestBody CreateTenantRequest request) {
        TenantResponse response = provisioningService.createTenant(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<TenantResponse>> listTenants() {
        List<TenantResponse> tenants = tenantService.findAll();
        return ResponseEntity.ok(tenants);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TenantResponse> getTenant(@PathVariable UUID id) {
        TenantResponse response = tenantService.findById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<TenantResponse> deactivateTenant(@PathVariable UUID id) {
        TenantResponse response = tenantService.deactivate(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<TenantResponse> activateTenant(@PathVariable UUID id) {
        TenantResponse response = tenantService.activate(id);
        return ResponseEntity.ok(response);
    }
}
