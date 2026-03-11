package com.chipcook.api.estoque.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ProducaoPorcaoDTO {
    private Long itemPorcaoId;
    private Double quantidadeProduzida;
    private String perfil;
    private List<ConsumoInternoDTO> consumosInternos = new ArrayList<>();
}
