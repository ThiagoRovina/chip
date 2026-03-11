package com.chipcook.api.usuario.dto;

import java.util.UUID;

public record UsuarioDTO(UUID idUsuario, String nmUsuario, String nmEmailUsuario, String dsSenhaUsuario, String fotoPerfilUsuario, String tenantId) {
}
