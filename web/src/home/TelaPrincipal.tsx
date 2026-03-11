import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { withTenantHeader } from '../utils/tenant';
import "./TelaPrincipal.css";
import {
    Users,
    LogOut,
    Bell,
    UtensilsCrossed,
    ChefHat,
    ClipboardList,
    DollarSign,
    Package
} from 'lucide-react';

// Definição dos itens do menu e suas permissões
const MENU_ITEMS = [
    {
        id: 'usuarios',
        title: 'Usuários',
        description: 'Cadastre e gerencie o acesso de funcionários.',
        icon: <Users size={32} />,
        path: '/usuarios',
        roles: ['GERENTE']
    },
    {
        id: 'estoque',
        title: 'Estoque',
        description: 'Controle de entradas, saídas e validade.',
        icon: <Package size={32} />,
        path: '/estoque',
        roles: ['GERENTE', 'COZINHEIRO', 'ESTOQUISTA']
    },
    {
        id: 'cozinha',
        title: 'Cozinha',
        description: 'Visualize e gerencie a fila de pedidos em tempo real.',
        icon: <ChefHat size={32} />,
        path: '/cozinha',
        roles: ['GERENTE', 'COZINHEIRO', 'CHAPEIRO']
    },
    {
        id: 'pedidos',
        title: 'Pedidos',
        description: 'Novo pedido e gerenciamento de mesas.',
        icon: <ClipboardList size={32} />,
        path: '/pedidos',
        roles: ['GERENTE', 'GARCOM', 'CAIXA']
    },
    {
        id: 'cardapio',
        title: 'Cardápio',
        description: 'Edite produtos, preços e disponibilidade.',
        icon: <UtensilsCrossed size={32} />,
        path: '/cardapio',
        roles: ['GERENTE', 'CHEF']
    }
];

export default function TelaPrincipal() {
    const navigate = useNavigate();
    const { user, logout } = useAuth();
    const [tenantName, setTenantName] = useState(' ');

    // Normaliza o cargo para garantir a comparação
    const userRole = (user as any)?.cargo?.toUpperCase() || 'GERENTE';

    useEffect(() => {
        fetchTenantInfo();
    }, []);

    const fetchTenantInfo = async () => {
        try {
            const response = await fetch('http://localhost:8080/api/tenant/current', {
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

    // Filtra os itens baseado no cargo do usuário
    const allowedMenuItems = MENU_ITEMS.filter(item => item.roles.includes(userRole));

    return (
        <div className="dashboard-container">
            {/* --- NAVBAR ACESSÍVEL --- */}
            <nav className="top-nav" aria-label="Navegação Principal">
                <div className="nav-left">
                    <span className="nav-title">{tenantName}</span>
                </div>

                <div className="nav-right">
                    <button className="icon-btn" title="Notificações" aria-label="3 novas notificações">
                        <Bell size={24} />
                        <span className="notification-badge"></span>
                    </button>

                    <div className="user-profile" role="group" aria-label="Menu do Usuário">
                        <div className="user-avatar" aria-hidden="true">
                            {getInitials(user?.nmUsuario)}
                        </div>
                        <div className="user-info">
                            <span className="user-name">{user?.nmUsuario || 'Usuário'}</span>
                            <span className="user-role">{userRole}</span>
                        </div>

                        <button
                            className="icon-btn"
                            onClick={handleLogout}
                            title="Sair do sistema"
                            aria-label="Sair do sistema"
                        >
                            <LogOut size={24} color="#D32F2F" />
                        </button>
                    </div>
                </div>
            </nav>

            {/* --- HERO SECTION (FULL WIDTH) --- */}
            <header className="welcome-section">
                <div className="welcome-content">
                    <h1>Olá, {user?.nmUsuario?.split(' ')[0] || 'Visitante'}! 👋</h1>
                    <p>Bem-vindo ao painel de controle do <strong>{tenantName}</strong>. O que você deseja gerenciar hoje?</p>
                </div>
            </header>

            {/* --- CONTEÚDO PRINCIPAL --- */}
            <main className="main-content">

                {/* ACESSO RÁPIDO */}
                <section aria-labelledby="quick-access-title">
                    <h2 id="quick-access-title" className="section-title">Acesso Rápido</h2>

                    {allowedMenuItems.length > 0 ? (
                        <div className="cards-grid">
                            {allowedMenuItems.map((item) => (
                                <button
                                    key={item.id}
                                    className="dashboard-card"
                                    onClick={() => navigate(item.path)}
                                    aria-label={`Ir para ${item.title}`}
                                >
                                    <div className="card-icon">
                                        {item.icon}
                                    </div>
                                    <h3>{item.title}</h3>
                                    <p>{item.description}</p>
                                </button>
                            ))}
                        </div>
                    ) : (
                        <div style={{ padding: '20px', background: '#fff', borderRadius: '8px', textAlign: 'center' }}>
                            <p>Seu perfil ({userRole}) não possui acessos rápidos configurados.</p>
                        </div>
                    )}
                </section>

            </main>
        </div>
    );
}
