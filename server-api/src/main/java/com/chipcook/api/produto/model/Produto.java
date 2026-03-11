package com.chipcook.api.produto.model;

import com.chipcook.api.domain.TenantContext;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

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
    private String imagem; // URL ou Emoji
    private Boolean disponivel;

    @Column(name = "tenant_id")
    private String tenantId;

    @PrePersist
    public void prePersist() {
        this.tenantId = TenantContext.getTenantId();
        if (this.disponivel == null) {
            this.disponivel = true;
        }
    }
}
