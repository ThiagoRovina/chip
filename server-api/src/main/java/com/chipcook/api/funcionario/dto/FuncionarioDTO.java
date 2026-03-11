package com.chipcook.api.funcionario.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class FuncionarioDTO {
    private UUID idFuncionario;
    private String nmFuncionario;
    private String dsCargo;
    private LocalDate dtNascimento;
    private LocalDate dtAdmissao;
    private String tpContrato;
    private LocalDate dtSaida;
    private String nrCpf;
    private String nrRg;
    private String nmMae;
    private String dsFuncao;
    private String tpContratacao;
    private String dsSetor;
    private String dsUnidadeTrabalho;
    private String nmEmail;
    private String nrTelefone;
    private String nrCep;
    private String nmRua;
    private String nrEndereco;
    private String nmBairro;
    private String nmCidade;
    private String sgEstado;
}
