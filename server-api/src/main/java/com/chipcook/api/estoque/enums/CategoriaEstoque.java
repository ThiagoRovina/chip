package com.chipcook.api.estoque.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum CategoriaEstoque {
    GERAL("geral", "Estoque Geral"),
    INTERNO("interno", "Estoque Interno"),
    PORCAO_GERAL("porcao_geral", "Estoque Porção Geral"),
    PORCAO("porcao", "Estoque Porção");

    private final String codigo;
    private final String descricao;

    CategoriaEstoque(String codigo, String descricao) {
        this.codigo = codigo;
        this.descricao = descricao;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getDescricao() {
        return descricao;
    }

    @JsonValue
    public String toJson() {
        return codigo;
    }

    @JsonCreator
    public static CategoriaEstoque from(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("CategoriaEstoque não pode ser vazia");
        }

        String normalizado = valor.trim()
                .replace('-', '_')
                .replace(' ', '_')
                .toLowerCase();

        return Arrays.stream(values())
                .filter(categoria -> categoria.codigo.equals(normalizado)
                        || categoria.name().equalsIgnoreCase(normalizado))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("CategoriaEstoque inválida: " + valor));
    }
}
