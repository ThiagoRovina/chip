package com.chipcook.api.estoque.repository;

import com.chipcook.api.estoque.enums.CategoriaEstoque;
import com.chipcook.api.estoque.model.EstoqueItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EstoqueRepository extends JpaRepository<EstoqueItem, Long> {
    List<EstoqueItem> findByTenantId(String tenantId);

    Optional<EstoqueItem> findByIdAndTenantId(Long id, String tenantId);

    List<EstoqueItem> findByTenantIdAndNomeIgnoreCase(String tenantId, String nome);

    Optional<EstoqueItem> findByTenantIdAndNomeIgnoreCaseAndCategoriaEstoque(String tenantId,
                                                                              String nome,
                                                                              CategoriaEstoque categoria);

    // Métodos para categorias
    List<EstoqueItem> findByTenantIdAndCategoriaEstoque(String tenantId, CategoriaEstoque categoria);

    @Query("SELECT e FROM EstoqueItem e WHERE e.tenantId = :tenantId AND e.categoriaEstoque = :categoria AND e.quantidade < e.estoqueMinimo")
    List<EstoqueItem> findItensAbaixoMinimoByCategoria(@Param("tenantId") String tenantId,
            @Param("categoria") CategoriaEstoque categoria);

    @Query("SELECT e FROM EstoqueItem e WHERE e.tenantId = :tenantId AND e.quantidade < e.estoqueMinimo")
    List<EstoqueItem> findItensAbaixoMinimo(@Param("tenantId") String tenantId);
}
