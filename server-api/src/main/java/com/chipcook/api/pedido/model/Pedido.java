package com.chipcook.api.pedido.model;

import com.chipcook.api.domain.TenantContext;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "tb_pedido")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String mesa;
    private String cliente;
    private LocalDateTime dataHora;
    private String status; // novo, preparando, pronto

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemPedido> itens = new ArrayList<>();

    @Column(name = "tenant_id")
    private String tenantId;

    @PrePersist
    public void prePersist() {
        this.dataHora = LocalDateTime.now();
        if (this.status == null) {
            this.status = "novo";
        }
        this.tenantId = TenantContext.getTenantId();
    }
}
