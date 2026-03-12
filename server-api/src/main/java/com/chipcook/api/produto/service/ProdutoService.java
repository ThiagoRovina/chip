package com.chipcook.api.produto.service;

import com.chipcook.api.domain.TenantContext;
import com.chipcook.api.estoque.enums.CategoriaEstoque;
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
            return produtoRepository.findByTenantId(tenantId);
        }

        boolean alterado = garantirProdutoCenario(produtos, "Pizza Margherita", "Pizza tradicional preparada com insumos da montagem",
                new BigDecimal("42.00"), "Pizzas", "🍕");

        return alterado ? produtoRepository.findByTenantId(tenantId) : produtos;
    }

    public Produto buscarPorId(Long id) {
        String tenantId = TenantContext.getTenantId();
        return produtoRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));
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

        List<EstoqueItem> encontrados = estoqueRepository.findByTenantIdAndNomeIgnoreCase(tenantId, nomeIngrediente.trim());
        if (encontrados.isEmpty()) {
            throw new IllegalArgumentException("Ingrediente não encontrado no estoque");
        }

        return encontrados.stream()
                .filter(item -> item.getCategoriaEstoque() == CategoriaEstoque.PORCAO)
                .findFirst()
                .orElse(encontrados.get(0));
    }

    private ProdutoIngrediente criarIngredienteSeed(String nomeItemEstoque, Double quantidade) {
        ProdutoIngrediente ingrediente = new ProdutoIngrediente();
        ingrediente.setNomeItemEstoque(nomeItemEstoque);
        ingrediente.setQuantidade(quantidade);
        return ingrediente;
    }
}
