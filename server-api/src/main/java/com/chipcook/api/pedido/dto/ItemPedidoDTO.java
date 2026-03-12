package com.chipcook.api.pedido.dto;

import lombok.Data;

@Data
public class ItemPedidoDTO {
    private Long produtoId;
    private String nomeProduto;
    private Integer quantidade;
    private String observacao;
}
