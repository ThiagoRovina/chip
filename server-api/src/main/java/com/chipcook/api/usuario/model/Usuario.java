package com.chipcook.api.usuario.model;

import com.chipcook.api.domain.Tenant;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "USUARIO")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID_USUARIO")
    private UUID idUsuario;

    @Column(name = "NM_USUARIO")
    private String nmUsuario;

    @Column(name = "NM_EMAIL_USUARIO")
    private String nmEmailUsuario;

    @Column(name = "DS_SENHA_USUARIO")
    private String dsSenhaUsuario;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "FOTO_PERFIL_USUARIO", columnDefinition = "TEXT")
    private String fotoPerfilUsuario;

    @ManyToOne
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    public Usuario() {
    }
}
