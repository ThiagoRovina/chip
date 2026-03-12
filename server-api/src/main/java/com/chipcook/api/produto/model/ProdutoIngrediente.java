package com.chipcook.api.produto.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;

@Data
@Embeddable
public class ProdutoIngrediente {

    @Column(name = "estoque_item_id")
    private Long estoqueItemId;

    @Column(name = "ingrediente")
    private String nomeItemEstoque;

    @Column(name = "quantidade")
    private Double quantidade;

    @Column(name = "unidade")
    private String unidade;
}
