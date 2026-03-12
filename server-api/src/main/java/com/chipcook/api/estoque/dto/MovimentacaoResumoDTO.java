package com.chipcook.api.estoque.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MovimentacaoResumoDTO {
    private String itemNome;
    private String tipo;
    private double quantidade;
    private String motivo;
    private String categoriaOrigem;
    private String categoriaDestino;
    private LocalDateTime dataHora;
}
