package com.chipcook.api.funcionario.model;

import com.chipcook.api.domain.Tenant;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "FUNCIONARIO")
public class Funcionario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID_FUNCIONARIO")
    private UUID idFuncionario;

    @Column(name = "NM_FUNCIONARIO")
    private String nmFuncionario;

    @Column(name = "DS_CARGO")
    private String dsCargo;

    @Column(name = "DT_NASCIMENTO")
    private LocalDate dtNascimento;

    @Column(name = "DT_ADMISSAO")
    private LocalDate dtAdmissao;

    @Column(name = "TP_CONTRATO")
    private String tpContrato;

    @Column(name = "DT_SAIDA")
    private LocalDate dtSaida;

    @Column(name = "NR_CPF")
    private String nrCpf;

    @Column(name = "NR_RG")
    private String nrRg;

    @Column(name = "NM_MAE")
    private String nmMae;

    @Column(name = "DS_FUNCAO")
    private String dsFuncao;

    @Column(name = "TP_CONTRATACAO")
    private String tpContratacao;

    @Column(name = "DS_SETOR")
    private String dsSetor;

    @Column(name = "DS_UNIDADE_TRABALHO")
    private String dsUnidadeTrabalho;

    @Column(name = "NM_EMAIL")
    private String nmEmail;

    @Column(name = "NR_TELEFONE")
    private String nrTelefone;

    @Column(name = "NR_CEP")
    private String nrCep;

    @Column(name = "NM_RUA")
    private String nmRua;

    @Column(name = "NR_ENDERECO")
    private String nrEndereco;

    @Column(name = "NM_BAIRRO")
    private String nmBairro;

    @Column(name = "NM_CIDADE")
    private String nmCidade;

    @Column(name = "SG_ESTADO")
    private String sgEstado;

    @ManyToOne
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    public Funcionario() {}
}
