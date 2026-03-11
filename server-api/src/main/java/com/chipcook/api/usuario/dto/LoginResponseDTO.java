package com.chipcook.api.usuario.dto;

import java.util.UUID;

public record LoginResponseDTO(
        String token,
        UUID usuarioId,
        String nmUsuario,
        String nmEmailUsuario,
        String tenantId,
        String cargo
) {
}
