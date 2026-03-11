package com.chipcook.api.usuario.dto;

import com.chipcook.api.funcionario.dto.FuncionarioDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioDetalhadoDTO {
    private UUID idUsuario;
    private String nmUsuario;
    private String nmEmailUsuario;
    private String fotoPerfilUsuario;
    private String tenantId;
    private FuncionarioDTO funcionario;
}
