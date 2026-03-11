package com.chipcook.api.pedido.dto;

import lombok.Data;

@Data
public class ItemPedidoDTO {
    private String nomeProduto;
    private Integer quantidade;
    private String observacao;
}
