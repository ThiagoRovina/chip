package com.chipcook.api.pedido.repository;

import com.chipcook.api.pedido.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByTenantIdAndStatusNot(String tenantId, String status);
    List<Pedido> findByTenantId(String tenantId);
    Optional<Pedido> findByIdAndTenantId(Long id, String tenantId);
}
