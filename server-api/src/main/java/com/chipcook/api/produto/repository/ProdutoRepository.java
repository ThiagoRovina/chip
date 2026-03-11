package com.chipcook.api.produto.repository;

import com.chipcook.api.produto.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {
    List<Produto> findByTenantId(String tenantId);
    Optional<Produto> findByIdAndTenantId(Long id, String tenantId);
    List<Produto> findByTenantIdAndCategoria(String tenantId, String categoria);
}
