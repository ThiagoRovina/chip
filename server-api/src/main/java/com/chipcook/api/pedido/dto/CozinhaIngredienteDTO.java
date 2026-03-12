package com.chipcook.api.pedido.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CozinhaIngredienteDTO {
    private Long estoqueItemId;
    private String nome;
    private Double quantidadeNecessaria;
    private Double quantidadeDisponivel;
    private String unidade;
    private boolean disponivel;
}
