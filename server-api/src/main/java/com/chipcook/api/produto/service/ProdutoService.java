package com.chipcook.api.produto.service;

import com.chipcook.api.domain.TenantContext;
import com.chipcook.api.estoque.model.EstoqueItem;
import com.chipcook.api.estoque.repository.EstoqueRepository;
import com.chipcook.api.estoque.service.EstoqueService;
import com.chipcook.api.produto.model.Produto;
import com.chipcook.api.produto.model.ProdutoIngrediente;
import com.chipcook.api.produto.model.ProdutoPassoReceita;
import com.chipcook.api.produto.repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class ProdutoService {

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private EstoqueRepository estoqueRepository;

    @Autowired
    private EstoqueService estoqueService;

    public List<Produto> listar() {
        String tenantId = TenantContext.getTenantId();
        estoqueService.garantirItensCenario();
        List<Produto> produtos = produtoRepository.findByTenantId(tenantId);

        // Seed inicial se vazio
        if (produtos.isEmpty()) {
            criarSeed("Pizza Margherita", "Pizza tradicional preparada com insumos da montagem", new BigDecimal("42.00"), "Pizzas", "🍕",
                    List.of(criarIngredienteSeed("Farinha de Trigo", 1.0)));
            criarSeed("X-Bacon", "Hambúrguer com bacon crocante", new BigDecimal("25.00"), "Lanches", "🍔");
            criarSeed("Coca-Cola Lata", "Refrigerante 350ml", new BigDecimal("6.00"), "Bebidas", "🥤");
            criarSeed("Batata Frita", "Porção média", new BigDecimal("15.00"), "Acompanhamentos", "🍟");
            produtos = produtoRepository.findByTenantId(tenantId);
        }

        boolean alterado = garantirProdutoCenario(produtos, "Pizza Margherita", "Pizza tradicional preparada com insumos da montagem",
                new BigDecimal("42.00"), "Pizzas", "🍕");

        List<Produto> produtosProcessados = alterado ? produtoRepository.findByTenantId(tenantId) : produtos;
        produtosProcessados.forEach(this::enriquecerDisponibilidade);
        return produtosProcessados;
    }

    public Produto buscarPorId(Long id) {
        String tenantId = TenantContext.getTenantId();
        Produto produto = produtoRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));
        return enriquecerDisponibilidade(produto);
    }

    public Produto buscarPorIdParaVenda(Long id) {
        Produto produto = buscarPorId(id);
        if (!Boolean.TRUE.equals(produto.getDisponivelVenda())) {
            String motivo = produto.getMotivoIndisponibilidade() == null || produto.getMotivoIndisponibilidade().isBlank()
                    ? "Produto indisponível para venda"
                    : produto.getMotivoIndisponibilidade();
            throw new IllegalArgumentException(motivo);
        }
        return produto;
    }

    public Produto salvar(Produto produto) {
        return produtoRepository.save(prepararProduto(produto));
    }

    public Produto atualizar(Long id, Produto produtoAtualizado) {
        Produto produtoExistente = buscarPorId(id);
        Produto produtoSanitizado = prepararProduto(produtoAtualizado);
        
        produtoExistente.setNome(produtoSanitizado.getNome());
        produtoExistente.setDescricao(produtoSanitizado.getDescricao());
        produtoExistente.setPreco(produtoSanitizado.getPreco());
        produtoExistente.setCategoria(produtoSanitizado.getCategoria());
        produtoExistente.setImagem(produtoSanitizado.getImagem());
        produtoExistente.setDisponivel(produtoSanitizado.getDisponivel());
        produtoExistente.setIngredientes(produtoSanitizado.getIngredientes());
        produtoExistente.setPassos(produtoSanitizado.getPassos());

        return produtoRepository.save(produtoExistente);
    }

    public void excluir(Long id) {
        Produto produto = buscarPorId(id);
        produtoRepository.delete(produto);
    }

    private void criarSeed(String nome, String desc, BigDecimal preco, String cat, String img) {
        criarSeed(nome, desc, preco, cat, img, List.of());
    }

    private void criarSeed(String nome, String desc, BigDecimal preco, String cat, String img,
                           List<ProdutoIngrediente> ingredientes) {
        Produto p = new Produto();
        p.setNome(nome);
        p.setDescricao(desc);
        p.setPreco(preco);
        p.setCategoria(cat);
        p.setImagem(img);
        p.setDisponivel(true);
        p.setIngredientes(ingredientes);
        produtoRepository.save(prepararProduto(p));
    }

    private boolean garantirProdutoCenario(List<Produto> produtos, String nome, String desc, BigDecimal preco, String cat, String img) {
        boolean existe = produtos.stream().anyMatch(produto -> nome.equalsIgnoreCase(produto.getNome()));
        if (!existe) {
            List<ProdutoIngrediente> ingredientes = "Pizza Margherita".equalsIgnoreCase(nome)
                    ? List.of(criarIngredienteSeed("Farinha de Trigo", 1.0))
                    : List.of();
            criarSeed(nome, desc, preco, cat, img, ingredientes);
            return true;
        }
        return false;
    }

    private Produto prepararProduto(Produto produto) {
        produto.setIngredientes(
                (produto.getIngredientes() == null ? List.<ProdutoIngrediente>of() : produto.getIngredientes()).stream()
                        .map(this::sanitizarIngrediente)
                        .filter(Objects::nonNull)
                        .collect(java.util.stream.Collectors.toCollection(ArrayList::new))
        );

        produto.setPassos(
                (produto.getPassos() == null ? List.<ProdutoPassoReceita>of() : produto.getPassos()).stream()
                        .filter(Objects::nonNull)
                        .filter(passo -> passo.getDescricao() != null && !passo.getDescricao().isBlank())
                        .peek(passo -> {
                            if (passo.getTempoSegundos() == null || passo.getTempoSegundos() < 0) {
                                passo.setTempoSegundos(0);
                            }
                            passo.setVideoUrl(passo.getVideoUrl() == null ? "" : passo.getVideoUrl().trim());
                        })
                        .collect(java.util.stream.Collectors.toCollection(ArrayList::new))
        );

        return produto;
    }

    private ProdutoIngrediente sanitizarIngrediente(ProdutoIngrediente ingrediente) {
        if (ingrediente == null) {
            return null;
        }

        EstoqueItem itemEstoque = resolverItemEstoque(ingrediente);
        if (itemEstoque == null) {
            return null;
        }

        ProdutoIngrediente sanitizado = new ProdutoIngrediente();
        sanitizado.setEstoqueItemId(itemEstoque.getId());
        sanitizado.setNomeItemEstoque(itemEstoque.getNome());
        sanitizado.setUnidade(itemEstoque.getUnidade());
        sanitizado.setQuantidade(ingrediente.getQuantidade() == null || ingrediente.getQuantidade() <= 0 ? 1.0 : ingrediente.getQuantidade());
        return sanitizado;
    }

    private EstoqueItem resolverItemEstoque(ProdutoIngrediente ingrediente) {
        String tenantId = TenantContext.getTenantId();

        if (ingrediente.getEstoqueItemId() != null) {
            return estoqueRepository.findByIdAndTenantId(ingrediente.getEstoqueItemId(), tenantId)
                    .orElseThrow(() -> new IllegalArgumentException("Ingrediente não encontrado no estoque"));
        }

        String nomeIngrediente = ingrediente.getNomeItemEstoque();
        if (nomeIngrediente == null || nomeIngrediente.isBlank()) {
            return null;
        }

        return estoqueRepository.findByTenantIdAndNomeIgnoreCase(tenantId, nomeIngrediente.trim()).stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Ingrediente não encontrado no estoque"));
    }

    private ProdutoIngrediente criarIngredienteSeed(String nomeItemEstoque, Double quantidade) {
        ProdutoIngrediente ingrediente = new ProdutoIngrediente();
        ingrediente.setNomeItemEstoque(nomeItemEstoque);
        ingrediente.setQuantidade(quantidade);
        return ingrediente;
    }

    public Produto enriquecerDisponibilidade(Produto produto) {
        if (produto == null) {
            return null;
        }

        boolean estoqueDisponivel = true;
        Integer quantidadeMaximaDisponivel = null;
        String motivoIndisponibilidade = null;

        if (produto.getIngredientes() != null && !produto.getIngredientes().isEmpty()) {
            int capacidade = Integer.MAX_VALUE;

            for (ProdutoIngrediente ingrediente : produto.getIngredientes()) {
                if (ingrediente == null) {
                    continue;
                }

                Optional<EstoqueItem> itemEstoque = localizarItemEstoque(ingrediente);
                double quantidadePorProduto = ingrediente.getQuantidade() == null || ingrediente.getQuantidade() <= 0
                        ? 1.0
                        : ingrediente.getQuantidade();

                if (itemEstoque.isEmpty()) {
                    estoqueDisponivel = false;
                    capacidade = 0;
                    motivoIndisponibilidade = "Ingrediente " + ingrediente.getNomeItemEstoque() + " não encontrado no estoque";
                    break;
                }

                double quantidadeAtual = itemEstoque.get().getQuantidade() == null ? 0.0 : itemEstoque.get().getQuantidade();
                int capacidadeIngrediente = (int) Math.floor(quantidadeAtual / quantidadePorProduto);
                capacidade = Math.min(capacidade, capacidadeIngrediente);

                if (capacidadeIngrediente <= 0 && motivoIndisponibilidade == null) {
                    motivoIndisponibilidade = "Falta " + itemEstoque.get().getNome() + " no estoque";
                }
            }

            quantidadeMaximaDisponivel = capacidade == Integer.MAX_VALUE ? 0 : Math.max(capacidade, 0);
            estoqueDisponivel = quantidadeMaximaDisponivel > 0;
        }

        boolean disponivelConfigurado = Boolean.TRUE.equals(produto.getDisponivel());
        produto.setEstoqueDisponivel(estoqueDisponivel);
        produto.setDisponivelVenda(disponivelConfigurado && estoqueDisponivel);
        produto.setQuantidadeMaximaDisponivel(quantidadeMaximaDisponivel);
        produto.setMotivoIndisponibilidade(
                !disponivelConfigurado ? "Produto marcado como indisponível no cardápio" : motivoIndisponibilidade
        );
        return produto;
    }

    private Optional<EstoqueItem> localizarItemEstoque(ProdutoIngrediente ingrediente) {
        String tenantId = TenantContext.getTenantId();

        if (ingrediente.getEstoqueItemId() != null) {
            return estoqueRepository.findByIdAndTenantId(ingrediente.getEstoqueItemId(), tenantId);
        }

        if (ingrediente.getNomeItemEstoque() == null || ingrediente.getNomeItemEstoque().isBlank()) {
            return Optional.empty();
        }

        return estoqueRepository.findByTenantIdAndNomeIgnoreCase(tenantId, ingrediente.getNomeItemEstoque().trim())
                .stream()
                .findFirst();
    }
}
