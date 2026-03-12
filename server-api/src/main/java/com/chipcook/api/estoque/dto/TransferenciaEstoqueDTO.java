package com.chipcook.api.estoque.dto;

import lombok.Data;

@Data
public class TransferenciaEstoqueDTO {
    private Long itemOrigemId;
    private String categoriaDestino;
    private Double quantidade;
    private String perfil;
}
