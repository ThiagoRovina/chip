import React, { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { buildApiUrl } from '../utils/api';
import { withTenantHeader } from '../utils/tenant';
import FeedbackModal from '../components/FeedbackModal';
import './TelaEstoque.css';
import {
    ArrowDownCircle,
    ArrowUpCircle,
    Trash2,
    ClipboardCheck,
    Bell,
    LogOut,
    Plus,
    Minus,
    Check,
    AlertTriangle,
    X,
    ArrowLeft,
    PackagePlus,
    Edit,
    Save,
    ChefHat,
    UtensilsCrossed
} from 'lucide-react';

type CategoriaEstoque = 'geral' | 'interno' | 'porcao_geral' | 'porcao';

interface EstoqueItem {
    id: number;
    nome: string;
    quantidade: number;
    unidade: string;
    validade?: string;
    imagem?: string;
    status: 'ok' | 'baixo' | 'vencendo';
    categoria: string;
    categoriaEstoque: CategoriaEstoque;
}

interface FeedbackState {
    open: boolean;
    title: string;
    message: string;
    variant: 'success' | 'error' | 'info';
}

export default function TelaEstoque() {
    const navigate = useNavigate();
    const { user, logout } = useAuth();

    const [tenantName, setTenantName] = useState(' ');
    const [items, setItems] = useState<EstoqueItem[]>([]);
    const [loading, setLoading] = useState(true);
    const [selectedCategoria, setSelectedCategoria] = useState<CategoriaEstoque>('geral');

    const [feedback, setFeedback] = useState<FeedbackState>({
        open: false,
        title: '',
        message: '',
        variant: 'info'
    });
    const [confirmDeleteOpen, setConfirmDeleteOpen] = useState(false);

    const [modalOpen, setModalOpen] = useState<string | null>(null);
    const [selectedItemId, setSelectedItemId] = useState<number | null>(null);
    const [quantidade, setQuantidade] = useState<number>(1);
    const [motivoPerda, setMotivoPerda] = useState<string>('');
    const [consumoInternoId, setConsumoInternoId] = useState<number | null>(null);
    const [quantidadeConsumo, setQuantidadeConsumo] = useState<number>(1);

    const [itemForm, setItemForm] = useState({
        nome: '',
        unidade: 'un',
        categoria: 'Geral',
        categoriaEstoque: 'geral' as CategoriaEstoque,
        imagem: '📦',
        validade: '',
        quantidade: 0
    });

    const userRole = (user as any)?.cargo?.toUpperCase() || 'GERENTE';
    const perfilOperacional = normalizePerfil((user as any)?.cargo);
    const categorias = useMemo<Array<{ value: CategoriaEstoque; label: string }>>(() => ([
        { value: 'geral', label: 'Geral' },
        { value: 'interno', label: 'Interno' },
        { value: 'porcao_geral', label: 'Porção Geral' },
        { value: 'porcao', label: 'Porção Montagem' }
    ]), []);

    useEffect(() => {
        fetchTenantInfo();
        fetchEstoque();
    }, []);

    const fetchTenantInfo = async () => {
        try {
            const response = await fetch(buildApiUrl('/api/tenant/current'), {
                headers: withTenantHeader()
            });
            if (response.ok) {
                const data = await response.json();
                setTenantName(data.name || ' ');
            }
        } catch (error) {
            console.error('Erro ao buscar informações do tenant:', error);
        }
    };

    const fetchEstoque = async () => {
        try {
            const response = await fetch(buildApiUrl('/api/estoque'), {
                headers: withTenantHeader()
            });
            if (response.ok) {
                const data = await response.json();
                setItems(data);
            }
        } catch (error) {
            console.error('Erro ao buscar estoque:', error);
        } finally {
            setLoading(false);
        }
    };

    const openFeedback = (title: string, message: string, variant: FeedbackState['variant']) => {
        setFeedback({ open: true, title, message, variant });
    };

    const closeModal = () => setModalOpen(null);

    const resetFlowState = () => {
        setSelectedItemId(null);
        setQuantidade(1);
        setMotivoPerda('');
        setConsumoInternoId(null);
        setQuantidadeConsumo(1);
    };

    const resetItemForm = () => {
        setItemForm({
            nome: '',
            unidade: 'un',
            categoria: 'Geral',
            categoriaEstoque: 'geral',
            imagem: '📦',
            validade: '',
            quantidade: 0
        });
    };

    const handleAction = (action: string) => {
        setModalOpen(action);
        resetFlowState();
    };

    const handleNewItem = () => {
        setModalOpen('novo');
        resetFlowState();
        resetItemForm();
    };

    const handleEditItem = (item: EstoqueItem) => {
        setModalOpen('editar');
        resetFlowState();
        setSelectedItemId(item.id);
        setItemForm({
            nome: item.nome,
            unidade: item.unidade,
            categoria: item.categoria,
            categoriaEstoque: item.categoriaEstoque || 'geral',
            imagem: item.imagem || '📦',
            validade: item.validade || '',
            quantidade: item.quantidade
        });
    };

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    const getInitials = (name: string | undefined) => {
        if (!name) return 'U';
        return name.split(' ').map(n => n[0]).join('').substring(0, 2).toUpperCase();
    };

    const allowedCategories = getAllowedCategories(perfilOperacional);
    const visibleItems = items.filter(i => allowedCategories.includes(i.categoriaEstoque ?? 'geral'));
    const itemsDaCategoria = visibleItems.filter(i => (i.categoriaEstoque ?? 'geral') === selectedCategoria);
    const criticalItems = itemsDaCategoria.filter(i => i.status === 'baixo' || i.status === 'vencendo');
    const normalItems = itemsDaCategoria.filter(i => i.status === 'ok');

    const itensProducao = visibleItems.filter(i => i.categoriaEstoque === 'porcao_geral' || i.categoriaEstoque === 'porcao');
    const itensMontagem = itensProducao;
    const insumosInternos = visibleItems.filter(i => i.categoriaEstoque === 'interno');

    const getModalTitle = () => {
        switch (modalOpen) {
            case 'entrada': return 'O que chegou?';
            case 'saida': return 'O que você usou?';
            case 'perda': return 'O que estragou?';
            case 'contagem': return 'O que você vai contar?';
            case 'producao': return 'Registrar Produção';
            case 'montagem': return 'Baixa na Montagem';
            case 'novo': return 'Novo Produto';
            case 'editar': return 'Editar Produto';
            default: return '';
        }
    };

    const getModalColor = () => {
        switch (modalOpen) {
            case 'entrada': return 'var(--stock-green)';
            case 'saida': return 'var(--stock-blue)';
            case 'perda': return 'var(--stock-red)';
            case 'contagem': return 'var(--stock-purple)';
            case 'producao': return '#7A4B2A';
            case 'montagem': return '#5A8F2C';
            case 'novo':
            case 'editar': return '#FF9800';
            default: return '#333';
        }
    };

    const adjustQuantity = (delta: number) => {
        setQuantidade(prev => Math.max(0.1, parseFloat((prev + delta).toFixed(2))));
    };

    const responseErrorText = async (response: Response, fallback: string) => {
        try {
            const text = await response.text();
            return text || fallback;
        } catch {
            return fallback;
        }
    };

    const postMovimentacao = async () => {
        if (selectedItemId === null) {
            openFeedback('Selecione um item', 'Toque em um item para continuar.', 'info');
            return;
        }

        const item = items.find(i => i.id === selectedItemId);
        const acaoTexto = modalOpen === 'entrada' ? 'Entrada' : modalOpen === 'saida' ? 'Saída' : modalOpen === 'perda' ? 'Perda' : 'Contagem';

        const response = await fetch(buildApiUrl(`/api/estoque/${selectedItemId}/movimentar`), {
            method: 'POST',
            headers: withTenantHeader({ 'Content-Type': 'application/json' }),
            body: JSON.stringify({
                tipo: modalOpen,
                quantidade,
                motivo: motivoPerda,
                perfil: perfilOperacional
            })
        });

        if (!response.ok) {
            const erro = await responseErrorText(response, 'Não foi possível registrar a movimentação.');
            openFeedback('Não foi possível registrar', erro, 'error');
            return;
        }

        openFeedback('Movimentação registrada', `${acaoTexto} de ${quantidade} ${item?.unidade} de ${item?.nome}.`, 'success');
        await fetchEstoque();
        closeModal();
    };

    const postProducao = async () => {
        if (selectedItemId === null) {
            openFeedback('Selecione a porção', 'Escolha o item de porção produzido.', 'info');
            return;
        }

        const response = await fetch(buildApiUrl('/api/estoque/producoes'), {
            method: 'POST',
            headers: withTenantHeader({ 'Content-Type': 'application/json' }),
            body: JSON.stringify({
                itemPorcaoId: selectedItemId,
                quantidadeProduzida: quantidade,
                perfil: perfilOperacional,
                consumosInternos: consumoInternoId
                    ? [{ itemInternoId: consumoInternoId, quantidade: quantidadeConsumo }]
                    : []
            })
        });

        if (!response.ok) {
            const erro = await responseErrorText(response, 'Falha ao registrar produção.');
            openFeedback('Não foi possível registrar', erro, 'error');
            return;
        }

        openFeedback('Produção registrada', 'Porções registradas com sucesso.', 'success');
        await fetchEstoque();
        closeModal();
    };

    const postMontagem = async () => {
        if (selectedItemId === null) {
            openFeedback('Selecione a porção', 'Escolha o item para baixar na montagem.', 'info');
            return;
        }

        const response = await fetch(buildApiUrl('/api/estoque/montagem/consumos'), {
            method: 'POST',
            headers: withTenantHeader({ 'Content-Type': 'application/json' }),
            body: JSON.stringify({
                itemPorcaoId: selectedItemId,
                quantidade,
                perfil: perfilOperacional
            })
        });

        if (!response.ok) {
            const erro = await responseErrorText(response, 'Falha ao registrar consumo na montagem.');
            openFeedback('Não foi possível registrar', erro, 'error');
            return;
        }

        openFeedback('Baixa registrada', 'Consumo na montagem registrado com sucesso.', 'success');
        await fetchEstoque();
        closeModal();
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        try {
            if (modalOpen === 'novo' || modalOpen === 'editar') {
                const url = modalOpen === 'novo'
                    ? buildApiUrl('/api/estoque')
                    : buildApiUrl(`/api/estoque/${selectedItemId}`);

                const method = modalOpen === 'novo' ? 'POST' : 'PUT';
                const response = await fetch(url, {
                    method,
                    headers: withTenantHeader({
                        'Content-Type': 'application/json',
                        'X-Perfil-Operacional': perfilOperacional,
                    }),
                    body: JSON.stringify(itemForm)
                });

                if (!response.ok) {
                    const erro = await responseErrorText(response, 'Não foi possível salvar o item.');
                    openFeedback('Não foi possível salvar', erro, 'error');
                    return;
                }

                openFeedback('Salvo com sucesso', modalOpen === 'novo' ? 'Item criado com sucesso.' : 'Item atualizado com sucesso.', 'success');
                await fetchEstoque();
                closeModal();
                return;
            }

            if (modalOpen === 'producao') {
                await postProducao();
                return;
            }

            if (modalOpen === 'montagem') {
                await postMontagem();
                return;
            }

            await postMovimentacao();
        } catch (error) {
            console.error('Erro:', error);
            openFeedback('Sem conexão', 'Verifique a internet e tente novamente.', 'error');
        }
    };

    const confirmDelete = () => {
        if (!selectedItemId) return;
        setConfirmDeleteOpen(true);
    };

    const handleDelete = async () => {
        if (!selectedItemId) return;

        try {
            const response = await fetch(buildApiUrl(`/api/estoque/${selectedItemId}`), {
                method: 'DELETE',
                headers: withTenantHeader({
                    'X-Perfil-Operacional': perfilOperacional,
                })
            });

            if (!response.ok) {
                const erro = await responseErrorText(response, 'Não foi possível excluir o item.');
                openFeedback('Não foi possível excluir', erro, 'error');
                return;
            }

            openFeedback('Item excluído', 'Item removido com sucesso.', 'success');
            await fetchEstoque();
            setConfirmDeleteOpen(false);
            closeModal();
        } catch (error) {
            console.error('Erro:', error);
            openFeedback('Sem conexão', 'Verifique a internet e tente novamente.', 'error');
        }
    };

    const itensSelecaoModal =
        modalOpen === 'producao' ? itensProducao :
            modalOpen === 'montagem' ? itensMontagem :
                itemsDaCategoria;

    return (
        <div className="stock-container">
            <nav className="top-nav" aria-label="Navegação Principal">
                <div className="nav-left">
                    <button className="back-btn" onClick={() => navigate('/home')} aria-label="Voltar">
                        <ArrowLeft size={24} />
                    </button>
                    <span className="nav-title">{tenantName}</span>
                </div>
                <div className="nav-right">
                    <button className="icon-btn" title="Notificações">
                        <Bell size={24} />
                        <span className="notification-badge"></span>
                    </button>
                    <div className="user-profile">
                        <div className="user-avatar">{getInitials((user as any)?.nmUsuario)}</div>
                        <div className="user-info">
                            <span className="user-name">{(user as any)?.nmUsuario || 'Usuário'}</span>
                            <span className="user-role">{userRole}</span>
                        </div>
                        <button className="icon-btn" onClick={handleLogout} title="Sair">
                            <LogOut size={24} color="#D32F2F" />
                        </button>
                    </div>
                </div>
            </nav>

            <div className="stock-content">
                <header className="stock-header">
                    <h1>Estoque Fácil</h1>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <p>Gerencie seu estoque por setor e função</p>
                        <button className="btn-new-item" onClick={handleNewItem}>
                            <PackagePlus size={20} style={{ marginRight: '8px' }} />
                            Novo Produto
                        </button>
                    </div>
                </header>

                <section className="stock-actions">
                    <button className="action-btn btn-entry" onClick={() => handleAction('entrada')}>
                        <ArrowDownCircle size={48} />
                        CHEGOU<br />MERCADORIA
                    </button>
                    <button className="action-btn btn-use" onClick={() => handleAction('saida')}>
                        <ArrowUpCircle size={48} />
                        USOU NA<br />COZINHA
                    </button>
                    <button className="action-btn btn-waste" onClick={() => handleAction('perda')}>
                        <Trash2 size={48} />
                        JOGOU<br />FORA
                    </button>
                    <button className="action-btn btn-count" onClick={() => handleAction('contagem')}>
                        <ClipboardCheck size={48} />
                        CONTAGEM<br />(BALANÇO)
                    </button>
                    <button className="action-btn btn-entry" onClick={() => handleAction('producao')}>
                        <ChefHat size={48} />
                        PRODUZIR<br />PORÇÕES
                    </button>
                    <button className="action-btn btn-use" onClick={() => handleAction('montagem')}>
                        <UtensilsCrossed size={48} />
                        BAIXA NA<br />MONTAGEM
                    </button>
                </section>

                <section style={{ display: 'flex', gap: '8px', flexWrap: 'wrap', marginBottom: '16px' }}>
                    {categorias.map(cat => (
                        <button
                            key={cat.value}
                            onClick={() => setSelectedCategoria(cat.value)}
                            style={{
                                border: selectedCategoria === cat.value ? '2px solid #2E7D32' : '1px solid #ddd',
                                background: selectedCategoria === cat.value ? '#E8F5E9' : '#fff',
                                borderRadius: '999px',
                                padding: '8px 14px',
                                cursor: 'pointer',
                                fontWeight: 700
                            }}
                        >
                            {cat.label}
                        </button>
                    ))}
                </section>

                <section className="stock-alerts">
                    <div className="alert-card critical">
                        <div className="alert-icon"><AlertTriangle size={40} /></div>
                        <div className="alert-info">
                            <h3>ACABANDO</h3>
                            <span>{criticalItems.filter(i => i.status === 'baixo').length} itens</span>
                        </div>
                    </div>
                    <div className="alert-card warning">
                        <div className="alert-icon">📅</div>
                        <div className="alert-info">
                            <h3>VENCENDO</h3>
                            <span>{criticalItems.filter(i => i.status === 'vencendo').length} itens</span>
                        </div>
                    </div>
                </section>

                <section className="stock-list-section">
                    {loading ? (
                        <p style={{ textAlign: 'center' }}>Carregando estoque...</p>
                    ) : (
                        <>
                            {criticalItems.length > 0 && (
                                <>
                                    <h2 style={{ color: '#C62828', borderColor: '#C62828' }}>⚠️ Atenção (Acabando)</h2>
                                    <div className="stock-grid" style={{ marginBottom: '40px' }}>
                                        {criticalItems.map(item => (
                                            <ItemCard key={item.id} item={item} onEdit={() => handleEditItem(item)} />
                                        ))}
                                    </div>
                                </>
                            )}

                            <h2>📦 Estoque Selecionado</h2>
                            <div className="stock-grid">
                                {normalItems.map(item => (
                                    <ItemCard key={item.id} item={item} onEdit={() => handleEditItem(item)} />
                                ))}
                            </div>

                            {itemsDaCategoria.length === 0 && (
                                <p style={{ textAlign: 'center', color: '#666', marginTop: '18px' }}>
                                    Nenhum item disponível nesta categoria para o seu perfil.
                                </p>
                            )}
                        </>
                    )}
                </section>
            </div>

            {modalOpen && (
                <div className="modal-overlay" onClick={(e) => { if (e.target === e.currentTarget) closeModal(); }}>
                    <div className="modal-content">
                        <div className="modal-header">
                            <h2 style={{ color: getModalColor() }}>{getModalTitle()}</h2>
                            <button className="close-btn-large" onClick={closeModal}>
                                <X size={32} />
                            </button>
                        </div>

                        <form onSubmit={handleSubmit}>
                            {(modalOpen === 'novo' || modalOpen === 'editar') ? (
                                <div className="form-crud">
                                    <div className="form-group">
                                        <label>Nome do Produto</label>
                                        <input
                                            type="text"
                                            value={itemForm.nome}
                                            onChange={(e) => setItemForm({ ...itemForm, nome: e.target.value })}
                                            placeholder="Ex: Arroz"
                                            required
                                        />
                                    </div>
                                    <div className="form-row">
                                        <div className="form-group">
                                            <label>Quantidade Inicial</label>
                                            <input
                                                type="number"
                                                value={itemForm.quantidade}
                                                onChange={(e) => setItemForm({ ...itemForm, quantidade: parseFloat(e.target.value || '0') })}
                                                min="0"
                                                step="1"
                                            />
                                        </div>
                                        <div className="form-group">
                                            <label>Unidade</label>
                                            <select
                                                value={itemForm.unidade}
                                                onChange={(e) => setItemForm({ ...itemForm, unidade: e.target.value })}
                                            >
                                                <option value="un">Unidade (un)</option>
                                                <option value="kg">Quilo (kg)</option>
                                                <option value="l">Litro (l)</option>
                                                <option value="cx">Caixa (cx)</option>
                                                <option value="porcao">Porção</option>
                                            </select>
                                        </div>
                                    </div>

                                    <div className="form-row">
                                        <div className="form-group">
                                            <label>Tipo de Estoque</label>
                                            <select
                                                value={itemForm.categoriaEstoque}
                                                onChange={(e) => setItemForm({ ...itemForm, categoriaEstoque: e.target.value as CategoriaEstoque })}
                                            >
                                                {categorias.map(cat => (
                                                    <option key={cat.value} value={cat.value}>{cat.label}</option>
                                                ))}
                                            </select>
                                        </div>
                                        <div className="form-group">
                                            <label>Categoria</label>
                                            <input
                                                type="text"
                                                value={itemForm.categoria}
                                                onChange={(e) => setItemForm({ ...itemForm, categoria: e.target.value })}
                                                placeholder="Ex: Proteínas"
                                            />
                                        </div>
                                    </div>

                                    <div className="form-group">
                                        <label>Imagem do Produto</label>
                                        <input
                                            type="file"
                                            accept="image/*"
                                            onChange={(e) => {
                                                const file = e.target.files?.[0];
                                                if (!file) return;
                                                const reader = new FileReader();
                                                reader.onloadend = () => setItemForm({ ...itemForm, imagem: reader.result as string });
                                                reader.readAsDataURL(file);
                                            }}
                                            style={{ padding: '8px' }}
                                        />
                                        {itemForm.imagem && itemForm.imagem.startsWith('data:') && (
                                            <div style={{ marginTop: '10px', textAlign: 'center' }}>
                                                <img
                                                    src={itemForm.imagem}
                                                    alt="Preview"
                                                    style={{ maxWidth: '100px', maxHeight: '100px', borderRadius: '8px', border: '2px solid #eee' }}
                                                />
                                            </div>
                                        )}
                                    </div>

                                    <div className="form-group">
                                        <label>Validade (Opcional)</label>
                                        <input
                                            type="date"
                                            value={itemForm.validade}
                                            onChange={(e) => setItemForm({ ...itemForm, validade: e.target.value })}
                                        />
                                    </div>

                                    <div className="modal-big-actions">
                                        {modalOpen === 'editar' && (
                                            <button type="button" className="btn-big-delete" onClick={confirmDelete}>
                                                <Trash2 size={24} /> Excluir
                                            </button>
                                        )}
                                        <button type="submit" className="btn-big-confirm">
                                            <Save size={24} style={{ marginRight: '10px' }} />
                                            SALVAR
                                        </button>
                                    </div>
                                </div>
                            ) : (
                                <>
                                    <div className="form-group">
                                        <label style={{ fontSize: '1.2rem', textAlign: 'center', marginBottom: '15px' }}>Toque no produto:</label>
                                        <div className="item-select-grid">
                                            {itensSelecaoModal.map(item => (
                                                <div
                                                    key={item.id}
                                                    className={`item-select-card ${selectedItemId === item.id ? 'selected' : ''}`}
                                                    onClick={() => setSelectedItemId(item.id)}
                                                >
                                                    {item.imagem?.startsWith('data:') ? (
                                                        <img
                                                            src={item.imagem}
                                                            alt={item.nome}
                                                            style={{ width: '60px', height: '60px', objectFit: 'cover', borderRadius: '8px', marginBottom: '5px' }}
                                                        />
                                                    ) : (
                                                        <span>{item.imagem || '📦'}</span>
                                                    )}
                                                    <div style={{ fontWeight: 'bold', fontSize: '0.9rem' }}>{item.nome}</div>
                                                </div>
                                            ))}
                                        </div>
                                    </div>

                                    {selectedItemId !== null && (
                                        <>
                                            {modalOpen === 'producao' && (
                                                <div className="form-group">
                                                    <label style={{ fontSize: '1.1rem' }}>Insumo interno consumido (opcional)</label>
                                                    <select
                                                        value={consumoInternoId ?? ''}
                                                        onChange={(e) => setConsumoInternoId(e.target.value ? Number(e.target.value) : null)}
                                                    >
                                                        <option value="">Nenhum</option>
                                                        {insumosInternos.map(item => (
                                                            <option key={item.id} value={item.id}>
                                                                {item.nome} ({item.quantidade} {item.unidade})
                                                            </option>
                                                        ))}
                                                    </select>
                                                    {consumoInternoId && (
                                                        <div style={{ marginTop: '10px' }}>
                                                            <label style={{ fontSize: '1rem' }}>Quantidade do insumo</label>
                                                            <input
                                                                type="number"
                                                                value={quantidadeConsumo}
                                                                step="0.01"
                                                                onChange={(e) => setQuantidadeConsumo(parseFloat(e.target.value || '0'))}
                                                            />
                                                        </div>
                                                    )}
                                                </div>
                                            )}

                                            <div className="form-group">
                                                <label style={{ fontSize: '1.2rem', textAlign: 'center' }}>
                                                    {modalOpen === 'producao' ? 'Quanto foi produzido?' : 'Quanto?'}
                                                </label>
                                                <div className="qty-control">
                                                    <button type="button" className="btn-qty btn-minus" onClick={() => adjustQuantity(-1)}>
                                                        <Minus size={32} />
                                                    </button>

                                                    <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
                                                        <input
                                                            type="number"
                                                            className="qty-display"
                                                            value={quantidade}
                                                            onChange={(e) => setQuantidade(parseFloat(e.target.value || '0'))}
                                                        />
                                                        <span style={{ fontSize: '1.2rem', color: '#666' }}>
                                                            {items.find(i => i.id === selectedItemId)?.unidade}
                                                        </span>
                                                    </div>

                                                    <button type="button" className="btn-qty btn-plus" onClick={() => adjustQuantity(1)}>
                                                        <Plus size={32} />
                                                    </button>
                                                </div>
                                            </div>

                                            {modalOpen === 'perda' && (
                                                <div className="form-group">
                                                    <label style={{ fontSize: '1.2rem' }}>Por que jogou fora?</label>
                                                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px' }}>
                                                        {['Venceu', 'Estragou', 'Caiu', 'Sobra'].map(motivo => (
                                                            <button
                                                                key={motivo}
                                                                type="button"
                                                                onClick={() => setMotivoPerda(motivo)}
                                                                style={{
                                                                    padding: '15px',
                                                                    borderRadius: '10px',
                                                                    border: motivoPerda === motivo ? '3px solid var(--stock-red)' : '2px solid #eee',
                                                                    background: motivoPerda === motivo ? '#FFEBEE' : 'white',
                                                                    fontSize: '1rem',
                                                                    fontWeight: 'bold',
                                                                    cursor: 'pointer'
                                                                }}
                                                            >
                                                                {motivo}
                                                            </button>
                                                        ))}
                                                    </div>
                                                </div>
                                            )}

                                            <div className="modal-big-actions">
                                                <button type="button" className="btn-big-cancel" onClick={closeModal}>Cancelar</button>
                                                <button type="submit" className="btn-big-confirm">
                                                    <Check size={24} style={{ marginRight: '10px', verticalAlign: 'middle' }} />
                                                    CONFIRMAR
                                                </button>
                                            </div>
                                        </>
                                    )}
                                </>
                            )}
                        </form>
                    </div>
                </div>
            )}

            {confirmDeleteOpen && (
                <div className="modal-overlay" onClick={(e) => { if (e.target === e.currentTarget) setConfirmDeleteOpen(false); }}>
                    <div className="modal-content" style={{ maxWidth: '420px' }}>
                        <div className="modal-header">
                            <h2 style={{ color: '#C62828', fontSize: '1.6rem' }}>Confirmar exclusão</h2>
                        </div>
                        <p style={{ textAlign: 'center', marginBottom: '24px', color: '#333' }}>
                            Tem certeza que deseja excluir este item? Essa ação não pode ser desfeita.
                        </p>
                        <div className="modal-big-actions">
                            <button
                                type="button"
                                className="btn-big-cancel"
                                onClick={() => setConfirmDeleteOpen(false)}
                            >
                                Cancelar
                            </button>
                            <button
                                type="button"
                                className="btn-big-delete"
                                onClick={handleDelete}
                            >
                                Confirmar exclusão
                            </button>
                        </div>
                    </div>
                </div>
            )}

            <FeedbackModal
                open={feedback.open}
                title={feedback.title}
                message={feedback.message}
                variant={feedback.variant}
                onClose={() => setFeedback({ open: false, title: '', message: '', variant: 'info' })}
            />
        </div>
    );
}

function ItemCard({ item, onEdit }: { item: EstoqueItem, onEdit: () => void }) {
    return (
        <div className="item-card" onClick={onEdit} style={{ cursor: 'pointer', position: 'relative' }}>
            <div className={`status-indicator ${item.status === 'baixo' ? 'status-red' : item.status === 'vencendo' ? 'status-yellow' : 'status-green'}`}></div>

            <div className="edit-icon-overlay">
                <Edit size={16} color="#666" />
            </div>

            <div className="item-image">
                {item.imagem?.startsWith('data:') ? (
                    <img
                        src={item.imagem}
                        alt={item.nome}
                        style={{ width: '100%', height: '100%', objectFit: 'cover', borderRadius: '8px' }}
                    />
                ) : (
                    item.imagem || '📦'
                )}
            </div>
            <div className="item-info">
                <div className="item-name">{item.nome}</div>
                <div>
                    <span className="item-qty">{item.quantidade}</span>
                    <span className="item-unit"> {item.unidade}</span>
                </div>
            </div>
        </div>
    );
}

function normalizePerfil(cargo?: string): string {
    if (!cargo) return 'gerente';

    const normalized = cargo
        .normalize('NFD')
        .replace(/[\u0300-\u036f]/g, '')
        .toLowerCase()
        .trim();

    if (normalized.includes('dono')) return 'dono';
    if (normalized.includes('gerente')) return 'gerente';
    if (normalized.includes('chefe')) return 'chefe_cozinha';
    if (normalized.includes('produc')) return 'producao';
    if (normalized.includes('montagem')) return 'montagem';

    return 'gerente';
}

function getAllowedCategories(perfil: string): CategoriaEstoque[] {
    if (perfil === 'producao') return ['interno', 'porcao_geral'];
    if (perfil === 'montagem') return ['porcao_geral', 'porcao'];
    return ['geral', 'interno', 'porcao_geral', 'porcao'];
}
