package com.chipcook.api.produto.service;

import com.chipcook.api.domain.TenantContext;
import com.chipcook.api.produto.model.Produto;
import com.chipcook.api.produto.model.ProdutoIngrediente;
import com.chipcook.api.produto.model.ProdutoPassoReceita;
import com.chipcook.api.produto.repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
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
            criarSeedXbacon();
            criarSeed("Coca-Cola Lata", "Refrigerante 350ml", new BigDecimal("6.00"), "Bebidas", "🥤");
            criarSeed("Batata Frita", "Porção média", new BigDecimal("15.00"), "Acompanhamentos", "🍟");
            return produtoRepository.findByTenantId(tenantId);
        }

        return produtos;
    }

    public Produto buscarPorId(Long id) {
        String tenantId = TenantContext.getTenantId();
        return produtoRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));
    }

    public Produto atualizar(Long id, Produto produtoAtualizado) {
        Produto produtoExistente = buscarPorId(id);
        
        produtoExistente.setNome(produtoAtualizado.getNome());
        produtoExistente.setDescricao(produtoAtualizado.getDescricao());
        produtoExistente.setPreco(produtoAtualizado.getPreco());
        produtoExistente.setCategoria(produtoAtualizado.getCategoria());
        produtoExistente.setImagem(produtoAtualizado.getImagem());
        produtoExistente.setDisponivel(produtoAtualizado.getDisponivel());
        produtoExistente.setIngredientes(normalizarIngredientes(produtoAtualizado.getIngredientes()));
        produtoExistente.setPassos(normalizarPassos(produtoAtualizado.getPassos()));

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

    private void criarSeedXbacon() {
        Produto p = new Produto();
        p.setNome("X-Bacon");
        p.setDescricao("Hambúrguer com bacon crocante");
        p.setPreco(new BigDecimal("25.00"));
        p.setCategoria("Lanches");
        p.setImagem("🍔");
        p.setDisponivel(true);
        p.setIngredientes(normalizarIngredientes(List.of(
                ingrediente("1 Pão de Hambúrguer"),
                ingrediente("1 Hambúrguer 180g"),
                ingrediente("2 Fatias de Bacon"),
                ingrediente("1 Fatia de Queijo Cheddar"),
                ingrediente("Maionese da Casa"),
                ingrediente("Alface e Tomate")
        )));
        p.setPassos(normalizarPassos(List.of(
                passo("Selar o pão na chapa com manteiga.", 10, ""),
                passo("Grelhar o hambúrguer (3 min cada lado).", 15, ""),
                passo("Fritar o bacon até ficar crocante.", 10, ""),
                passo("Derreter o queijo sobre a carne.", 5, ""),
                passo("Montar: pão, carne, queijo, bacon e salada.", 10, "")
        )));
        produtoRepository.save(p);
    }

    public Produto salvar(Produto produto) {
        produto.setIngredientes(normalizarIngredientes(produto.getIngredientes()));
        produto.setPassos(normalizarPassos(produto.getPassos()));
        return produtoRepository.save(produto);
    }

    private List<ProdutoIngrediente> normalizarIngredientes(List<ProdutoIngrediente> ingredientes) {
        if (ingredientes == null) {
            return new ArrayList<>();
        }

        return ingredientes.stream()
                .filter(item -> item != null && item.getValor() != null && !item.getValor().isBlank())
                .map(item -> {
                    ProdutoIngrediente ingrediente = new ProdutoIngrediente();
                    ingrediente.setValor(item.getValor().trim());
                    return ingrediente;
                })
                .toList();
    }

    private List<ProdutoPassoReceita> normalizarPassos(List<ProdutoPassoReceita> passos) {
        if (passos == null) {
            return new ArrayList<>();
        }

        return passos.stream()
                .filter(passo -> passo != null && passo.getDescricao() != null && !passo.getDescricao().isBlank())
                .map(passo -> {
                    ProdutoPassoReceita novoPasso = new ProdutoPassoReceita();
                    novoPasso.setDescricao(passo.getDescricao().trim());
                    novoPasso.setTempoSegundos(passo.getTempoSegundos() == null ? 0 : Math.max(passo.getTempoSegundos(), 0));
                    novoPasso.setVideoUrl(passo.getVideoUrl() == null ? "" : passo.getVideoUrl().trim());
                    return novoPasso;
                })
                .toList();
    }

    private ProdutoIngrediente ingrediente(String valor) {
        ProdutoIngrediente ingrediente = new ProdutoIngrediente();
        ingrediente.setValor(valor);
        return ingrediente;
    }

    private ProdutoPassoReceita passo(String descricao, Integer tempoSegundos, String videoUrl) {
        ProdutoPassoReceita passo = new ProdutoPassoReceita();
        passo.setDescricao(descricao);
        passo.setTempoSegundos(tempoSegundos);
        passo.setVideoUrl(videoUrl);
        return passo;
    }
}
