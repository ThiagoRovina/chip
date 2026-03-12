package com.chipcook.api.pedido.controller;

import com.chipcook.api.pedido.dto.PedidoCozinhaDTO;
import com.chipcook.api.pedido.dto.PedidoDTO;
import com.chipcook.api.pedido.model.Pedido;
import com.chipcook.api.pedido.service.PedidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    @Autowired
    private PedidoService pedidoService;

    @GetMapping
    public ResponseEntity<List<Pedido>> listar() {
        return ResponseEntity.ok(pedidoService.listarPedidosAtivos());
    }

    @GetMapping("/cozinha")
    public ResponseEntity<List<PedidoCozinhaDTO>> listarCozinha() {
        return ResponseEntity.ok(pedidoService.listarPedidosCozinha());
    }

    @PostMapping
    public ResponseEntity<?> criar(@RequestBody PedidoDTO dto) {
        try {
            return ResponseEntity.ok(pedidoService.criarPedido(dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/iniciar")
    public ResponseEntity<Pedido> iniciar(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoService.iniciarPreparo(id));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Pedido> atualizarStatus(@PathVariable Long id, @RequestBody String status) {
        return ResponseEntity.ok(pedidoService.atualizarStatus(id, status));
    }

    @PutMapping("/{id}/pronto")
    public ResponseEntity<?> concluirComBaixa(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(pedidoService.concluirPedidoComBaixa(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
