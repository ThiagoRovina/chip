package com.chipcook.api.pedido.service;

import com.chipcook.api.domain.TenantContext;
import com.chipcook.api.pedido.dto.ItemPedidoDTO;
import com.chipcook.api.pedido.dto.PedidoDTO;
import com.chipcook.api.pedido.model.ItemPedido;
import com.chipcook.api.pedido.model.Pedido;
import com.chipcook.api.pedido.repository.PedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PedidoService {

    @Autowired
    private PedidoRepository pedidoRepository;

    public List<Pedido> listarPedidosAtivos() {
        String tenantId = TenantContext.getTenantId();
        return pedidoRepository.findByTenantIdAndStatusNot(tenantId, "concluido");
    }

    @Transactional
    public Pedido criarPedido(PedidoDTO dto) {
        Pedido pedido = new Pedido();
        pedido.setMesa(dto.getMesa());
        pedido.setCliente(dto.getCliente());
        
        List<ItemPedido> itens = dto.getItens().stream().map(itemDto -> {
            ItemPedido item = new ItemPedido();
            item.setNomeProduto(itemDto.getNomeProduto());
            item.setQuantidade(itemDto.getQuantidade());
            item.setObservacao(itemDto.getObservacao());
            item.setPedido(pedido);
            return item;
        }).collect(Collectors.toList());

        pedido.setItens(itens);
        return pedidoRepository.save(pedido);
    }

    public Pedido atualizarStatus(Long id, String novoStatus) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));
        pedido.setStatus(novoStatus);
        return pedidoRepository.save(pedido);
    }
}
