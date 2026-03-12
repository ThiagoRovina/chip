package com.chipcook.api.estoque.model;

import com.chipcook.api.domain.TenantContext;
import com.chipcook.api.estoque.enums.CategoriaEstoque;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "tb_estoque_item")
public class EstoqueItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private Double quantidade;
    private String unidade; // kg, un, l
    private LocalDate validade;
    private String categoria; // Frios, Hortifruti, etc.

    @Column(columnDefinition = "TEXT")
    private String imagem; // Emoji, URL ou base64

    private String status; // ok, baixo, vencendo

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria_estoque")
    private CategoriaEstoque categoriaEstoque;

    @Column(name = "estoque_minimo")
    private Double estoqueMinimo;

    private String localizacao;

    @Column(name = "tenant_id")
    private String tenantId;

    @PrePersist
    public void prePersist() {
        this.tenantId = TenantContext.getTenantId();
        atualizarStatus();
        if (this.categoriaEstoque == null) {
            this.categoriaEstoque = CategoriaEstoque.GERAL;
        }
    }

    @PreUpdate
    public void preUpdate() {
        atualizarStatus();
    }

    public void atualizarStatus() {
        double qtdAtual = this.quantidade == null ? 0.0 : this.quantidade;
        double minimo = this.estoqueMinimo == null ? 5.0 : this.estoqueMinimo;

        if (qtdAtual <= minimo) {
            this.status = "baixo";
        } else if (this.validade != null && this.validade.isBefore(LocalDate.now().plusDays(3))) {
            this.status = "vencendo";
        } else {
            this.status = "ok";
        }
    }
}
