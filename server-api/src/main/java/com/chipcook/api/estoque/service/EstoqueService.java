package com.chipcook.api.estoque.service;

import com.chipcook.api.domain.TenantContext;
import com.chipcook.api.estoque.dto.ConsumoInternoDTO;
import com.chipcook.api.estoque.dto.ConsumoMontagemDTO;
import com.chipcook.api.estoque.dto.MovimentacaoDTO;
import com.chipcook.api.estoque.dto.ProducaoPorcaoDTO;
import com.chipcook.api.estoque.enums.CategoriaEstoque;
import com.chipcook.api.estoque.enums.PerfilOperacionalEstoque;
import com.chipcook.api.estoque.model.EstoqueItem;
import com.chipcook.api.estoque.repository.EstoqueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class EstoqueService {

    private static final Set<PerfilOperacionalEstoque> PERFIS_GERENCIAIS = Set.of(
            PerfilOperacionalEstoque.DONO,
            PerfilOperacionalEstoque.GERENTE,
            PerfilOperacionalEstoque.CHEFE_COZINHA
    );

    private final EstoqueRepository estoqueRepository;

    public List<EstoqueItem> listarItens() {
        String tenantId = TenantContext.getTenantId();
        List<EstoqueItem> itens = estoqueRepository.findByTenantId(tenantId);

        if (itens.isEmpty()) {
            seedItensIniciais();
            return estoqueRepository.findByTenantId(tenantId);
        }

        return itens;
    }

    public List<EstoqueItem> listarPorCategoria(CategoriaEstoque categoria) {
        String tenantId = TenantContext.getTenantId();
        return estoqueRepository.findByTenantIdAndCategoriaEstoque(tenantId, categoria);
    }

    public EstoqueItem buscarPorId(Long id) {
        String tenantId = TenantContext.getTenantId();
        return estoqueRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Item não encontrado"));
    }

    public EstoqueItem criar(EstoqueItem item, String perfil) {
        PerfilOperacionalEstoque perfilOperacional = PerfilOperacionalEstoque.from(perfil);
        validarOperacaoGerencial(perfilOperacional);

        if (item.getQuantidade() == null) {
            item.setQuantidade(0.0);
        }

        validarQuantidadeNaoNegativa(item.getQuantidade(), "Quantidade inicial não pode ser negativa");
        return estoqueRepository.save(item);
    }

    public EstoqueItem atualizar(Long id, EstoqueItem itemAtualizado, String perfil) {
        PerfilOperacionalEstoque perfilOperacional = PerfilOperacionalEstoque.from(perfil);
        validarOperacaoGerencial(perfilOperacional);

        EstoqueItem itemExistente = buscarPorId(id);

        itemExistente.setNome(itemAtualizado.getNome());
        itemExistente.setQuantidade(itemAtualizado.getQuantidade());
        itemExistente.setUnidade(itemAtualizado.getUnidade());
        itemExistente.setValidade(itemAtualizado.getValidade());
        itemExistente.setCategoria(itemAtualizado.getCategoria());
        itemExistente.setImagem(itemAtualizado.getImagem());
        itemExistente.setCategoriaEstoque(itemAtualizado.getCategoriaEstoque());
        itemExistente.setEstoqueMinimo(itemAtualizado.getEstoqueMinimo());
        itemExistente.setLocalizacao(itemAtualizado.getLocalizacao());

        validarQuantidadeNaoNegativa(itemExistente.getQuantidade(), "Quantidade não pode ser negativa");

        return estoqueRepository.save(itemExistente);
    }

    public void excluir(Long id, String perfil) {
        PerfilOperacionalEstoque perfilOperacional = PerfilOperacionalEstoque.from(perfil);
        validarOperacaoGerencial(perfilOperacional);

        EstoqueItem item = buscarPorId(id);
        estoqueRepository.delete(item);
    }

    @Transactional
    public EstoqueItem movimentar(Long id, MovimentacaoDTO dto) {
        EstoqueItem item = buscarPorId(id);

        PerfilOperacionalEstoque perfil = PerfilOperacionalEstoque.from(dto.getPerfil());
        validarAcessoCategoria(perfil, item.getCategoriaEstoque());
        validarQuantidadePositiva(dto.getQuantidade(), "Quantidade da movimentação deve ser maior que zero");

        String tipo = normalizarTipoMovimentacao(dto.getTipo());

        switch (tipo) {
            case "entrada" -> item.setQuantidade(item.getQuantidade() + dto.getQuantidade());
            case "saida" -> retirarQuantidade(item, dto.getQuantidade(), "Estoque insuficiente para saída");
            case "perda" -> retirarQuantidade(item, dto.getQuantidade(), "Estoque insuficiente para perda");
            case "contagem" -> item.setQuantidade(dto.getQuantidade());
            default -> throw new IllegalArgumentException("Tipo de movimentação inválido");
        }

        return estoqueRepository.save(item);
    }

    @Transactional
    public EstoqueItem registrarProducao(ProducaoPorcaoDTO dto) {
        validarQuantidadePositiva(dto.getQuantidadeProduzida(), "Quantidade produzida deve ser maior que zero");

        PerfilOperacionalEstoque perfil = PerfilOperacionalEstoque.from(dto.getPerfil());
        EstoqueItem itemPorcao = buscarPorId(dto.getItemPorcaoId());

        if (itemPorcao.getCategoriaEstoque() != CategoriaEstoque.PORCAO_GERAL
                && itemPorcao.getCategoriaEstoque() != CategoriaEstoque.PORCAO) {
            throw new IllegalArgumentException("Produção só pode creditar itens de estoque de porção");
        }

        validarAcessoCategoria(perfil, itemPorcao.getCategoriaEstoque());

        for (ConsumoInternoDTO consumo : dto.getConsumosInternos()) {
            validarQuantidadePositiva(consumo.getQuantidade(), "Consumo interno deve ser maior que zero");

            EstoqueItem itemInterno = buscarPorId(consumo.getItemInternoId());
            if (itemInterno.getCategoriaEstoque() != CategoriaEstoque.INTERNO) {
                throw new IllegalArgumentException("Consumo de produção só aceita itens do estoque interno");
            }

            validarAcessoCategoria(perfil, itemInterno.getCategoriaEstoque());
            retirarQuantidade(itemInterno, consumo.getQuantidade(), "Estoque interno insuficiente para produção");
            estoqueRepository.save(itemInterno);
        }

        itemPorcao.setQuantidade(itemPorcao.getQuantidade() + dto.getQuantidadeProduzida());
        return estoqueRepository.save(itemPorcao);
    }

    @Transactional
    public EstoqueItem registrarConsumoMontagem(ConsumoMontagemDTO dto) {
        validarQuantidadePositiva(dto.getQuantidade(), "Quantidade consumida deve ser maior que zero");

        PerfilOperacionalEstoque perfil = PerfilOperacionalEstoque.from(dto.getPerfil());
        EstoqueItem itemPorcao = buscarPorId(dto.getItemPorcaoId());

        if (itemPorcao.getCategoriaEstoque() != CategoriaEstoque.PORCAO_GERAL
                && itemPorcao.getCategoriaEstoque() != CategoriaEstoque.PORCAO) {
            throw new IllegalArgumentException("Montagem só pode consumir itens de estoque de porção");
        }

        validarAcessoCategoria(perfil, itemPorcao.getCategoriaEstoque());
        retirarQuantidade(itemPorcao, dto.getQuantidade(), "Estoque de porção insuficiente para montagem");

        return estoqueRepository.save(itemPorcao);
    }

    private void validarOperacaoGerencial(PerfilOperacionalEstoque perfil) {
        if (!PERFIS_GERENCIAIS.contains(perfil)) {
            throw new IllegalArgumentException("Operação permitida apenas para dono, gerente ou chefe de cozinha");
        }
    }

    private void validarAcessoCategoria(PerfilOperacionalEstoque perfil, CategoriaEstoque categoria) {
        if (perfil == PerfilOperacionalEstoque.DONO
                || perfil == PerfilOperacionalEstoque.GERENTE
                || perfil == PerfilOperacionalEstoque.CHEFE_COZINHA) {
            return;
        }

        boolean autorizado = switch (perfil) {
            case PRODUCAO -> categoria == CategoriaEstoque.INTERNO || categoria == CategoriaEstoque.PORCAO_GERAL;
            case MONTAGEM -> categoria == CategoriaEstoque.PORCAO_GERAL || categoria == CategoriaEstoque.PORCAO;
            default -> false;
        };

        if (!autorizado) {
            throw new IllegalArgumentException("Perfil sem acesso a esta categoria de estoque");
        }
    }

    private void validarQuantidadePositiva(Double quantidade, String mensagem) {
        if (quantidade == null || quantidade <= 0) {
            throw new IllegalArgumentException(mensagem);
        }
    }

    private void validarQuantidadeNaoNegativa(Double quantidade, String mensagem) {
        if (quantidade == null || quantidade < 0) {
            throw new IllegalArgumentException(mensagem);
        }
    }

    private void retirarQuantidade(EstoqueItem item, Double quantidade, String mensagemErro) {
        double atual = item.getQuantidade() == null ? 0.0 : item.getQuantidade();
        if (atual < quantidade) {
            throw new IllegalArgumentException(mensagemErro);
        }
        item.setQuantidade(atual - quantidade);
    }

    private String normalizarTipoMovimentacao(String tipo) {
        if (tipo == null || tipo.isBlank()) {
            throw new IllegalArgumentException("Tipo de movimentação é obrigatório");
        }
        return tipo.trim().toLowerCase(Locale.ROOT);
    }

    private void seedItensIniciais() {
        criarItemInicial("Carne Bovina (Alcatra)", 50.0, "kg", LocalDate.now().plusDays(5),
                "Carnes", "🥩", CategoriaEstoque.GERAL, 10.0, "Câmara Fria A");
        criarItemInicial("Arroz (Saco 25kg)", 100.0, "kg", LocalDate.now().plusMonths(6),
                "Mercearia", "🌾", CategoriaEstoque.GERAL, 20.0, "Depósito Principal");
        criarItemInicial("Coca-Cola (Caixa)", 48.0, "lata", LocalDate.now().plusMonths(8),
                "Bebidas", "🥤", CategoriaEstoque.GERAL, 24.0, "Depósito Bebidas");

        criarItemInicial("Carne Marinada", 15.0, "kg", LocalDate.now().plusDays(2),
                "Semi-Preparados", "🍖", CategoriaEstoque.INTERNO, 5.0, "Geladeira Produção");
        criarItemInicial("Caldo Base", 8.0, "l", LocalDate.now().plusDays(3),
                "Caldos", "🍲", CategoriaEstoque.INTERNO, 2.0, "Área Quente");

        criarItemInicial("Legumes Pré-Cortados", 5.0, "kg", LocalDate.now().plusDays(2),
                "Vegetais", "🥕", CategoriaEstoque.PORCAO_GERAL, 2.0, "Geladeira Compartilhada");
        criarItemInicial("Arroz Cozido (Porção)", 30.0, "porção", LocalDate.now().plusDays(1),
                "Acompanhamentos", "🍚", CategoriaEstoque.PORCAO_GERAL, 10.0, "Pass Quente");

        criarItemInicial("Filé Grelhado Porcionado", 12.0, "porção", LocalDate.now().plusDays(1),
                "Proteínas Prontas", "🍽️", CategoriaEstoque.PORCAO, 5.0, "Pass Frio");
        criarItemInicial("Molho Especial (Porção)", 20.0, "porção", LocalDate.now().plusDays(2),
                "Molhos", "🥫", CategoriaEstoque.PORCAO, 8.0, "Pass Montagem");
    }

    private void criarItemInicial(String nome, Double qtd, String un, LocalDate val,
                                  String cat, String img, CategoriaEstoque catEstoque, Double estoqueMin, String localizacao) {
        EstoqueItem item = new EstoqueItem();
        item.setNome(nome);
        item.setQuantidade(qtd);
        item.setUnidade(un);
        item.setValidade(val);
        item.setCategoria(cat);
        item.setImagem(img);
        item.setCategoriaEstoque(catEstoque);
        item.setEstoqueMinimo(estoqueMin);
        item.setLocalizacao(localizacao);
        estoqueRepository.save(item);
    }
}
