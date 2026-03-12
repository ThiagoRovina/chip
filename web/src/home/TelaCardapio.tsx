import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowLeft, Bell, ChefHat, LogOut, Package, Pencil, Plus, Save, Trash2, Video } from 'lucide-react';
import { useAuth } from '../auth/AuthContext';
import { buildApiUrl } from '../utils/api';
import { withTenantHeader } from '../utils/tenant';
import FeedbackModal from '../components/FeedbackModal';
import './TelaCardapio.css';

interface ProdutoIngrediente {
    valor: string;
}

interface ProdutoPasso {
    descricao: string;
    tempoSegundos: number;
    videoUrl: string;
}

interface Produto {
    id?: number;
    nome: string;
    descricao: string;
    preco: number;
    categoria: string;
    imagem: string;
    disponivel: boolean;
    ingredientes: ProdutoIngrediente[];
    passos: ProdutoPasso[];
}

const createEmptyProduto = (): Produto => ({
    nome: '',
    descricao: '',
    preco: 0,
    categoria: 'Lanches',
    imagem: '🍽️',
    disponivel: true,
    ingredientes: [{ valor: '' }],
    passos: [{ descricao: '', tempoSegundos: 0, videoUrl: '' }]
});

export default function TelaCardapio() {
    const navigate = useNavigate();
    const { user, logout } = useAuth();
    const [tenantName, setTenantName] = useState(' ');
    const [produtos, setProdutos] = useState<Produto[]>([]);
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [editingId, setEditingId] = useState<number | null>(null);
    const [form, setForm] = useState<Produto>(createEmptyProduto());
    const [feedback, setFeedback] = useState<{ open: boolean; title: string; message: string; variant: 'success' | 'error' | 'info' }>({
        open: false,
        title: '',
        message: '',
        variant: 'info'
    });

    const userRole = (user as any)?.cargo?.toUpperCase() || 'GERENTE';

    useEffect(() => {
        fetchTenantInfo();
        fetchProdutos();
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
            console.error('Erro ao buscar tenant:', error);
        }
    };

    const fetchProdutos = async () => {
        try {
            const response = await fetch(buildApiUrl('/api/produtos'), {
                headers: withTenantHeader()
            });
            if (!response.ok) {
                throw new Error('Falha ao buscar produtos');
            }
            const data = await response.json();
            setProdutos(data);
        } catch (error) {
            console.error('Erro ao buscar produtos:', error);
            setFeedback({
                open: true,
                title: 'Sem acesso ao cardápio',
                message: 'Não foi possível carregar os produtos.',
                variant: 'error'
            });
        } finally {
            setLoading(false);
        }
    };

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    const getInitials = (name: string | undefined) => {
        if (!name) return 'U';
        return name.split(' ').map(n => n[0]).join('').substring(0, 2).toUpperCase();
    };

    const updateField = <K extends keyof Produto,>(field: K, value: Produto[K]) => {
        setForm(prev => ({ ...prev, [field]: value }));
    };

    const updateIngrediente = (index: number, value: string) => {
        setForm(prev => ({
            ...prev,
            ingredientes: prev.ingredientes.map((item, idx) => idx === index ? { valor: value } : item)
        }));
    };

    const addIngrediente = () => {
        setForm(prev => ({ ...prev, ingredientes: [...prev.ingredientes, { valor: '' }] }));
    };

    const removeIngrediente = (index: number) => {
        setForm(prev => ({
            ...prev,
            ingredientes: prev.ingredientes.filter((_, idx) => idx !== index)
        }));
    };

    const updatePasso = (index: number, field: keyof ProdutoPasso, value: string | number) => {
        setForm(prev => ({
            ...prev,
            passos: prev.passos.map((passo, idx) => idx === index ? { ...passo, [field]: value } : passo)
        }));
    };

    const addPasso = () => {
        setForm(prev => ({
            ...prev,
            passos: [...prev.passos, { descricao: '', tempoSegundos: 0, videoUrl: '' }]
        }));
    };

    const removePasso = (index: number) => {
        setForm(prev => ({
            ...prev,
            passos: prev.passos.filter((_, idx) => idx !== index)
        }));
    };

    const startCreate = () => {
        setEditingId(null);
        setForm(createEmptyProduto());
    };

    const startEdit = (produto: Produto) => {
        setEditingId(produto.id ?? null);
        setForm({
            ...produto,
            ingredientes: produto.ingredientes?.length ? produto.ingredientes : [{ valor: '' }],
            passos: produto.passos?.length ? produto.passos : [{ descricao: '', tempoSegundos: 0, videoUrl: '' }]
        });
    };

    const normalizeProduto = (produto: Produto) => ({
        ...produto,
        ingredientes: produto.ingredientes.filter(item => item.valor.trim()),
        passos: produto.passos.filter(passo => passo.descricao.trim()).map(passo => ({
            ...passo,
            tempoSegundos: Number.isFinite(passo.tempoSegundos) ? passo.tempoSegundos : 0,
            videoUrl: passo.videoUrl.trim()
        }))
    });

    const saveProduto = async (e: React.FormEvent) => {
        e.preventDefault();
        setSaving(true);

        const payload = normalizeProduto(form);

        try {
            const response = await fetch(
                editingId ? buildApiUrl(`/api/produtos/${editingId}`) : buildApiUrl('/api/produtos'),
                {
                    method: editingId ? 'PUT' : 'POST',
                    headers: withTenantHeader({ 'Content-Type': 'application/json' }),
                    body: JSON.stringify(payload)
                }
            );

            if (!response.ok) {
                throw new Error('Falha ao salvar produto');
            }

            setFeedback({
                open: true,
                title: 'Cardápio atualizado',
                message: editingId ? 'Produto atualizado com sucesso.' : 'Produto criado com sucesso.',
                variant: 'success'
            });
            setForm(createEmptyProduto());
            setEditingId(null);
            await fetchProdutos();
        } catch (error) {
            console.error('Erro ao salvar produto:', error);
            setFeedback({
                open: true,
                title: 'Não foi possível salvar',
                message: 'Revise os dados e tente novamente.',
                variant: 'error'
            });
        } finally {
            setSaving(false);
        }
    };

    const deleteProduto = async (produtoId?: number) => {
        if (!produtoId) return;

        try {
            const response = await fetch(buildApiUrl(`/api/produtos/${produtoId}`), {
                method: 'DELETE',
                headers: withTenantHeader()
            });

            if (!response.ok) {
                throw new Error('Falha ao excluir produto');
            }

            if (editingId === produtoId) {
                setEditingId(null);
                setForm(createEmptyProduto());
            }

            setFeedback({
                open: true,
                title: 'Produto removido',
                message: 'O item saiu do cardápio.',
                variant: 'success'
            });
            await fetchProdutos();
        } catch (error) {
            console.error('Erro ao excluir produto:', error);
            setFeedback({
                open: true,
                title: 'Não foi possível excluir',
                message: 'Tente novamente em alguns segundos.',
                variant: 'error'
            });
        }
    };

    return (
        <div className="cardapio-page">
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
                        <div className="user-avatar">{getInitials(user?.nmUsuario)}</div>
                        <div className="user-info">
                            <span className="user-name">{user?.nmUsuario || 'Usuário'}</span>
                            <span className="user-role">{userRole}</span>
                        </div>
                        <button className="icon-btn" onClick={handleLogout} title="Sair">
                            <LogOut size={24} color="#D32F2F" />
                        </button>
                    </div>
                </div>
            </nav>

            <main className="cardapio-content">
                <section className="cardapio-list-panel">
                    <div className="section-header">
                        <div>
                            <p className="eyebrow">Cardápio</p>
                            <h1>Produtos e receitas</h1>
                        </div>
                        <button className="primary-action" onClick={startCreate}>
                            <Plus size={18} />
                            Novo produto
                        </button>
                    </div>

                    {loading ? (
                        <div className="empty-state">Carregando produtos...</div>
                    ) : produtos.length === 0 ? (
                        <div className="empty-state">Nenhum produto cadastrado ainda.</div>
                    ) : (
                        <div className="produto-list">
                            {produtos.map(produto => (
                                <article key={produto.id} className={`produto-card ${editingId === produto.id ? 'selected' : ''}`}>
                                    <div className="produto-card-top">
                                        <span className="produto-emoji">{produto.imagem || '🍽️'}</span>
                                        <div>
                                            <h3>{produto.nome}</h3>
                                            <p>{produto.categoria}</p>
                                        </div>
                                    </div>
                                    <div className="produto-meta">
                                        <span>R$ {Number(produto.preco || 0).toFixed(2)}</span>
                                        <span>{produto.disponivel ? 'Disponível' : 'Indisponível'}</span>
                                        <span>{produto.passos?.length || 0} passos</span>
                                    </div>
                                    <div className="produto-actions">
                                        <button type="button" onClick={() => startEdit(produto)}>
                                            <Pencil size={16} />
                                            Editar
                                        </button>
                                        <button type="button" onClick={() => deleteProduto(produto.id)}>
                                            <Trash2 size={16} />
                                            Excluir
                                        </button>
                                    </div>
                                </article>
                            ))}
                        </div>
                    )}
                </section>

                <section className="cardapio-form-panel">
                    <div className="section-header">
                        <div>
                            <p className="eyebrow">{editingId ? 'Edição' : 'Cadastro'}</p>
                            <h2>{editingId ? 'Editar produto' : 'Novo produto'}</h2>
                        </div>
                    </div>

                    <form className="produto-form" onSubmit={saveProduto}>
                        <div className="form-grid">
                            <label>
                                Nome
                                <input value={form.nome} onChange={(e) => updateField('nome', e.target.value)} required />
                            </label>
                            <label>
                                Categoria
                                <input value={form.categoria} onChange={(e) => updateField('categoria', e.target.value)} required />
                            </label>
                            <label>
                                Preço
                                <input
                                    type="number"
                                    min="0"
                                    step="0.01"
                                    value={form.preco}
                                    onChange={(e) => updateField('preco', Number(e.target.value))}
                                    required
                                />
                            </label>
                            <label>
                                Imagem/emoji
                                <input value={form.imagem} onChange={(e) => updateField('imagem', e.target.value)} placeholder="🍔 ou URL" />
                            </label>
                        </div>

                        <label>
                            Descrição
                            <textarea value={form.descricao} onChange={(e) => updateField('descricao', e.target.value)} rows={3} />
                        </label>

                        <label className="toggle-field">
                            <input
                                type="checkbox"
                                checked={form.disponivel}
                                onChange={(e) => updateField('disponivel', e.target.checked)}
                            />
                            Produto disponível para pedido
                        </label>

                        <div className="form-section">
                            <div className="subsection-header">
                                <h3><Package size={18} /> Ingredientes</h3>
                                <button type="button" onClick={addIngrediente}>Adicionar</button>
                            </div>
                            <div className="dynamic-list">
                                {form.ingredientes.map((ingrediente, index) => (
                                    <div key={`ingrediente-${index}`} className="dynamic-row">
                                        <input
                                            value={ingrediente.valor}
                                            onChange={(e) => updateIngrediente(index, e.target.value)}
                                            placeholder="Ex: 2 fatias de bacon"
                                        />
                                        <button type="button" onClick={() => removeIngrediente(index)} disabled={form.ingredientes.length === 1}>
                                            <Trash2 size={16} />
                                        </button>
                                    </div>
                                ))}
                            </div>
                        </div>

                        <div className="form-section">
                            <div className="subsection-header">
                                <h3><ChefHat size={18} /> Passos com vídeo</h3>
                                <button type="button" onClick={addPasso}>Adicionar passo</button>
                            </div>
                            <div className="dynamic-steps">
                                {form.passos.map((passo, index) => (
                                    <div key={`passo-${index}`} className="step-card">
                                        <div className="step-card-header">
                                            <span>Passo {index + 1}</span>
                                            <button type="button" onClick={() => removePasso(index)} disabled={form.passos.length === 1}>
                                                <Trash2 size={16} />
                                            </button>
                                        </div>
                                        <textarea
                                            value={passo.descricao}
                                            onChange={(e) => updatePasso(index, 'descricao', e.target.value)}
                                            rows={2}
                                            placeholder="Descreva a execução da etapa"
                                        />
                                        <div className="form-grid">
                                            <label>
                                                Tempo (segundos)
                                                <input
                                                    type="number"
                                                    min="0"
                                                    value={passo.tempoSegundos}
                                                    onChange={(e) => updatePasso(index, 'tempoSegundos', Number(e.target.value))}
                                                />
                                            </label>
                                            <label>
                                                URL do vídeo
                                                <input
                                                    value={passo.videoUrl}
                                                    onChange={(e) => updatePasso(index, 'videoUrl', e.target.value)}
                                                    placeholder="https://..."
                                                />
                                            </label>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        </div>

                        <div className="form-footer">
                            <div className="hint">
                                <Video size={16} />
                                Cada etapa pode ter seu próprio vídeo. A cozinha poderá alternar entre os passos.
                            </div>
                            <button type="submit" className="primary-action" disabled={saving}>
                                <Save size={18} />
                                {saving ? 'Salvando...' : 'Salvar produto'}
                            </button>
                        </div>
                    </form>
                </section>
            </main>

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
