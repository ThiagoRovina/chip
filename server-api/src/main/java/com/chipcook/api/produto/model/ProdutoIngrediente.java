package com.chipcook.api.produto.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;

@Data
@Embeddable
public class ProdutoIngrediente {

    @Column(name = "ingrediente")
    private String valor;
}
