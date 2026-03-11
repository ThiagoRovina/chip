package com.chipcook.api.funcionario.service;

import com.chipcook.api.domain.Tenant;
import com.chipcook.api.domain.TenantContext;
import com.chipcook.api.funcionario.dto.FuncionarioDTO;
import com.chipcook.api.funcionario.model.Funcionario;
import com.chipcook.api.funcionario.repository.FuncionarioRepository;
import com.chipcook.api.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FuncionarioService {

    private final FuncionarioRepository funcionarioRepository;
    private final TenantRepository tenantRepository;

    public FuncionarioDTO salvar(FuncionarioDTO dto) {
        Funcionario funcionario = new Funcionario();
        BeanUtils.copyProperties(dto, funcionario);
        
        String tenantId = TenantContext.getTenantId();
        Tenant tenant = tenantRepository.findByTenantIdAndActiveTrue(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant não encontrado: " + tenantId));
        funcionario.setTenant(tenant);

        funcionario = funcionarioRepository.save(funcionario);
        
        FuncionarioDTO result = new FuncionarioDTO();
        BeanUtils.copyProperties(funcionario, result);
        return result;
    }

    public List<FuncionarioDTO> listarTodos() {
        String tenantId = TenantContext.getTenantId();
        Tenant tenant = tenantRepository.findByTenantIdAndActiveTrue(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant não encontrado"));
                
        return funcionarioRepository.findByTenant_Id(tenant.getId()).stream()
                .map(f -> {
                    FuncionarioDTO dto = new FuncionarioDTO();
                    BeanUtils.copyProperties(f, dto);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public FuncionarioDTO buscarPorId(UUID id) {
        String tenantId = TenantContext.getTenantId();
        Tenant tenant = tenantRepository.findByTenantIdAndActiveTrue(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant não encontrado"));

        Funcionario funcionario = funcionarioRepository.findByIdFuncionarioAndTenant_Id(id, tenant.getId())
                .orElseThrow(() -> new RuntimeException("Funcionário não encontrado"));
        
        FuncionarioDTO dto = new FuncionarioDTO();
        BeanUtils.copyProperties(funcionario, dto);
        return dto;
    }

    public FuncionarioDTO buscarPorEmail(String email) {
        String tenantId = TenantContext.getTenantId();
        Tenant tenant = tenantRepository.findByTenantIdAndActiveTrue(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant não encontrado"));

        List<Funcionario> funcionarios = funcionarioRepository.findAllByNmEmailAndTenant_Id(email, tenant.getId());

        if (!funcionarios.isEmpty()) {
            if (funcionarios.size() > 1) {
                log.warn("Encontrados {} funcionários para o email {} no tenant {}. Usando o primeiro por ID.",
                        funcionarios.size(), email, tenantId);
            }

            Funcionario escolhido = funcionarios.stream()
                    .sorted(Comparator.comparing(Funcionario::getIdFuncionario,
                            Comparator.nullsLast(Comparator.naturalOrder())))
                    .findFirst()
                    .orElse(null);

            if (escolhido == null) {
                return null;
            }

            FuncionarioDTO dto = new FuncionarioDTO();
            BeanUtils.copyProperties(escolhido, dto);
            return dto;
        }
        return null;
    }

    public FuncionarioDTO atualizar(UUID id, FuncionarioDTO dto) {
        String tenantId = TenantContext.getTenantId();
        Tenant tenant = tenantRepository.findByTenantIdAndActiveTrue(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant não encontrado"));

        Funcionario funcionario = funcionarioRepository.findByIdFuncionarioAndTenant_Id(id, tenant.getId())
                .orElseThrow(() -> new RuntimeException("Funcionário não encontrado"));

        BeanUtils.copyProperties(dto, funcionario, "idFuncionario", "tenant");
        funcionario = funcionarioRepository.save(funcionario);

        FuncionarioDTO result = new FuncionarioDTO();
        BeanUtils.copyProperties(funcionario, result);
        return result;
    }

    public void deletar(UUID id) {
        String tenantId = TenantContext.getTenantId();
        Tenant tenant = tenantRepository.findByTenantIdAndActiveTrue(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant não encontrado"));

        Funcionario funcionario = funcionarioRepository.findByIdFuncionarioAndTenant_Id(id, tenant.getId())
                .orElseThrow(() -> new RuntimeException("Funcionário não encontrado"));
        funcionarioRepository.delete(funcionario);
    }
}
