package com.chipcook.api.estoque.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum PerfilOperacionalEstoque {
    DONO("dono"),
    GERENTE("gerente"),
    CHEFE_COZINHA("chefe_cozinha"),
    PRODUCAO("producao"),
    MONTAGEM("montagem");

    private final String codigo;

    PerfilOperacionalEstoque(String codigo) {
        this.codigo = codigo;
    }

    @JsonValue
    public String toJson() {
        return codigo;
    }

    @JsonCreator
    public static PerfilOperacionalEstoque from(String valor) {
        if (valor == null || valor.isBlank()) {
            return GERENTE;
        }

        String normalizado = valor.trim()
                .replace('-', '_')
                .replace(' ', '_')
                .toLowerCase();

        return Arrays.stream(values())
                .filter(perfil -> perfil.codigo.equals(normalizado)
                        || perfil.name().equalsIgnoreCase(normalizado))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Perfil operacional inválido: " + valor));
    }
}
