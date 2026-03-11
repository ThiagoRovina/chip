package com.chipcook.api.estoque.dto;

import lombok.Data;

@Data
public class ConsumoMontagemDTO {
    private Long itemPorcaoId;
    private Double quantidade;
    private String perfil;
}
