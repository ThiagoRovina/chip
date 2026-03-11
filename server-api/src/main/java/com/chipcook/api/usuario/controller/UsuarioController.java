package com.chipcook.api.usuario.controller;

import com.chipcook.api.domain.Tenant;
import com.chipcook.api.domain.TenantContext;
import com.chipcook.api.repository.TenantRepository;
import com.chipcook.api.usuario.dto.*;
import com.chipcook.api.usuario.model.Usuario;
import com.chipcook.api.usuario.service.UsuarioService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/usuario")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    private final TenantRepository tenantRepository;

    private final ObjectMapper objectMapper;

    @PostMapping(value = "/registrar", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<?> registrarUsuario(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @RequestPart("dados") String dadosJson,
            @RequestPart(value = "foto", required = false) MultipartFile foto) {

        Tenant tenant = tenantRepository.findByTenantIdAndActiveTrue(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant inválido."));

        TenantContext.setTenantId(tenantId);
        
        try {
            RegistrarUsuarioDTO registrarUsuarioDTO = objectMapper.readValue(dadosJson, RegistrarUsuarioDTO.class);

            Usuario novoUsuario = new Usuario();
            novoUsuario.setNmUsuario(registrarUsuarioDTO.getNmUsuario());
            novoUsuario.setNmEmailUsuario(registrarUsuarioDTO.getNmEmailUsuario());
            novoUsuario.setDsSenhaUsuario(registrarUsuarioDTO.getDsSenhaUsuario());

            if (foto != null && !foto.isEmpty()) {
                String base64Image = "data:" + foto.getContentType() + ";base64," +
                                     Base64.getEncoder().encodeToString(foto.getBytes());
                novoUsuario.setFotoPerfilUsuario(base64Image);
            } else {
                novoUsuario.setFotoPerfilUsuario(registrarUsuarioDTO.getFotoPerfilUsuario());
            }

            novoUsuario.setTenant(tenant);

            Usuario usuarioRegistrado = usuarioService.registrarUsuario(novoUsuario, registrarUsuarioDTO.getFuncionario());
            return ResponseEntity.ok(usuarioRegistrado);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Erro ao processar dados: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } finally {
            TenantContext.clear();
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> autenticarUsuario(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @RequestBody LoginRequestDTO loginRequest) {

        Tenant tenant = tenantRepository.findByTenantIdAndActiveTrue(tenantId)
                .orElseThrow(() -> new SecurityException("Tenant inválido."));

        try {
            LoginResponseDTO response = usuarioService.autenticarComToken(
                    loginRequest.getEmail(),
                    loginRequest.getSenha(),
                    tenant
            );
            return ResponseEntity.ok(response);

        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Usuario>> listarUsuarios(@RequestHeader("X-Tenant-ID") String tenantId) {
        Tenant tenant = tenantRepository.findByTenantIdAndActiveTrue(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant inválido."));
        List<Usuario> usuarios = usuarioService.listarUsuarios(tenant);
        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/{idUsuario}")
    public ResponseEntity<?> buscarUsuarioPorId(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @PathVariable UUID idUsuario) {

        Tenant tenant = tenantRepository.findByTenantIdAndActiveTrue(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant inválido."));

        try {
            UsuarioDetalhadoDTO usuario = usuarioService.buscarPorId(idUsuario, tenant);
            return ResponseEntity.ok(usuario);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }

    @PutMapping(value = "/{idUsuario}", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<?> atualizarUsuario(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @PathVariable UUID idUsuario,
            @RequestPart("dados") String dadosJson,
            @RequestPart(value = "foto", required = false) MultipartFile foto) {

        Tenant tenant = tenantRepository.findByTenantIdAndActiveTrue(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant inválido."));
        
        try {
            UsuarioDTO usuarioDTO = objectMapper.readValue(dadosJson, UsuarioDTO.class);

            if (foto != null && !foto.isEmpty()) {
                String base64Image = "data:" + foto.getContentType() + ";base64," +
                                     Base64.getEncoder().encodeToString(foto.getBytes());
                usuarioDTO = new UsuarioDTO(
                    usuarioDTO.idUsuario(),
                    usuarioDTO.nmUsuario(),
                    usuarioDTO.nmEmailUsuario(),
                    usuarioDTO.dsSenhaUsuario(),
                    base64Image,
                    usuarioDTO.tenantId()
                );
            }

            UsuarioDTO usuarioAtualizado = usuarioService.atualizar(idUsuario, usuarioDTO, tenant);
            return ResponseEntity.ok(usuarioAtualizado);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Erro ao processar dados: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }

    @DeleteMapping("/{idUsuario}")
    public ResponseEntity<?> deletarUsuario(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @PathVariable UUID idUsuario) {

        Tenant tenant = tenantRepository.findByTenantIdAndActiveTrue(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant inválido."));

        try {
            usuarioService.deletar(idUsuario, tenant);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }
}
