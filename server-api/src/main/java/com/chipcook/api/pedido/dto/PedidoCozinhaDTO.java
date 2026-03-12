package com.chipcook.api.pedido.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class PedidoCozinhaDTO {
    private Long id;
    private String mesa;
    private String cliente;
    private LocalDateTime dataHora;
    private String status;
    private boolean estoqueDisponivel;
    private List<String> pendenciasEstoque;
    private List<CozinhaItemPedidoDTO> itens;
}
