package com.chipcook.api.pedido.service;

import com.chipcook.api.domain.TenantContext;
import com.chipcook.api.estoque.enums.CategoriaEstoque;
import com.chipcook.api.estoque.model.EstoqueItem;
import com.chipcook.api.estoque.repository.EstoqueRepository;
import com.chipcook.api.estoque.service.EstoqueService;
import com.chipcook.api.pedido.dto.CozinhaIngredienteDTO;
import com.chipcook.api.pedido.dto.CozinhaItemPedidoDTO;
import com.chipcook.api.pedido.dto.PedidoCozinhaDTO;
import com.chipcook.api.pedido.dto.PedidoDTO;
import com.chipcook.api.pedido.model.ItemPedido;
import com.chipcook.api.pedido.model.Pedido;
import com.chipcook.api.pedido.repository.PedidoRepository;
import com.chipcook.api.produto.model.Produto;
import com.chipcook.api.produto.repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PedidoService {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private EstoqueRepository estoqueRepository;

    @Autowired
    private EstoqueService estoqueService;

    @Autowired
    private ProdutoRepository produtoRepository;

    private static final String STATUS_CONCLUIDO = "concluido";
    private static final String STATUS_PREPARANDO = "preparando";

    public List<Pedido> listarPedidosAtivos() {
        String tenantId = TenantContext.getTenantId();
        return pedidoRepository.findByTenantIdAndStatusNot(tenantId, STATUS_CONCLUIDO);
    }

    public List<PedidoCozinhaDTO> listarPedidosCozinha() {
        estoqueService.garantirItensCenario();
        return listarPedidosAtivos().stream()
                .map(this::montarPedidoCozinha)
                .collect(Collectors.toList());
    }

    @Transactional
    public Pedido criarPedido(PedidoDTO dto) {
        Pedido pedido = new Pedido();
        pedido.setMesa(dto.getMesa());
        pedido.setCliente(dto.getCliente());
        
        List<ItemPedido> itens = dto.getItens().stream().map(itemDto -> {
            ItemPedido item = new ItemPedido();
            item.setProdutoId(itemDto.getProdutoId());
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
        Pedido pedido = buscarPedidoPorId(id);
        pedido.setStatus(novoStatus);
        return pedidoRepository.save(pedido);
    }

    @Transactional
    public Pedido iniciarPreparo(Long id) {
        Pedido pedido = buscarPedidoPorId(id);
        pedido.setStatus(STATUS_PREPARANDO);
        return pedidoRepository.save(pedido);
    }

    @Transactional
    public Pedido concluirPedidoComBaixa(Long id) {
        estoqueService.garantirItensCenario();
        Pedido pedido = buscarPedidoPorId(id);
        String tenantId = TenantContext.getTenantId();

        Map<Long, Double> consumosPorItem = new LinkedHashMap<>();

        for (ItemPedido itemPedido : pedido.getItens()) {
            for (ReceitaIngredienteConfig receita : obterReceita(itemPedido)) {
                EstoqueItem itemEstoque = resolverItemEstoque(tenantId, receita);

                double quantidadeNecessaria = receita.quantidadePorUnidade() * itemPedido.getQuantidade();
                double disponivel = itemEstoque.getQuantidade() == null ? 0.0 : itemEstoque.getQuantidade();

                if (disponivel < quantidadeNecessaria) {
                    throw new IllegalArgumentException(
                            "Estoque insuficiente para " + receita.nomeInsumo()
                                    + ". Necessário: " + formatarQuantidade(quantidadeNecessaria)
                                    + " " + itemEstoque.getUnidade()
                                    + ", disponível: " + formatarQuantidade(disponivel)
                                    + " " + itemEstoque.getUnidade()
                    );
                }

                consumosPorItem.merge(itemEstoque.getId(), quantidadeNecessaria, Double::sum);
            }
        }

        for (Map.Entry<Long, Double> consumo : consumosPorItem.entrySet()) {
            EstoqueItem itemEstoque = estoqueRepository.findByIdAndTenantId(consumo.getKey(), tenantId)
                    .orElseThrow(() -> new IllegalArgumentException("Item de estoque não encontrado"));
            itemEstoque.setQuantidade(itemEstoque.getQuantidade() - consumo.getValue());
            estoqueRepository.save(itemEstoque);
        }

        pedido.setStatus(STATUS_CONCLUIDO);
        return pedidoRepository.save(pedido);
    }

    private Pedido buscarPedidoPorId(Long id) {
        String tenantId = TenantContext.getTenantId();
        return pedidoRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));
    }

    private PedidoCozinhaDTO montarPedidoCozinha(Pedido pedido) {
        String tenantId = TenantContext.getTenantId();
        List<String> pendencias = new ArrayList<>();

        List<CozinhaItemPedidoDTO> itens = pedido.getItens().stream()
                .map(item -> montarItemCozinha(tenantId, item, pendencias))
                .collect(Collectors.toList());

        boolean estoqueDisponivel = pendencias.isEmpty();

        return PedidoCozinhaDTO.builder()
                .id(pedido.getId())
                .mesa(pedido.getMesa())
                .cliente(pedido.getCliente())
                .dataHora(pedido.getDataHora())
                .status(pedido.getStatus())
                .estoqueDisponivel(estoqueDisponivel)
                .pendenciasEstoque(pendencias)
                .itens(itens)
                .build();
    }

    private CozinhaItemPedidoDTO montarItemCozinha(String tenantId, ItemPedido item, List<String> pendencias) {
        List<CozinhaIngredienteDTO> ingredientes = obterReceita(item).stream()
                .map(receita -> montarIngredienteCozinha(tenantId, item, receita, pendencias))
                .collect(Collectors.toList());

        boolean estoqueDisponivel = ingredientes.stream().allMatch(CozinhaIngredienteDTO::isDisponivel);

        return CozinhaItemPedidoDTO.builder()
                .id(item.getId())
                .produtoId(item.getProdutoId())
                .nomeProduto(item.getNomeProduto())
                .quantidade(item.getQuantidade())
                .observacao(item.getObservacao())
                .estoqueDisponivel(estoqueDisponivel)
                .ingredientes(ingredientes)
                .build();
    }

    private CozinhaIngredienteDTO montarIngredienteCozinha(String tenantId,
                                                           ItemPedido itemPedido,
                                                           ReceitaIngredienteConfig receita,
                                                           List<String> pendencias) {
        double quantidadeNecessaria = receita.quantidadePorUnidade() * itemPedido.getQuantidade();
        EstoqueItem itemEstoque = buscarItemEstoque(tenantId, receita);

        double disponivel = itemEstoque == null || itemEstoque.getQuantidade() == null ? 0.0 : itemEstoque.getQuantidade();
        String unidade = itemEstoque == null ? receita.unidade() : itemEstoque.getUnidade();
        boolean disponivelNoEstoque = disponivel >= quantidadeNecessaria;

        if (!disponivelNoEstoque) {
            pendencias.add(
                    itemPedido.getNomeProduto() + ": falta "
                            + receita.nomeInsumo()
                            + " no estoque. Necessário "
                            + formatarQuantidade(quantidadeNecessaria)
                            + " " + unidade
                            + " e disponível "
                            + formatarQuantidade(disponivel)
                            + " " + unidade
            );
        }

        return new CozinhaIngredienteDTO(
                itemEstoque == null ? null : itemEstoque.getId(),
                receita.nomeInsumo(),
                quantidadeNecessaria,
                disponivel,
                unidade,
                disponivelNoEstoque
        );
    }

    private List<ReceitaIngredienteConfig> obterReceita(ItemPedido itemPedido) {
        Produto produto = obterProduto(itemPedido);

        if (produto != null && produto.getIngredientes() != null && !produto.getIngredientes().isEmpty()) {
            return produto.getIngredientes().stream()
                    .filter(ingrediente -> ingrediente.getNomeItemEstoque() != null && !ingrediente.getNomeItemEstoque().isBlank())
                    .map(ingrediente -> new ReceitaIngredienteConfig(
                            ingrediente.getEstoqueItemId(),
                            ingrediente.getNomeItemEstoque(),
                            ingrediente.getQuantidade() == null || ingrediente.getQuantidade() <= 0 ? 1.0 : ingrediente.getQuantidade(),
                            ingrediente.getUnidade()
                    ))
                    .toList();
        }

        if (itemPedido.getNomeProduto() != null
                && ("Pizza Margherita".equalsIgnoreCase(itemPedido.getNomeProduto())
                || "Pizza".equalsIgnoreCase(itemPedido.getNomeProduto()))) {
            return List.of(new ReceitaIngredienteConfig(null, "Farinha de Trigo", 1.0, "kg"));
        }

        return List.of();
    }

    private Produto obterProduto(ItemPedido itemPedido) {
        if (itemPedido.getProdutoId() == null) {
            return null;
        }

        String tenantId = TenantContext.getTenantId();
        return produtoRepository.findByIdAndTenantId(itemPedido.getProdutoId(), tenantId).orElse(null);
    }

    private EstoqueItem resolverItemEstoque(String tenantId, ReceitaIngredienteConfig receita) {
        EstoqueItem itemEstoque = buscarItemEstoque(tenantId, receita);
        if (itemEstoque == null) {
            throw new IllegalArgumentException("Insumo " + receita.nomeInsumo() + " não encontrado no estoque");
        }
        return itemEstoque;
    }

    private EstoqueItem buscarItemEstoque(String tenantId, ReceitaIngredienteConfig receita) {
        if (receita.estoqueItemId() != null) {
            return estoqueRepository.findByIdAndTenantId(receita.estoqueItemId(), tenantId).orElse(null);
        }

        List<EstoqueItem> encontrados = estoqueRepository.findByTenantIdAndNomeIgnoreCase(tenantId, receita.nomeInsumo());
        if (encontrados.isEmpty()) {
            return null;
        }

        return encontrados.stream()
                .filter(item -> item.getCategoriaEstoque() == CategoriaEstoque.PORCAO)
                .findFirst()
                .orElse(encontrados.get(0));
    }

    private String formatarQuantidade(double quantidade) {
        if (Math.floor(quantidade) == quantidade) {
            return String.format("%.0f", quantidade);
        }
        return String.format("%.2f", quantidade);
    }

    private record ReceitaIngredienteConfig(Long estoqueItemId, String nomeInsumo, double quantidadePorUnidade, String unidade) {
    }
}
