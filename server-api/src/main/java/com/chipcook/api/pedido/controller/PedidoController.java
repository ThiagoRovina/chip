package com.chipcook.api.pedido.controller;

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

    @PostMapping
    public ResponseEntity<Pedido> criar(@RequestBody PedidoDTO dto) {
        return ResponseEntity.ok(pedidoService.criarPedido(dto));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Pedido> atualizarStatus(@PathVariable Long id, @RequestBody String status) {
        return ResponseEntity.ok(pedidoService.atualizarStatus(id, status));
    }
}
