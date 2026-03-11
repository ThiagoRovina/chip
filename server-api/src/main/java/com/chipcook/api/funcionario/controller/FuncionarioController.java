package com.chipcook.api.funcionario.controller;

import com.chipcook.api.domain.TenantContext;
import com.chipcook.api.funcionario.dto.FuncionarioDTO;
import com.chipcook.api.funcionario.service.FuncionarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/funcionario")
@RequiredArgsConstructor
public class FuncionarioController {

    private final FuncionarioService funcionarioService;

    @PostMapping
    public ResponseEntity<FuncionarioDTO> criar(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @RequestBody FuncionarioDTO dto) {
        TenantContext.setTenantId(tenantId);
        try {
            return ResponseEntity.ok(funcionarioService.salvar(dto));
        } finally {
            TenantContext.clear();
        }
    }

    @GetMapping
    public ResponseEntity<List<FuncionarioDTO>> listar(@RequestHeader("X-Tenant-ID") String tenantId) {
        TenantContext.setTenantId(tenantId);
        try {
            return ResponseEntity.ok(funcionarioService.listarTodos());
        } finally {
            TenantContext.clear();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<FuncionarioDTO> buscarPorId(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @PathVariable UUID id) {
        TenantContext.setTenantId(tenantId);
        try {
            return ResponseEntity.ok(funcionarioService.buscarPorId(id));
        } finally {
            TenantContext.clear();
        }
    }

    @GetMapping("/buscar")
    public ResponseEntity<FuncionarioDTO> buscarPorEmail(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @RequestParam String email) {
        TenantContext.setTenantId(tenantId);
        try {
            FuncionarioDTO funcionario = funcionarioService.buscarPorEmail(email);
            if (funcionario != null) {
                return ResponseEntity.ok(funcionario);
            }
            return ResponseEntity.notFound().build();
        } finally {
            TenantContext.clear();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<FuncionarioDTO> atualizar(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @PathVariable UUID id, 
            @RequestBody FuncionarioDTO dto) {
        TenantContext.setTenantId(tenantId);
        try {
            return ResponseEntity.ok(funcionarioService.atualizar(id, dto));
        } finally {
            TenantContext.clear();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @PathVariable UUID id) {
        TenantContext.setTenantId(tenantId);
        try {
            funcionarioService.deletar(id);
            return ResponseEntity.noContent().build();
        } finally {
            TenantContext.clear();
        }
    }
}
