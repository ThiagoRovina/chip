import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { buildApiUrl } from '../utils/api';
import { withTenantHeader } from '../utils/tenant';
import FeedbackModal from '../components/FeedbackModal';
import './TelaPedidos.css';
import {
    ArrowLeft,
    Plus,
    Minus,
    Send,
    Utensils,
    Coffee,
    Beer,
    IceCream,
    Bell,
    LogOut
} from 'lucide-react';

// Tipos
interface Produto {
    id: string;
    nome: string;
    preco: number;
    categoria: string;
    imagem: string; // Emoji
}

interface ItemPedido {
    produto: Produto;
    quantidade: number;
    observacao?: string;
}

// Dados Mockados
const CATEGORIAS = [
    { id: 'esfihas', nome: 'Esfihas', icon: <Utensils size={18} /> },
    { id: 'bebidas', nome: 'Bebidas', icon: <Beer size={18} /> },
    { id: 'sobremesas', nome: 'Sobremesas', icon: <IceCream size={18} /> },
    { id: 'combos', nome: 'Combos', icon: <Coffee size={18} /> },
];

const PRODUTOS: Produto[] = [
    { id: '1', nome: 'Carne', preco: 6.50, categoria: 'esfihas', imagem: '🥩' },
    { id: '2', nome: 'Queijo', preco: 6.50, categoria: 'esfihas', imagem: '🧀' },
    { id: '3', nome: 'Calabresa', preco: 6.50, categoria: 'esfihas', imagem: '🥓' },
    { id: '4', nome: 'Frango c/ Catupiry', preco: 7.00, categoria: 'esfihas', imagem: '🍗' },
    { id: '5', nome: 'Coca-Cola Lata', preco: 6.00, categoria: 'bebidas', imagem: '🥤' },
    { id: '6', nome: 'Suco Laranja', preco: 8.00, categoria: 'bebidas', imagem: '🍊' },
    { id: '7', nome: 'Chocolate', preco: 8.50, categoria: 'sobremesas', imagem: '🍫' },
    { id: '8', nome: 'Romeu e Julieta', preco: 8.50, categoria: 'sobremesas', imagem: '🍓' },
];

export default function TelaPedidos() {
    const navigate = useNavigate();
    const { user, logout } = useAuth();
    const [categoriaAtiva, setCategoriaAtiva] = useState('esfihas');
    const [mesa, setMesa] = useState('01');
    const [itensPedido, setItensPedido] = useState<ItemPedido[]>([]);
    const [loading, setLoading] = useState(false);
    const [tenantName, setTenantName] = useState(' ');
    const [feedback, setFeedback] = useState<{ open: boolean; title: string; message: string; variant: 'success' | 'error' | 'info'; }>({
        open: false,
        title: '',
        message: '',
        variant: 'info'
    });

    const userRole = (user as any)?.cargo?.toUpperCase() || 'GARCOM';

    useEffect(() => {
        fetchTenantInfo();
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

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    const getInitials = (name: string | undefined) => {
        if (!name) return 'U';
        return name.split(' ').map(n => n[0]).join('').substring(0, 2).toUpperCase();
    };

    // Filtra produtos pela categoria
    const produtosVisiveis = PRODUTOS.filter(p => p.categoria === categoriaAtiva);

    // Adicionar produto ao pedido
    const adicionarProduto = (produto: Produto) => {
        setItensPedido(prev => {
            const existente = prev.find(item => item.produto.id === produto.id);
            if (existente) {
                return prev.map(item =>
                    item.produto.id === produto.id
                        ? { ...item, quantidade: item.quantidade + 1 }
                        : item
                );
            }
            return [...prev, { produto, quantidade: 1 }];
        });
    };

    // Ajustar quantidade (+ / -)
    const ajustarQuantidade = (idProduto: string, delta: number) => {
        setItensPedido(prev => prev.map(item => {
            if (item.produto.id === idProduto) {
                const novaQtd = item.quantidade + delta;
                return novaQtd > 0 ? { ...item, quantidade: novaQtd } : null;
            }
            return item;
        }).filter(Boolean) as ItemPedido[]);
    };

    // Calcular Total
    const totalPedido = itensPedido.reduce((acc, item) => acc + (item.produto.preco * item.quantidade), 0);

    const enviarPedido = async () => {
        if (itensPedido.length === 0) return;
        setLoading(true);

        // Monta o DTO esperado pelo backend
        const pedidoDTO = {
            mesa: mesa,
            cliente: `Mesa ${mesa}`, // Opcional, pode ser nome do cliente se tiver input
            itens: itensPedido.map(item => ({
                nomeProduto: item.produto.nome,
                quantidade: item.quantidade,
                observacao: item.observacao || ''
            }))
        };

        try {
            const response = await fetch(buildApiUrl('/api/pedidos'), {
                method: 'POST',
                headers: withTenantHeader({
                    'Content-Type': 'application/json',
                }),
                body: JSON.stringify(pedidoDTO)
            });

            if (response.ok) {
                const pedidoCriado = await response.json();
                setFeedback({
                    open: true,
                    title: 'Pedido enviado',
                    message: `Pedido #${pedidoCriado.id} enviado para a cozinha.`,
                    variant: 'success'
                });
                setItensPedido([]); // Limpa a comanda
            } else {
                setFeedback({
                    open: true,
                    title: 'Não foi possível enviar',
                    message: 'Tente novamente em alguns segundos.',
                    variant: 'error'
                });
            }
        } catch (error) {
            console.error('Erro de conexão:', error);
            setFeedback({
                open: true,
                title: 'Sem conexão',
                message: 'Verifique a internet e tente novamente.',
                variant: 'error'
            });
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="pdv-wrapper">
            {/* --- NAVBAR --- */}
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

            <div className="pdv-container">

                {/* --- ESQUERDA: CATÁLOGO --- */}
                <div className="pdv-catalog">
                    {/* Categorias */}
                    <div className="category-bar">
                        {CATEGORIAS.map(cat => (
                            <button
                                key={cat.id}
                                className={`category-btn ${categoriaAtiva === cat.id ? 'active' : ''}`}
                                onClick={() => setCategoriaAtiva(cat.id)}
                            >
                                <span style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                                    {cat.icon} {cat.nome}
                                </span>
                            </button>
                        ))}
                    </div>

                    {/* Grid de Produtos */}
                    <div className="products-grid">
                        {produtosVisiveis.map(produto => (
                            <div
                                key={produto.id}
                                className="product-card"
                                onClick={() => adicionarProduto(produto)}
                            >
                                <div className="product-img">{produto.imagem}</div>
                                <div className="product-name">{produto.nome}</div>
                                <div className="product-price">R$ {produto.preco.toFixed(2)}</div>
                            </div>
                        ))}
                    </div>
                </div>

                {/* --- DIREITA: COMANDA --- */}
                <div className="pdv-sidebar">
                    <div className="sidebar-header">
                        <div className="mesa-selector">
                            <span style={{ fontWeight: 'bold', color: '#666' }}>MESA:</span>
                            <input
                                type="text"
                                value={mesa}
                                onChange={(e) => setMesa(e.target.value)}
                                style={{ border: 'none', fontSize: '1.2rem', fontWeight: 'bold', width: '50px', outline: 'none' }}
                            />
                        </div>
                    </div>

                    <div className="order-list">
                        {itensPedido.length === 0 ? (
                            <div style={{ textAlign: 'center', padding: '40px', color: '#aaa' }}>
                                <p>Comanda vazia</p>
                                <small>Toque nos produtos para adicionar</small>
                            </div>
                        ) : (
                            itensPedido.map(item => (
                                <div key={item.produto.id} className="order-item">
                                    <div className="item-details">
                                        <span className="item-title">{item.produto.nome}</span>
                                        <span className="item-price-unit">R$ {item.produto.preco.toFixed(2)}</span>
                                    </div>

                                    <div className="item-controls">
                                        <button className="btn-qty-small" onClick={() => ajustarQuantidade(item.produto.id, -1)}>
                                            <Minus size={16} />
                                        </button>
                                        <span style={{ fontWeight: 'bold' }}>{item.quantidade}</span>
                                        <button className="btn-qty-small" onClick={() => ajustarQuantidade(item.produto.id, 1)}>
                                            <Plus size={16} />
                                        </button>
                                    </div>

                                    <div className="item-total">
                                        R$ {(item.produto.preco * item.quantidade).toFixed(2)}
                                    </div>
                                </div>
                            ))
                        )}
                    </div>

                    <div className="sidebar-footer">
                        <div className="total-row">
                            <span>TOTAL</span>
                            <span>R$ {totalPedido.toFixed(2)}</span>
                        </div>
                        <button
                            className="btn-send"
                            onClick={enviarPedido}
                            disabled={itensPedido.length === 0 || loading}
                        >
                            <Send size={24} />
                            {loading ? 'ENVIANDO...' : 'ENVIAR PARA COZINHA'}
                        </button>
                    </div>
                </div>
            </div>
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
