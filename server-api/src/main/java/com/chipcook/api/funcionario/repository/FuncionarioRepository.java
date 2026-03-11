package com.chipcook.api.funcionario.repository;

import com.chipcook.api.funcionario.model.Funcionario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FuncionarioRepository extends JpaRepository<Funcionario, UUID> {
    List<Funcionario> findByTenant_Id(UUID tenantId);
    Optional<Funcionario> findByIdFuncionarioAndTenant_Id(UUID id, UUID tenantId);
    Optional<Funcionario> findByNmEmailAndTenant_Id(String email, UUID tenantId);
    List<Funcionario> findAllByNmEmailAndTenant_Id(String email, UUID tenantId);
}
