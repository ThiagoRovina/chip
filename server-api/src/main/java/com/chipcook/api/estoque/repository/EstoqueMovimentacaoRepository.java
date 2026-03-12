package com.chipcook.api.estoque.repository;

import com.chipcook.api.estoque.model.EstoqueMovimentacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EstoqueMovimentacaoRepository extends JpaRepository<EstoqueMovimentacao, Long> {
    List<EstoqueMovimentacao> findTop10ByTenantIdAndTipoOrderByDataHoraDesc(String tenantId, String tipo);

    long countByTenantIdAndTipoAndDataHoraAfter(String tenantId, String tipo, LocalDateTime dataHora);

    @Query("select coalesce(sum(m.quantidade), 0) from EstoqueMovimentacao m where m.tenantId = :tenantId and m.tipo = :tipo and m.dataHora >= :dataHora")
    Double somarQuantidadePorTipoDesde(@Param("tenantId") String tenantId,
                                       @Param("tipo") String tipo,
                                       @Param("dataHora") LocalDateTime dataHora);

    @Query("select m from EstoqueMovimentacao m where m.tenantId = :tenantId order by m.dataHora desc")
    List<EstoqueMovimentacao> listarRecentes(@Param("tenantId") String tenantId);
}
