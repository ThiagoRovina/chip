package com.chipcook.api.estoque.model;

import com.chipcook.api.domain.TenantContext;
import com.chipcook.api.estoque.enums.CategoriaEstoque;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "tb_estoque_movimentacao")
public class EstoqueMovimentacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "item_id")
    private Long itemId;

    @Column(name = "item_nome")
    private String itemNome;

    private String tipo;

    private Double quantidade;

    private String motivo;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria_origem")
    private CategoriaEstoque categoriaOrigem;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria_destino")
    private CategoriaEstoque categoriaDestino;

    @Column(name = "data_hora")
    private LocalDateTime dataHora;

    @Column(name = "tenant_id")
    private String tenantId;

    @PrePersist
    public void prePersist() {
        this.tenantId = TenantContext.getTenantId();
        if (this.dataHora == null) {
            this.dataHora = LocalDateTime.now();
        }
    }
}
