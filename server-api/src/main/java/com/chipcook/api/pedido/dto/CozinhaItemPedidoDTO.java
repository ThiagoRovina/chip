package com.chipcook.api.pedido.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CozinhaItemPedidoDTO {
    private Long id;
    private Long produtoId;
    private String nomeProduto;
    private Integer quantidade;
    private String observacao;
    private boolean estoqueDisponivel;
    private List<CozinhaIngredienteDTO> ingredientes;
}
