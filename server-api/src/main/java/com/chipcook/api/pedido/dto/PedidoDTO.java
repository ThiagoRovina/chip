package com.chipcook.api.pedido.dto;

import lombok.Data;
import java.util.List;

@Data
public class PedidoDTO {
    private String mesa;
    private String cliente;
    private List<ItemPedidoDTO> itens;
}
