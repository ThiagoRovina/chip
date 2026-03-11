package com.chipcook.api.usuario.service;

import com.chipcook.api.domain.Tenant;
import com.chipcook.api.domain.TenantContext;
import com.chipcook.api.funcionario.dto.FuncionarioDTO;
import com.chipcook.api.funcionario.service.FuncionarioService;
import com.chipcook.api.security.JwtTokenProvider;
import com.chipcook.api.usuario.dto.LoginResponseDTO;
import com.chipcook.api.usuario.dto.UsuarioDTO;
import com.chipcook.api.usuario.dto.UsuarioDetalhadoDTO;
import com.chipcook.api.usuario.model.Usuario;
import com.chipcook.api.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final FuncionarioService funcionarioService;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public Usuario registrarUsuario(Usuario novoUsuario, FuncionarioDTO funcionarioDTO){
        if(usuarioRepository.findByNmEmailUsuarioAndTenant(novoUsuario.getNmEmailUsuario(), novoUsuario.getTenant()).isPresent()){
            throw new IllegalArgumentException("Este Email( "+ novoUsuario.getNmEmailUsuario() + ") ja existe.");
        }
        novoUsuario.setDsSenhaUsuario(passwordEncoder.encode(novoUsuario.getDsSenhaUsuario()));
        Usuario usuarioSalvo = usuarioRepository.save(novoUsuario);

        if (funcionarioDTO != null) {
            if (TenantContext.getTenantId() == null) {
                TenantContext.setTenantId(novoUsuario.getTenant().getTenantId());
            }

            if (funcionarioDTO.getNmFuncionario() == null || funcionarioDTO.getNmFuncionario().isEmpty()) {
                funcionarioDTO.setNmFuncionario(novoUsuario.getNmUsuario());
            }
            if (funcionarioDTO.getNmEmail() == null || funcionarioDTO.getNmEmail().isEmpty()) {
                funcionarioDTO.setNmEmail(novoUsuario.getNmEmailUsuario());
            }

            funcionarioService.salvar(funcionarioDTO);
        }

        return usuarioSalvo;
    }

    public Usuario autenticar(String email, String senha, Tenant tenant) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByNmEmailUsuarioAndTenant(email, tenant);
        if (usuarioOpt.isEmpty()) {
            throw new SecurityException("Usuário não encontrado.");
        }
        Usuario usuario = usuarioOpt.get();
        if (!passwordEncoder.matches(senha, usuario.getDsSenhaUsuario())) {
            throw new SecurityException("Senha inválida.");
        }

        return usuario;
    }

    public LoginResponseDTO autenticarComToken(String email, String senha, Tenant tenant) {
        Usuario usuario = autenticar(email, senha, tenant);
        String cargo = null;

        TenantContext.setTenantId(tenant.getTenantId());
        try {
            FuncionarioDTO funcionario = funcionarioService.buscarPorEmail(usuario.getNmEmailUsuario());
            if (funcionario != null) {
                cargo = funcionario.getDsCargo();
            }
        } finally {
            TenantContext.clear();
        }

        String token = jwtTokenProvider.generateToken(
                usuario.getIdUsuario(),
                usuario.getNmEmailUsuario(),
                tenant.getTenantId(),
                usuario.getNmUsuario()
        );

        return new LoginResponseDTO(
                token,
                usuario.getIdUsuario(),
                usuario.getNmUsuario(),
                usuario.getNmEmailUsuario(),
                tenant.getTenantId(),
                cargo
        );
    }

    public List<Usuario> listarUsuarios(Tenant tenant) {
        return usuarioRepository.findAllByTenant(tenant);
    }

    public UsuarioDetalhadoDTO buscarPorId(UUID idUsuario, Tenant tenant) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(idUsuario);
        if (usuarioOpt.isEmpty()) {
            throw new IllegalArgumentException("Usuário não encontrado.");
        }
        Usuario usuario = usuarioOpt.get();
        if (!usuario.getTenant().getId().equals(tenant.getId())) {
            throw new SecurityException("Usuário não pertence a este tenant.");
        }

        TenantContext.setTenantId(tenant.getTenantId());
        try {
            FuncionarioDTO funcionarioDTO = funcionarioService.buscarPorEmail(usuario.getNmEmailUsuario());

            return new UsuarioDetalhadoDTO(
                    usuario.getIdUsuario(),
                    usuario.getNmUsuario(),
                    usuario.getNmEmailUsuario(),
                    usuario.getFotoPerfilUsuario(),
                    usuario.getTenant().getTenantId(),
                    funcionarioDTO
            );
        } finally {
            TenantContext.clear();
        }
    }

    public UsuarioDTO atualizar(UUID idUsuario, UsuarioDTO usuarioDTO, Tenant tenant){
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(idUsuario);
        if(usuarioOpt.isEmpty()){
            throw new IllegalArgumentException("Usuário não encontrado.");
        }
        Usuario usuario = usuarioOpt.get();
        if (!usuario.getTenant().getId().equals(tenant.getId())) {
             throw new SecurityException("Usuário não pertence a este tenant.");
        }
        usuario.setNmUsuario(usuarioDTO.nmUsuario());
        usuario.setNmEmailUsuario(usuarioDTO.nmEmailUsuario());
        if (usuarioDTO.dsSenhaUsuario() != null && !usuarioDTO.dsSenhaUsuario().isEmpty()) {
            usuario.setDsSenhaUsuario(passwordEncoder.encode(usuarioDTO.dsSenhaUsuario()));
        }
        if (usuarioDTO.fotoPerfilUsuario() != null) {
            usuario.setFotoPerfilUsuario(usuarioDTO.fotoPerfilUsuario());
        }
        Usuario atualizado = usuarioRepository.save(usuario);
        return new UsuarioDTO(atualizado.getIdUsuario(), atualizado.getNmUsuario(), atualizado.getNmEmailUsuario(), null, atualizado.getFotoPerfilUsuario(), atualizado.getTenant().getTenantId());
    }

    public void deletar(UUID idUsuario, Tenant tenant) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(idUsuario);
        if (usuarioOpt.isEmpty()) {
            throw new IllegalArgumentException("Usuário não encontrado.");
        }
        Usuario usuario = usuarioOpt.get();
        if (!usuario.getTenant().getId().equals(tenant.getId())) {
            throw new SecurityException("Usuário não pertence a este tenant.");
        }

        usuarioRepository.delete(usuario);
    }
}
