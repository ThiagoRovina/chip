package com.chipcook.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateTenantRequest {

    @NotBlank(message = "Tenant ID é obrigatório")
    @Pattern(regexp = "^[a-z0-9_]+$")
    @Size(max = 63, message = "Máximo 63 caracteres")
    private String tenantId;

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 255, message = "Máximo 255 caracteres")
    private String name;

    @NotBlank(message = "Subdomain é obrigatório")
    @Pattern(regexp = "[a-z0-9_]+$")
    @Size(max = 63, message = "Máximo 63 caracteres")
    private String subdomain;
}
