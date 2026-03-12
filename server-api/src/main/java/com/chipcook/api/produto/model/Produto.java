package com.chipcook.api.produto.model;

import com.chipcook.api.domain.TenantContext;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "tb_produto")
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String descricao;
    private BigDecimal preco;
    private String categoria; // Bebidas, Lanches, Sobremesas

    @Column(columnDefinition = "TEXT")
    private String imagem; // URL, emoji ou base64

    private Boolean disponivel;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "tb_produto_ingrediente", joinColumns = @JoinColumn(name = "produto_id"))
    @OrderColumn(name = "ordem")
    private List<ProdutoIngrediente> ingredientes = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "tb_produto_passo", joinColumns = @JoinColumn(name = "produto_id"))
    @OrderColumn(name = "ordem")
    private List<ProdutoPassoReceita> passos = new ArrayList<>();

    @Column(name = "tenant_id")
    private String tenantId;

    @PrePersist
    public void prePersist() {
        this.tenantId = TenantContext.getTenantId();
        if (this.disponivel == null) {
            this.disponivel = true;
        }
        if (this.ingredientes == null) {
            this.ingredientes = new ArrayList<>();
        }
        if (this.passos == null) {
            this.passos = new ArrayList<>();
        }
    }

    @PreUpdate
    public void preUpdate() {
        if (this.ingredientes == null) {
            this.ingredientes = new ArrayList<>();
        }
        if (this.passos == null) {
            this.passos = new ArrayList<>();
        }
    }
}
