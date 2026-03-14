import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { buildApiUrl } from '../utils/api';
import { withTenantHeader } from '../utils/tenant';
import "./TelaLogin.css";

interface UsuarioLogin {
  idUsuario: string;
  nmUsuario: string;
  nmEmailUsuario: string;
  fotoPerfilUsuario?: string;
}

const TelaLogin: React.FC = () => {
  const [erro, setErro] = useState('');
  const [tenantName, setTenantName] = useState('');
  const [usuarios, setUsuarios] = useState<UsuarioLogin[]>([]);
  const [carregandoUsuarios, setCarregandoUsuarios] = useState(true);
  const [usuarioSelecionado, setUsuarioSelecionado] = useState<UsuarioLogin | null>(null);
  const [pin, setPin] = useState('');
  const { login, loading } = useAuth();
  const navigate = useNavigate();

  const fetchTenantInfo = useCallback(async () => {
    try {
      const response = await fetch(buildApiUrl('/api/tenant/current'), {
        headers: withTenantHeader()
      });
      if (response.ok) {
        const data = await response.json();
        setTenantName(data.name || '');
      }
    } catch (error) {
      console.error('Erro ao buscar informações do tenant:', error);
    }
  }, []);

  const fetchUsuarios = useCallback(async () => {
    try {
      setCarregandoUsuarios(true);
      const response = await fetch(buildApiUrl('/api/usuario'), {
        headers: withTenantHeader()
      });
      if (!response.ok) {
        throw new Error('Falha ao carregar usuários');
      }
      const data = await response.json();
      setUsuarios(Array.isArray(data) ? data : []);
    } catch (error) {
      console.error('Erro ao buscar usuários:', error);
      setErro('Não foi possível carregar a lista de usuários.');
    } finally {
      setCarregandoUsuarios(false);
    }
  }, []);

  useEffect(() => {
    fetchTenantInfo();
    fetchUsuarios();
  }, [fetchTenantInfo, fetchUsuarios]);

  const abrirTelaPin = (usuario: UsuarioLogin) => {
    setUsuarioSelecionado(usuario);
    setPin('');
    setErro('');
  };

  const voltarParaUsuarios = () => {
    setUsuarioSelecionado(null);
    setPin('');
    setErro('');
  };

  const handleKeypad = (tecla: string) => {
    if (!usuarioSelecionado || loading) {
      return;
    }

    if (tecla === 'delete') {
      setPin((prev) => prev.slice(0, -1));
      return;
    }

    if (pin.length >= 6) {
      return;
    }

    if (/^\d$/.test(tecla)) {
      setPin((prev) => prev + tecla);
    }
  };

  const autenticarComPin = useCallback(async (pinAtual: string) => {
    if (!usuarioSelecionado) {
      return;
    }

    try {
      const user = await login(usuarioSelecionado.nmEmailUsuario, pinAtual);
      const cargo = (user as any)?.cargo?.toUpperCase();

      if (cargo === 'COZINHEIRO' || cargo === 'CHAPEIRO' || cargo === 'CHEF') {
        navigate('/cozinha');
      } else {
        navigate('/home');
      }
    } catch (err: any) {
      const mensagem = typeof err === 'string'
        ? err
        : err?.message || err?.erro || 'PIN inválido';
      setErro(mensagem);
      setPin('');
    }
  }, [usuarioSelecionado, login, navigate]);

  useEffect(() => {
    if (pin.length === 6) {
      autenticarComPin(pin);
    }
  }, [pin, autenticarComPin]);

  const teclado = ['1', '2', '3', '4', '5', '6', '7', '8', '9', '', '0', 'delete'];
  const getIniciais = (nome: string) => nome.split(' ').map((p) => p[0]).join('').slice(0, 2).toUpperCase();
  const tenantDisplayName = tenantName.trim() || 'ChipCook';

  return (
    <main className="login-page" aria-label="Acesso do totem">
      <header className="login-navbar" aria-labelledby="totem-title">
        <div className="login-brand">
          <h1 id="totem-title">{tenantDisplayName}</h1>
        </div>
      </header>

      <section className="login-body" aria-label="Conteúdo de acesso">
        {!usuarioSelecionado ? (
          <div className="login-selection-shell">
            <div className="login-copy">
              <h2 className="login-title">Selecione o usuário</h2>
              <p className="login-subtitle">Toque no cartão para continuar com o token.</p>
            </div>

            {erro && <div className="error-message" role="alert">{erro}</div>}

            <div className="user-cards-grid" role="list" aria-label="Lista de usuários">
              {carregandoUsuarios ? (
                <div className="user-list-status">Carregando usuários...</div>
              ) : usuarios.length === 0 ? (
                <div className="user-list-status">Nenhum usuário encontrado para este tenant.</div>
              ) : (
                usuarios.map((usuario) => (
                  <button
                    key={usuario.idUsuario}
                    className="user-card-login"
                    onClick={() => abrirTelaPin(usuario)}
                    type="button"
                    role="listitem"
                    aria-label={`Entrar como ${usuario.nmUsuario}`}
                    title={usuario.nmEmailUsuario}
                  >
                    <div className={`user-card-media ${usuario.fotoPerfilUsuario ? 'has-photo' : 'no-photo'}`}>
                      {usuario.fotoPerfilUsuario ? (
                        <img src={usuario.fotoPerfilUsuario} alt={`Foto de ${usuario.nmUsuario}`} className="user-card-photo" />
                      ) : (
                        <span className="user-card-initials">{getIniciais(usuario.nmUsuario)}</span>
                      )}
                    </div>
                    <div className="user-card-caption">
                      <span className="user-item-name">{usuario.nmUsuario}</span>
                    </div>
                  </button>
                ))
              )}
            </div>
          </div>
        ) : (
          <div className="pin-panel">
            <div className="pin-panel-header">
              <h2 className="login-title">Digite seu token</h2>
              <p className="login-subtitle">{usuarioSelecionado.nmUsuario}</p>
            </div>

            {erro && <div className="error-message" role="alert">{erro}</div>}

            <div className="selected-user">
              <div className="user-avatar-login">
                {usuarioSelecionado.fotoPerfilUsuario ? (
                  <img src={usuarioSelecionado.fotoPerfilUsuario} alt={`Foto de ${usuarioSelecionado.nmUsuario}`} />
                ) : (
                  getIniciais(usuarioSelecionado.nmUsuario)
                )}
              </div>
              <span>{usuarioSelecionado.nmUsuario}</span>
            </div>

            <div className="pin-dots" aria-label={`Token com ${pin.length} de 6 dígitos`}>
              {[0, 1, 2, 3, 4, 5].map((pos) => (
                <span key={pos} className={`pin-dot ${pin.length > pos ? 'filled' : ''}`} />
              ))}
            </div>

            <div className="keypad-grid" aria-label="Teclado numérico">
              {teclado.map((tecla, idx) => {
                if (!tecla) {
                  return <div key={`empty-${idx}`} className="keypad-empty" />;
                }

                return (
                  <button
                    key={tecla}
                    type="button"
                    className="keypad-btn"
                    onClick={() => handleKeypad(tecla)}
                    disabled={loading}
                    aria-label={tecla === 'delete' ? 'Apagar dígito' : `Dígito ${tecla}`}
                  >
                    {tecla === 'delete' ? '⌫' : tecla}
                  </button>
                );
              })}
            </div>

            <button
              type="button"
              className="back-users-btn"
              onClick={voltarParaUsuarios}
              disabled={loading}
            >
              Voltar para usuários
            </button>
          </div>
        )}
      </section>
    </main>
  );
};

export default TelaLogin;
