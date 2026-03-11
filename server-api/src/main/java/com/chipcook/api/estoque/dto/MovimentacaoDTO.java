package com.chipcook.api.estoque.dto;

import lombok.Data;

@Data
public class MovimentacaoDTO {
    private String tipo; // entrada, saida, perda, contagem
    private Double quantidade;
    private String motivo; // opcional, para perda
    private String perfil; // dono, gerente, chefe_cozinha, producao, montagem
}
