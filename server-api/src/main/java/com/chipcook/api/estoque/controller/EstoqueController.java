package com.chipcook.api.estoque.controller;

import com.chipcook.api.estoque.dto.ConsumoMontagemDTO;
import com.chipcook.api.estoque.dto.MovimentacaoDTO;
import com.chipcook.api.estoque.dto.ProducaoPorcaoDTO;
import com.chipcook.api.estoque.dto.TransferenciaEstoqueDTO;
import com.chipcook.api.estoque.enums.CategoriaEstoque;
import com.chipcook.api.estoque.model.EstoqueItem;
import com.chipcook.api.estoque.service.EstoqueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/estoque")
@RequiredArgsConstructor
public class EstoqueController {

    private final EstoqueService estoqueService;

    @GetMapping
    public ResponseEntity<List<EstoqueItem>> listar(@RequestParam(required = false) String categoria) {
        if (categoria != null && !categoria.isEmpty()) {
            try {
                CategoriaEstoque categoriaEstoque = CategoriaEstoque.from(categoria);
                return ResponseEntity.ok(estoqueService.listarPorCategoria(categoriaEstoque));
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().build();
            }
        }
        return ResponseEntity.ok(estoqueService.listarItens());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EstoqueItem> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(estoqueService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<?> criar(@RequestBody EstoqueItem item,
                                   @RequestHeader(value = "X-Perfil-Operacional", required = false) String perfil) {
        try {
            return ResponseEntity.ok(estoqueService.criar(item, perfil));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable Long id,
                                       @RequestBody EstoqueItem item,
                                       @RequestHeader(value = "X-Perfil-Operacional", required = false) String perfil) {
        try {
            return ResponseEntity.ok(estoqueService.atualizar(id, item, perfil));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluir(@PathVariable Long id,
                                     @RequestHeader(value = "X-Perfil-Operacional", required = false) String perfil) {
        try {
            estoqueService.excluir(id, perfil);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/movimentar")
    public ResponseEntity<?> movimentar(@PathVariable Long id, @RequestBody MovimentacaoDTO dto) {
        try {
            return ResponseEntity.ok(estoqueService.movimentar(id, dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/producoes")
    public ResponseEntity<?> registrarProducao(@RequestBody ProducaoPorcaoDTO dto) {
        try {
            return ResponseEntity.ok(estoqueService.registrarProducao(dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/montagem/consumos")
    public ResponseEntity<?> registrarConsumoMontagem(@RequestBody ConsumoMontagemDTO dto) {
        try {
            return ResponseEntity.ok(estoqueService.registrarConsumoMontagem(dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/transferencias")
    public ResponseEntity<?> transferirEntreEstoques(@RequestBody TransferenciaEstoqueDTO dto) {
        try {
            estoqueService.transferirDoEstoqueGeral(dto);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
