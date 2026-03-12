package com.chipcook.api.estoque.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ItemAlertaEstoqueDTO {
    private Long itemId;
    private String nome;
    private String categoriaEstoque;
    private double quantidadeAtual;
    private double estoqueMinimo;
    private String unidade;
    private String status;
}
