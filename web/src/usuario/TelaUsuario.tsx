import React, { useState, useEffect } from 'react';
import { useAuth } from '../auth/AuthContext';
import { useNavigate } from 'react-router-dom';
import { buildApiUrl } from '../utils/api';
import { withTenantHeader } from '../utils/tenant';
import FeedbackModal from '../components/FeedbackModal';
import "./TelaUsuario.css";
import { 
  Search, 
  LogOut, 
  UserPlus,
  Trash2,
  Edit,
  Bell,
  User,
  Mail,
  ArrowLeft
} from 'lucide-react';

interface Usuario {
  idUsuario: string;
  nmUsuario: string;
  nmEmailUsuario: string;
  fotoPerfilUsuario?: string;
}

const TelaUsuario: React.FC = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [usuarios, setUsuarios] = useState<Usuario[]>([]);
  const [loading, setLoading] = useState(true);
  const [tenantName, setTenantName] = useState(' ');
  const [feedback, setFeedback] = useState<{ open: boolean; title: string; message: string; variant: 'success' | 'error' | 'info'; }>({
    open: false,
    title: '',
    message: '',
    variant: 'info'
  });
  const [confirmarExclusao, setConfirmarExclusao] = useState<Usuario | null>(null);

  // Normaliza o cargo para garantir a comparação
  const userRole = (user as any)?.cargo?.toUpperCase() || 'GERENTE';

  useEffect(() => {
    fetchUsuarios();
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

  const fetchUsuarios = async () => {
    try {
      const response = await fetch(buildApiUrl('/api/usuario'), {
        headers: withTenantHeader()
      });
      if (response.ok) {
        const data = await response.json();
        setUsuarios(data);
      }
    } catch (error) {
      console.error('Erro ao buscar usuários:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id: string) => {
    try {
      const response = await fetch(buildApiUrl(`/api/usuario/${id}`), {
        method: 'DELETE',
        headers: withTenantHeader()
      });

      if (response.ok) {
        fetchUsuarios();
        setConfirmarExclusao(null);
      } else {
        setFeedback({
          open: true,
          title: 'Não foi possível excluir',
          message: 'Tente novamente em alguns segundos.',
          variant: 'error'
        });
      }
    } catch (error) {
      console.error('Erro ao excluir:', error);
      setFeedback({
        open: true,
        title: 'Sem conexão',
        message: 'Verifique a internet e tente novamente.',
        variant: 'error'
      });
    }
  };

  const handleEdit = (user: Usuario) => {
    navigate(`/usuarios/editar/${user.idUsuario}`);
  };

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const getInitials = (name: string | undefined) => {
    if (!name) return '?';
    return name.split(' ').map(n => n[0]).join('').substring(0, 2).toUpperCase();
  };

  return (
    <div className="dashboard-container">
      
      {/* --- NAVBAR --- */}
      <nav className="top-nav" aria-label="Navegação Principal">
          <div className="nav-left">
              <button className="back-btn" onClick={() => navigate('/home')} aria-label="Voltar">
                  <ArrowLeft size={24} />
              </button>
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

      {/* --- CONTEÚDO --- */}
      <main className="main-content">
        <div className="page-header">
          <div className="page-title">
            <h1>Gerenciar Equipe</h1>
          </div>
          <div style={{display: 'flex', gap: '10px'}}>
            <button className="new-user-btn" onClick={() => navigate('/usuarios/cadastro')}>
                <UserPlus size={24} /> 
                <span>Adicionar Pessoa</span>
            </button>
          </div>
        </div>

        {/* GRID DE CARTÕES (Visual e Intuitivo) */}
        <div className="user-grid">
          {loading ? (
            <div className="empty-state">Carregando equipe...</div>
          ) : usuarios.length === 0 ? (
            <div className="empty-state">
              <User size={48} color="#ccc" />
              <p>Nenhuma pessoa cadastrada ainda.</p>
            </div>
          ) : (
            usuarios.map((u) => (
              <article key={u.idUsuario} className="user-card">
                
                {/* Avatar Grande para Identificação Visual */}
                <div className="card-avatar-large">
                  {u.fotoPerfilUsuario ? (
                    <img src={u.fotoPerfilUsuario} alt={u.nmUsuario} style={{width: '100%', height: '100%', borderRadius: '50%', objectFit: 'cover'}} />
                  ) : (
                    getInitials(u.nmUsuario)
                  )}
                </div>

                {/* Informações Principais */}
                <div className="user-info">
                  <h3>{u.nmUsuario}</h3>
                  <p>
                    <Mail size={16} /> {u.nmEmailUsuario}
                  </p>
                </div>

                {/* Botões de Ação Grandes */}
                <div className="card-actions">
                  <button 
                    className="action-btn btn-edit-card" 
                    onClick={() => handleEdit(u)}
                    aria-label={`Editar ${u.nmUsuario}`}
                    title="Editar Cadastro"
                  >
                    <Edit size={24} />
                    <span>Editar</span>
                  </button>
                  
                  <button 
                    className="action-btn btn-delete-card" 
                    onClick={() => setConfirmarExclusao(u)}
                    aria-label={`Excluir ${u.nmUsuario}`}
                    title="Excluir Cadastro"
                  >
                    <Trash2 size={24} />
                    <span>Excluir</span>
                  </button>
                </div>

              </article>
            ))
          )}
        </div>
      </main>
      <FeedbackModal
        open={feedback.open}
        title={feedback.title}
        message={feedback.message}
        variant={feedback.variant}
        onClose={() => setFeedback({ open: false, title: '', message: '', variant: 'info' })}
      />

      {confirmarExclusao && (
        <div className="confirm-modal-overlay" onClick={(e) => { if (e.target === e.currentTarget) setConfirmarExclusao(null); }}>
          <div className="confirm-modal-content">
            <h3>Confirmar exclusão</h3>
            <p>
              Deseja excluir <strong>{confirmarExclusao.nmUsuario}</strong>?
              Essa ação não pode ser desfeita.
            </p>
            <div className="confirm-modal-actions">
              <button
                type="button"
                className="confirm-btn-cancel"
                onClick={() => setConfirmarExclusao(null)}
              >
                Cancelar
              </button>
              <button
                type="button"
                className="confirm-btn-delete"
                onClick={() => handleDelete(confirmarExclusao.idUsuario)}
              >
                Confirmar exclusão
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default TelaUsuario;
