package com.chipcook.api.produto.model;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Data
@Embeddable
public class ProdutoPassoReceita {

    private String descricao;

    private Integer tempoSegundos;

    private String videoUrl;
}
