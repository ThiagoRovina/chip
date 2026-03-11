package com.chipcook.api.usuario.dto;

import com.chipcook.api.funcionario.dto.FuncionarioDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegistrarUsuarioDTO {
    private String nmUsuario;
    private String nmEmailUsuario;
    private String dsSenhaUsuario;
    private String fotoPerfilUsuario;
    private FuncionarioDTO funcionario;
}
