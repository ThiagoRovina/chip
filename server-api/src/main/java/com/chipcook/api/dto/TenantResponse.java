package com.chipcook.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TenantResponse {

    private UUID id;
    private String tenantId;
    private String name;
    private String subdomain;
    private Boolean active;
    private LocalDateTime createdAt;
}
