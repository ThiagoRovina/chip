package com.chipcook.api.produto.service;

import com.chipcook.api.domain.TenantContext;
import com.chipcook.api.produto.model.Produto;
import com.chipcook.api.produto.repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProdutoService {

    @Autowired
    private ProdutoRepository produtoRepository;

    public List<Produto> listar() {
        String tenantId = TenantContext.getTenantId();
        List<Produto> produtos = produtoRepository.findByTenantId(tenantId);

        // Seed inicial se vazio
        if (produtos.isEmpty()) {
            criarSeed("Pizza Margherita", "Pizza tradicional preparada com insumos da montagem", new BigDecimal("42.00"), "Pizzas", "🍕");
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
        // TenantId é setado no PrePersist
        return produtoRepository.save(produto);
    }

    public Produto atualizar(Long id, Produto produtoAtualizado) {
        Produto produtoExistente = buscarPorId(id);
        
        produtoExistente.setNome(produtoAtualizado.getNome());
        produtoExistente.setDescricao(produtoAtualizado.getDescricao());
        produtoExistente.setPreco(produtoAtualizado.getPreco());
        produtoExistente.setCategoria(produtoAtualizado.getCategoria());
        produtoExistente.setImagem(produtoAtualizado.getImagem());
        produtoExistente.setDisponivel(produtoAtualizado.getDisponivel());

        return produtoRepository.save(produtoExistente);
    }

    public void excluir(Long id) {
        Produto produto = buscarPorId(id);
        produtoRepository.delete(produto);
    }

    private void criarSeed(String nome, String desc, BigDecimal preco, String cat, String img) {
        Produto p = new Produto();
        p.setNome(nome);
        p.setDescricao(desc);
        p.setPreco(preco);
        p.setCategoria(cat);
        p.setImagem(img);
        p.setDisponivel(true);
        produtoRepository.save(p);
    }

    private boolean garantirProdutoCenario(List<Produto> produtos, String nome, String desc, BigDecimal preco, String cat, String img) {
        boolean existe = produtos.stream().anyMatch(produto -> nome.equalsIgnoreCase(produto.getNome()));
        if (!existe) {
            criarSeed(nome, desc, preco, cat, img);
            return true;
        }
        return false;
    }
}
