package com.chipcook.api.estoque.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class EstoqueDashboardDTO {
    private long itensAbaixoMinimo;
    private long itensVencendo;
    private long perdasUltimos7Dias;
    private double quantidadePerdidaUltimos7Dias;
    private long transferenciasUltimos7Dias;
    private double quantidadeTransferidaUltimos7Dias;
    private List<ItemAlertaEstoqueDTO> reposicoesPendentes;
    private List<MovimentacaoResumoDTO> perdasRecentes;
    private List<MovimentacaoResumoDTO> transferenciasRecentes;
}
