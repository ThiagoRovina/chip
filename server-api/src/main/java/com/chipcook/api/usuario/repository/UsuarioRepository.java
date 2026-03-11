package com.chipcook.api.usuario.repository;

import com.chipcook.api.domain.Tenant;
import com.chipcook.api.usuario.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {
    Optional<Usuario> findByNmEmailUsuarioAndTenant(String nmEmailUsuario, Tenant tenant);
    List<Usuario> findAllByTenant(Tenant tenant);
}
