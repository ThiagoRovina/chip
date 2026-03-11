import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { withTenantHeader } from '../utils/tenant';
import FeedbackModal from '../components/FeedbackModal';
import './TelaCozinha.css';
import { 
    Clock, 
    CheckCircle, 
    AlertTriangle, 
    ChefHat,
    LogOut,
    Bell,
    ArrowLeft,
    X,
    Play,
    Pause,
    RotateCcw,
    Video,
    CheckSquare
} from 'lucide-react';

// Tipos
interface ItemPedido {
    id: string;
    nomeProduto: string; // Backend retorna nomeProduto
    quantidade: number;
    observacao?: string;
}

interface Pedido {
    id: string;
    mesa?: string;
    cliente?: string;
    itens: ItemPedido[];
    dataHora: string; // Backend retorna string ISO
    status: 'novo' | 'preparando' | 'pronto';
}

interface PassoReceita {
    descricao: string;
    tempoSegundos: number;
}

interface Receita {
    nome: string;
    ingredientes: string[];
    passos: PassoReceita[];
}

// Mock de Receitas (Mantido local por enquanto)
const MOCK_RECEITAS: Record<string, Receita> = {
    'X-Bacon': {
        nome: 'X-Bacon Clássico',
        ingredientes: [
            '1 Pão de Hambúrguer', 
            '1 Hambúrguer 180g', 
            '2 Fatias de Bacon', 
            '1 Fatia de Queijo Cheddar', 
            'Maionese da Casa',
            'Alface e Tomate'
        ],
        passos: [
            { descricao: 'Selar o pão na chapa com manteiga.', tempoSegundos: 10 },
            { descricao: 'Grelhar o hambúrguer (3 min cada lado).', tempoSegundos: 15 },
            { descricao: 'Fritar o bacon até ficar crocante.', tempoSegundos: 10 },
            { descricao: 'Derreter o queijo sobre a carne.', tempoSegundos: 5 },
            { descricao: 'Montar: Pão, Carne, Queijo, Bacon, Salada.', tempoSegundos: 10 }
        ]
    },
    'Esfiha Carne': {
        nome: 'Esfiha de Carne',
        ingredientes: ['Massa de Esfiha', 'Recheio de Carne Temperada', 'Limão'],
        passos: [
            { descricao: 'Abrir a massa em formato circular.', tempoSegundos: 10 },
            { descricao: 'Adicionar o recheio de carne no centro.', tempoSegundos: 5 },
            { descricao: 'Assar no forno a 250°C.', tempoSegundos: 20 }
        ]
    }
};

const DEFAULT_RECEITA: Receita = {
    nome: 'Receita Padrão',
    ingredientes: ['Ingrediente 1', 'Ingrediente 2', 'Ingrediente 3'],
    passos: [
        { descricao: 'Preparar os ingredientes.', tempoSegundos: 5 },
        { descricao: 'Executar o processo de cozimento.', tempoSegundos: 10 },
        { descricao: 'Finalizar e empratar.', tempoSegundos: 5 }
    ]
};

export default function TelaCozinha() {
    const navigate = useNavigate();
    const { user, logout } = useAuth();
    const [pedidos, setPedidos] = useState<Pedido[]>([]);
    const [now, setNow] = useState(new Date());
    const [tenantName, setTenantName] = useState(' ');

    // Estados para o Modal de Receita
    const [receitaAtiva, setReceitaAtiva] = useState<Receita | null>(null);
    const [passoAtual, setPassoAtual] = useState(0);
    const [tempoRestante, setTempoRestante] = useState(0);
    const [timerRodando, setTimerRodando] = useState(false);
    const [passoConcluido, setPassoConcluido] = useState(false);
    const [feedback, setFeedback] = useState<{ open: boolean; title: string; message: string; variant: 'success' | 'error' | 'info'; }>({
        open: false,
        title: '',
        message: '',
        variant: 'info'
    });

    const audioRef = useRef<HTMLAudioElement | null>(null);
    const alarmIntervalRef = useRef<NodeJS.Timeout | null>(null);

    const userRole = (user as any)?.cargo?.toUpperCase() || 'GERENTE';

    // Inicializa o som de beep
    useEffect(() => {
        audioRef.current = new Audio('https://actions.google.com/sounds/v1/alarms/beep_short.ogg');
    }, []);

    // Carrega pedidos iniciais e configura polling
    useEffect(() => {
        carregarPedidos();
        const interval = setInterval(() => {
            setNow(new Date());
            carregarPedidos(); // Atualiza a lista a cada 2 segundos
        }, 2000);
        
        fetchTenantInfo();
        return () => clearInterval(interval);
    }, []);

    const carregarPedidos = async () => {
        try {
            const response = await fetch('http://localhost:8080/api/pedidos', {
                headers: withTenantHeader()
            });
            if (response.ok) {
                const data = await response.json();
                // Ordena por data (mais antigos primeiro)
                const pedidosOrdenados = data.sort((a: Pedido, b: Pedido) => 
                    new Date(a.dataHora).getTime() - new Date(b.dataHora).getTime()
                );
                setPedidos(pedidosOrdenados);
            }
        } catch (error) {
            console.error("Erro ao carregar pedidos:", error);
        }
    };

    // Timer do Passo da Receita
    useEffect(() => {
        let interval: NodeJS.Timeout;
        if (timerRodando && tempoRestante > 0) {
            interval = setInterval(() => {
                setTempoRestante((prev) => prev - 1);
            }, 1000);
        } else if (tempoRestante === 0 && timerRodando) {
            // Tempo acabou
            setTimerRodando(false);
            setPassoConcluido(true);
            startAlarm();
        }
        return () => clearInterval(interval);
    }, [timerRodando, tempoRestante]);

    // Limpa o alarme ao desmontar ou fechar modal
    useEffect(() => {
        return () => stopAlarm();
    }, []);

    const startAlarm = () => {
        if (audioRef.current) {
            audioRef.current.play().catch(e => console.log("Erro ao tocar som:", e));
            alarmIntervalRef.current = setInterval(() => {
                audioRef.current?.play().catch(e => console.log("Erro ao tocar som:", e));
            }, 2000);
        }
    };

    const stopAlarm = () => {
        if (alarmIntervalRef.current) {
            clearInterval(alarmIntervalRef.current);
            alarmIntervalRef.current = null;
        }
    };

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

    const concluirPedido = async (id: string) => {
        try {
            const response = await fetch(`http://localhost:8080/api/pedidos/${id}/status`, {
                method: 'PUT',
                headers: withTenantHeader({
                    'Content-Type': 'application/json',
                }),
                body: 'concluido' // Envia a string direta conforme esperado pelo Controller
            });

            if (response.ok) {
                // Remove da lista visual imediatamente para feedback rápido
                setPedidos(prev => prev.filter(p => p.id !== id));
            } else {
                setFeedback({
                    open: true,
                    title: 'Não foi possível concluir',
                    message: 'Tente novamente em alguns segundos.',
                    variant: 'error'
                });
            }
        } catch (error) {
            console.error('Erro ao concluir pedido:', error);
            setFeedback({
                open: true,
                title: 'Sem conexão',
                message: 'Verifique a internet e tente novamente.',
                variant: 'error'
            });
        }
    };

    const getTempoDecorrido = (dataHoraStr: string) => {
        const dataHora = new Date(dataHoraStr);
        const diff = Math.floor((now.getTime() - dataHora.getTime()) / 1000); // segundos
        const min = Math.floor(diff / 60);
        const sec = diff % 60;
        return `${min.toString().padStart(2, '0')}:${sec.toString().padStart(2, '0')}`;
    };

    const getStatusColorClass = (dataHoraStr: string) => {
        const dataHora = new Date(dataHoraStr);
        const diffMin = (now.getTime() - dataHora.getTime()) / 1000 / 60;
        if (diffMin > 20) return 'header-late';
        if (diffMin > 10) return 'header-warning';
        return 'header-new';
    };

    // --- Lógica da Receita ---

    const abrirReceita = (nomeItem: string) => {
        const receita = MOCK_RECEITAS[nomeItem] || { ...DEFAULT_RECEITA, nome: nomeItem };
        setReceitaAtiva(receita);
        setPassoAtual(0);
        setTempoRestante(receita.passos[0].tempoSegundos);
        setTimerRodando(false);
        setPassoConcluido(false);
        stopAlarm();
    };

    const fecharReceita = () => {
        setReceitaAtiva(null);
        setTimerRodando(false);
        stopAlarm();
    };

    const toggleTimer = () => {
        setTimerRodando(!timerRodando);
    };

    const reiniciarPasso = () => {
        if (!receitaAtiva) return;
        stopAlarm();
        setTempoRestante(receitaAtiva.passos[passoAtual].tempoSegundos);
        setTimerRodando(false);
        setPassoConcluido(false);
    };

    const proximoPasso = () => {
        stopAlarm();
        if (!receitaAtiva) return;
        if (passoAtual < receitaAtiva.passos.length - 1) {
            const proximo = passoAtual + 1;
            setPassoAtual(proximo);
            setTempoRestante(receitaAtiva.passos[proximo].tempoSegundos);
            setTimerRodando(false);
            setPassoConcluido(false);
        } else {
            setFeedback({
                open: true,
                title: 'Receita concluída',
                message: 'Prato pronto para seguir no atendimento.',
                variant: 'success'
            });
            fecharReceita();
        }
    };

    return (
        <div className="kds-container">
            {/* NAVBAR KDS */}
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

            {/* BOARD DE PEDIDOS */}
            <main className="kds-board">
                <div className="kds-stats-bar">
                    <div className="stat-badge stat-waiting">
                        <Clock size={16} />
                        <span>{pedidos.length} Pendentes</span>
                    </div>
                    <div className="stat-badge stat-late">
                        <AlertTriangle size={16} />
                        <span>{pedidos.filter(p => (now.getTime() - new Date(p.dataHora).getTime()) / 60000 > 20).length} Atrasados</span>
                    </div>
                </div>

                {pedidos.length === 0 ? (
                    <div style={{gridColumn: '1/-1', textAlign: 'center', padding: '50px', color: '#aaa'}}>
                        <h2>Tudo limpo! Sem pedidos pendentes. 👨‍🍳✨</h2>
                    </div>
                ) : (
                    pedidos.map(pedido => (
                        <div key={pedido.id} className="order-card">
                            <div className={`order-header ${getStatusColorClass(pedido.dataHora)}`}>
                                <span className="order-id">#{pedido.id}</span>
                                <span className="order-timer">
                                    <Clock size={16} style={{verticalAlign: 'middle', marginRight: '5px'}} />
                                    {getTempoDecorrido(pedido.dataHora)}
                                </span>
                            </div>
                            
                            <div className="order-info">
                                <span>{pedido.mesa ? `Mesa ${pedido.mesa}` : pedido.cliente}</span>
                                <span>{new Date(pedido.dataHora).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})}</span>
                            </div>

                            <ul className="order-items">
                                {pedido.itens.map(item => (
                                    <li 
                                        key={item.id} 
                                        className="order-item clickable-item"
                                        onClick={() => abrirReceita(item.nomeProduto)}
                                        title="Clique para ver a receita"
                                    >
                                        <span className="item-qty">{item.quantidade}</span>
                                        <div className="item-desc">
                                            {item.nomeProduto}
                                            {item.observacao && (
                                                <span className="item-obs">⚠️ {item.observacao}</span>
                                            )}
                                        </div>
                                        <Video size={20} className="recipe-icon" />
                                    </li>
                                ))}
                            </ul>

                            <div className="order-actions">
                                <button className="btn-done" onClick={() => concluirPedido(pedido.id)}>
                                    <CheckCircle size={24} style={{verticalAlign: 'middle', marginRight: '10px'}} />
                                    PRONTO
                                </button>
                            </div>
                        </div>
                    ))
                )}
            </main>

            {/* MODAL DE RECEITA */}
            {receitaAtiva && (
                <div className="recipe-modal-overlay">
                    <div className="recipe-modal">
                        <div className="recipe-header">
                            <h2>
                                <ChefHat size={28} style={{marginRight: '10px'}} />
                                {receitaAtiva.nome}
                            </h2>
                            <button className="close-btn" onClick={fecharReceita}>
                                <X size={24} />
                            </button>
                        </div>

                        <div className="recipe-content">
                            {/* Lado Esquerdo: Vídeo Placeholder e Ingredientes */}
                            <div className="recipe-left">
                                <div className="video-placeholder">
                                    <Video size={48} color="#ccc" />
                                    <span>Vídeo da Receita</span>
                                </div>
                                <div className="ingredients-list">
                                    <h3>Ingredientes</h3>
                                    <ul>
                                        {receitaAtiva.ingredientes.map((ing, idx) => (
                                            <li key={idx}>{ing}</li>
                                        ))}
                                    </ul>
                                </div>
                            </div>

                            {/* Lado Direito: Passos e Timer */}
                            <div className="recipe-right">
                                <div className="step-indicator">
                                    Passo {passoAtual + 1} de {receitaAtiva.passos.length}
                                </div>
                                
                                <div className="step-display">
                                    <p className="step-description">
                                        {receitaAtiva.passos[passoAtual].descricao}
                                    </p>
                                    
                                    <div className={`timer-display ${passoConcluido ? 'timer-done' : ''}`}>
                                        {Math.floor(tempoRestante / 60).toString().padStart(2, '0')}:
                                        {(tempoRestante % 60).toString().padStart(2, '0')}
                                    </div>

                                    {passoConcluido && (
                                        <div className="step-alert">
                                            <Bell size={24} className="bell-icon" />
                                            <span>Passo Concluído!</span>
                                        </div>
                                    )}
                                </div>

                                <div className="step-controls">
                                    {!passoConcluido ? (
                                        <>
                                            <button 
                                                className={`btn-timer ${timerRodando ? 'btn-pause' : 'btn-play'}`}
                                                onClick={toggleTimer}
                                            >
                                                {timerRodando ? <Pause size={24} /> : <Play size={24} />}
                                                {timerRodando ? 'PAUSAR' : 'INICIAR'}
                                            </button>
                                            <button className="btn-reset" onClick={reiniciarPasso}>
                                                <RotateCcw size={24} />
                                            </button>
                                        </>
                                    ) : (
                                        <button className="btn-next-step" onClick={proximoPasso}>
                                            <CheckSquare size={24} style={{marginRight: '8px'}} />
                                            {passoAtual < receitaAtiva.passos.length - 1 ? 'PRÓXIMO PASSO' : 'FINALIZAR'}
                                        </button>
                                    )}
                                </div>
                            </div>
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
