import React, { useState, useEffect, useRef } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { buildApiUrl } from '../utils/api';
import { withTenantHeader } from '../utils/tenant';
import "./telaCadUsuario.css";
import { 
  Search, 
  LogOut, 
  Bell,
  User,
  ArrowLeft,
  Save,
  ScanFace,
  Camera,
  Upload
} from 'lucide-react';

export default function TelaCadUsuario() {
    const navigate = useNavigate();
    const { id } = useParams(); // Pega o ID da URL se estiver editando
    const { user, logout } = useAuth();
    const [loading, setLoading] = useState(false);
    const [saving, setSaving] = useState(false);
    const fileInputRef = useRef<HTMLInputElement>(null);
    const [tenantName, setTenantName] = useState(' ');
    const [confirmarSalvarAberto, setConfirmarSalvarAberto] = useState(false);
    const [retornoSalvar, setRetornoSalvar] = useState<{
        aberto: boolean;
        sucesso: boolean;
        titulo: string;
        mensagem: string;
    }>({
        aberto: false,
        sucesso: false,
        titulo: '',
        mensagem: ''
    });
    
    // Normaliza o cargo para garantir a comparação
    const userRole = (user as any)?.cargo?.toUpperCase() || 'GERENTE';

    // State for all fields
    const [formData, setFormData] = useState({
        // Usuario
        nmUsuario: '',
        nmEmailUsuario: '',
        dsSenhaUsuario: '',
        dsSenhaConfirmacao: '',
        fotoPerfilUsuario: '',
        
        // Funcionario
        dsCargo: '',
        dtNascimento: '',
        dtAdmissao: '',
        tpContrato: 'TEMPORARIO',
        dtSaida: '',
        nrCpf: '',
        nrRg: '',
        nmMae: '',
        dsFuncao: '',
        tpContratacao: '',
        dsSetor: '',
        dsUnidadeTrabalho: '',
        nrTelefone: '',
        nrCep: '',
        nmRua: '',
        nrEndereco: '',
        nmBairro: '',
        nmCidade: '',
        sgEstado: ''
    });

    const [selectedFile, setSelectedFile] = useState<File | null>(null);
    const [previewUrl, setPreviewUrl] = useState<string | null>(null);

    useEffect(() => {
        if (id) {
            carregarDadosUsuario(id);
        }
        fetchTenantInfo();
    }, [id]);

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

    const carregarDadosUsuario = async (userId: string) => {
        setLoading(true);
        try {
            const responseUsuario = await fetch(buildApiUrl(`/api/usuario/${userId}`), {
                headers: withTenantHeader()
            });
            
            if (responseUsuario.ok) {
                const usuarioData = await responseUsuario.json();
                const func = usuarioData.funcionario;
                
                const newState = {
                    ...formData,
                    nmUsuario: usuarioData.nmUsuario,
                    nmEmailUsuario: usuarioData.nmEmailUsuario,
                    fotoPerfilUsuario: usuarioData.fotoPerfilUsuario || '',
                    dsSenhaUsuario: '',
                    dsSenhaConfirmacao: '',
                    dsCargo: func?.dsCargo || '',
                    dtNascimento: func?.dtNascimento || '',
                    dtAdmissao: func?.dtAdmissao || '',
                    tpContrato: func?.tpContrato || 'TEMPORARIO',
                    dtSaida: func?.dtSaida || '',
                    nrCpf: func?.nrCpf || '',
                    nrRg: func?.nrRg || '',
                    nmMae: func?.nmMae || '',
                    dsFuncao: func?.dsFuncao || '',
                    tpContratacao: func?.tpContratacao || '',
                    dsSetor: func?.dsSetor || '',
                    dsUnidadeTrabalho: func?.dsUnidadeTrabalho || '',
                    nrTelefone: func?.nrTelefone || '',
                    nrCep: func?.nrCep || '',
                    nmRua: func?.nmRua || '',
                    nrEndereco: func?.nrEndereco || '',
                    nmBairro: func?.nmBairro || '',
                    nmCidade: func?.nmCidade || '',
                    sgEstado: func?.sgEstado || ''
                };

                if (usuarioData.fotoPerfilUsuario) {
                    setPreviewUrl(usuarioData.fotoPerfilUsuario);
                }

                setFormData(newState);
            } else {
                setRetornoSalvar({
                    aberto: true,
                    sucesso: false,
                    titulo: 'Erro ao carregar',
                    mensagem: 'Não foi possível abrir este usuário.'
                });
                navigate('/usuarios');
            }
        } catch (error) {
            console.error('Erro ao carregar:', error);
            setRetornoSalvar({
                aberto: true,
                sucesso: false,
                titulo: 'Sem conexão',
                mensagem: 'Verifique a internet e tente novamente.'
            });
        } finally {
            setLoading(false);
        }
    };

    const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        if (e.target.files && e.target.files[0]) {
            const file = e.target.files[0];
            setSelectedFile(file);
            
            // Create preview
            const reader = new FileReader();
            reader.onloadend = () => {
                setPreviewUrl(reader.result as string);
            };
            reader.readAsDataURL(file);
        }
    };

    const triggerFileInput = () => {
        fileInputRef.current?.click();
    };

    // Função para buscar CEP
    const handleCepBlur = async (e: React.FocusEvent<HTMLInputElement>) => {
        const cep = e.target.value.replace(/\D/g, ''); // Remove caracteres não numéricos

        if (cep.length === 8) {
            try {
                const response = await fetch(`https://viacep.com.br/ws/${cep}/json/`);
                const data = await response.json();

                if (!data.erro) {
                    setFormData(prev => ({
                        ...prev,
                        nmRua: data.logradouro,
                        nmBairro: data.bairro,
                        nmCidade: data.localidade,
                        sgEstado: data.uf
                    }));
                } else {
                    setRetornoSalvar({
                        aberto: true,
                        sucesso: false,
                        titulo: 'CEP não encontrado',
                        mensagem: 'Confira o número e tente de novo.'
                    });
                }
            } catch (error) {
                console.error("Erro ao buscar CEP:", error);
                setRetornoSalvar({
                    aberto: true,
                    sucesso: false,
                    titulo: 'Erro no CEP',
                    mensagem: 'Não foi possível buscar o CEP agora.'
                });
            }
        }
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        
        // Validação de senha apenas na criação ou se o campo estiver preenchido na edição
        if (!id && formData.dsSenhaUsuario !== formData.dsSenhaConfirmacao) {
            setRetornoSalvar({
                aberto: true,
                sucesso: false,
                titulo: 'Senha diferente',
                mensagem: 'Confirme a senha igual nos dois campos.'
            });
            return;
        }
        if (id && formData.dsSenhaUsuario && formData.dsSenhaUsuario !== formData.dsSenhaConfirmacao) {
            setRetornoSalvar({
                aberto: true,
                sucesso: false,
                titulo: 'Senha diferente',
                mensagem: 'Confirme a senha igual nos dois campos.'
            });
             return;
        }

        setConfirmarSalvarAberto(true);
    };

    const salvarCadastro = async () => {
        setConfirmarSalvarAberto(false);
        setSaving(true);

        try {
            const usuarioData = {
                nmUsuario: formData.nmUsuario,
                nmEmailUsuario: formData.nmEmailUsuario,
                dsSenhaUsuario: formData.dsSenhaUsuario, // Pode ser vazio na edição
                fotoPerfilUsuario: formData.fotoPerfilUsuario, // Mantém a URL antiga se não houver nova foto
                funcionario: {
                    nmFuncionario: formData.nmUsuario,
                    nmEmail: formData.nmEmailUsuario,
                    dsCargo: formData.dsCargo,
                    dtNascimento: formData.dtNascimento || null,
                    dtAdmissao: formData.dtAdmissao || null,
                    tpContrato: formData.tpContrato,
                    dtSaida: formData.dtSaida || null,
                    nrCpf: formData.nrCpf,
                    nrRg: formData.nrRg,
                    nmMae: formData.nmMae,
                    dsFuncao: formData.dsFuncao,
                    tpContratacao: formData.tpContratacao,
                    dsSetor: formData.dsSetor,
                    dsUnidadeTrabalho: formData.dsUnidadeTrabalho,
                    nrTelefone: formData.nrTelefone,
                    nrCep: formData.nrCep,
                    nmRua: formData.nmRua,
                    nrEndereco: formData.nrEndereco,
                    nmBairro: formData.nmBairro,
                    nmCidade: formData.nmCidade,
                    sgEstado: formData.sgEstado
                }
            };

            // Usando FormData para enviar arquivo + JSON
            const formDataToSend = new FormData();
            formDataToSend.append('dados', JSON.stringify(usuarioData));
            
            if (selectedFile) {
                formDataToSend.append('foto', selectedFile);
            }

            const url = id 
                ? buildApiUrl(`/api/usuario/${id}`)
                : buildApiUrl('/api/usuario/registrar');
            
            const method = id ? 'PUT' : 'POST';

            const response = await fetch(url, {
                method: method,
                headers: withTenantHeader(),
                body: formDataToSend
            });

            if (response.ok) {
                setRetornoSalvar({
                    aberto: true,
                    sucesso: true,
                    titulo: 'Tudo certo',
                    mensagem: id ? 'Cadastro atualizado com sucesso.' : 'Cadastro salvo com sucesso.'
                });
            } else {
                const err = await response.text();
                setRetornoSalvar({
                    aberto: true,
                    sucesso: false,
                    titulo: 'Não foi possível salvar',
                    mensagem: err || 'Tente novamente em alguns segundos.'
                });
            }
        } catch (error) {
            console.error('Erro:', error);
            setRetornoSalvar({
                aberto: true,
                sucesso: false,
                titulo: 'Sem conexão',
                mensagem: 'Verifique a internet e tente novamente.'
            });
        } finally {
            setSaving(false);
        }
    };

    const fecharRetornoSalvar = () => {
        const sucesso = retornoSalvar.sucesso;
        setRetornoSalvar({
            aberto: false,
            sucesso: false,
            titulo: '',
            mensagem: ''
        });
        if (sucesso) {
            navigate('/usuarios');
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

    if (loading) {
        return <div className="dashboard-container" style={{justifyContent:'center', alignItems:'center'}}>Carregando...</div>;
    }

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

            <main className="main-content">
                <div className="page-header">
                    <div className="page-title">
                        <h1>{id ? 'Editar Usuário' : 'Novo Usuário'}</h1>
                    </div>
                </div>

                {/* FORM */}
                <form className="form" onSubmit={handleSubmit}>
                    
                    {/* DADOS PESSOAIS */}
                    <section className="card" aria-labelledby="dados-pessoais-title">
                        <h3 id="dados-pessoais-title">Dados Pessoais e Contrato</h3>
                        
                        <div className="form-grid">
                            {/* Foto de Perfil */}
                            <div className="form-group full-width" style={{display: 'flex', flexDirection: 'column', alignItems: 'center', marginBottom: '20px'}}>
                                <div 
                                    className="profile-photo-preview" 
                                    style={{
                                        width: '120px', 
                                        height: '120px', 
                                        borderRadius: '50%', 
                                        backgroundColor: '#f0f0f0', 
                                        display: 'flex', 
                                        alignItems: 'center', 
                                        justifyContent: 'center',
                                        overflow: 'hidden',
                                        marginBottom: '10px',
                                        border: '3px solid #fff',
                                        boxShadow: '0 2px 10px rgba(0,0,0,0.1)'
                                    }}
                                >
                                    {previewUrl ? (
                                        <img src={previewUrl} alt="Preview" style={{width: '100%', height: '100%', objectFit: 'cover'}} />
                                    ) : (
                                        <User size={60} color="#ccc" />
                                    )}
                                </div>
                                <input 
                                    type="file" 
                                    ref={fileInputRef} 
                                    onChange={handleFileChange} 
                                    accept="image/*" 
                                    style={{display: 'none'}} 
                                />
                                <button 
                                    type="button" 
                                    className="btn-upload-photo"
                                    onClick={triggerFileInput}
                                    style={{
                                        display: 'flex',
                                        alignItems: 'center',
                                        gap: '8px',
                                        padding: '8px 16px',
                                        borderRadius: '20px',
                                        border: '1px solid #ddd',
                                        background: '#fff',
                                        cursor: 'pointer',
                                        fontSize: '0.9rem'
                                    }}
                                >
                                    <Camera size={18} />
                                    Alterar Foto
                                </button>
                            </div>

                            <div className="form-group full-width">
                                <label htmlFor="nmUsuario">Nome Completo <span className="required">*</span></label>
                                <input 
                                    id="nmUsuario"
                                    name="nmUsuario"
                                    placeholder="Ex: João da Silva" 
                                    value={formData.nmUsuario}
                                    onChange={handleChange}
                                    required
                                />
                            </div>

                            <div className="form-group full-width">
                                <label htmlFor="dsCargo">Cargo / Função</label>
                                <select 
                                    id="dsCargo"
                                    name="dsCargo"
                                    value={formData.dsCargo}
                                    onChange={handleChange}
                                >
                                    <option value="">Selecione...</option>
                                    <option value="GERENTE">Gerente</option>
                                    <option value="COZINHEIRO">Cozinheiro</option>
                                    <option value="GARCOM">Garçom</option>
                                </select>
                            </div>

                            <div className="form-group">
                                <label htmlFor="dtNascimento">Data de Nascimento</label>
                                <input 
                                    id="dtNascimento"
                                    name="dtNascimento"
                                    type="date" 
                                    value={formData.dtNascimento}
                                    onChange={handleChange}
                                />
                            </div>

                            <div className="form-group">
                                <label htmlFor="dtAdmissao">Data de Admissão</label>
                                <input 
                                    id="dtAdmissao"
                                    name="dtAdmissao"
                                    type="date" 
                                    value={formData.dtAdmissao}
                                    onChange={handleChange}
                                />
                            </div>

                            <div className="radio-group-container full-width">
                                <span className="radio-group-label">Tipo de Contrato</span>
                                <div className="radio-options" role="radiogroup">
                                    <label className="radio-option">
                                        <input 
                                            type="radio" 
                                            name="tpContrato" 
                                            value="FIXO"
                                            checked={formData.tpContrato === 'FIXO'}
                                            onChange={handleChange}
                                        /> Fixo (Efetivo)
                                    </label>
                                    <label className="radio-option">
                                        <input 
                                            type="radio" 
                                            name="tpContrato" 
                                            value="TEMPORARIO"
                                            checked={formData.tpContrato === 'TEMPORARIO'}
                                            onChange={handleChange}
                                        /> Temporário / Bico
                                    </label>
                                </div>
                            </div>

                            <div className="form-group">
                                <label htmlFor="dtSaida">Data de Saída (Opcional)</label>
                                <input 
                                    id="dtSaida"
                                    name="dtSaida"
                                    type="date" 
                                    value={formData.dtSaida}
                                    onChange={handleChange}
                                />
                            </div>
                        </div>
                    </section>

                    {/* DOCUMENTAÇÃO */}
                    <section className="card" aria-labelledby="docs-title">
                        <h3 id="docs-title">Documentação e Detalhes</h3>

                        <div className="form-grid">
                            <div className="form-group">
                                <label htmlFor="nrCpf">CPF</label>
                                <input 
                                    id="nrCpf"
                                    name="nrCpf"
                                    placeholder="000.000.000-00" 
                                    value={formData.nrCpf}
                                    onChange={handleChange}
                                />
                            </div>

                            <div className="form-group">
                                <label htmlFor="nrRg">RG / Outro Doc.</label>
                                <input 
                                    id="nrRg"
                                    name="nrRg"
                                    placeholder="00.000.000-0" 
                                    value={formData.nrRg}
                                    onChange={handleChange}
                                />
                            </div>

                            <div className="form-group full-width">
                                <label htmlFor="nmMae">Nome Completo da Mãe</label>
                                <input 
                                    id="nmMae"
                                    name="nmMae"
                                    placeholder="Nome da mãe" 
                                    value={formData.nmMae}
                                    onChange={handleChange}
                                />
                            </div>

                            <div className="form-group">
                                <label htmlFor="dsFuncao">Função</label>
                                <select id="dsFuncao" name="dsFuncao" value={formData.dsFuncao} onChange={handleChange}>
                                    <option value="">Selecione...</option>
                                    <option value="OPERACIONAL">Operacional</option>
                                    <option value="ADMINISTRATIVO">Administrativo</option>
                                </select>
                            </div>

                            <div className="form-group">
                                <label htmlFor="tpContratacao">Regime de Contratação</label>
                                <select id="tpContratacao" name="tpContratacao" value={formData.tpContratacao} onChange={handleChange}>
                                    <option value="">Selecione...</option>
                                    <option value="CLT">CLT</option>
                                    <option value="PJ">PJ</option>
                                    <option value="FREELANCE">Freelance</option>
                                </select>
                            </div>

                            <div className="form-group">
                                <label htmlFor="dsSetor">Setor / Departamento</label>
                                <input 
                                    id="dsSetor"
                                    name="dsSetor"
                                    placeholder="Ex: Cozinha" 
                                    value={formData.dsSetor}
                                    onChange={handleChange}
                                />
                            </div>

                            <div className="form-group">
                                <label htmlFor="dsUnidadeTrabalho">Unidade de Trabalho</label>
                                <input 
                                    id="dsUnidadeTrabalho"
                                    name="dsUnidadeTrabalho"
                                    placeholder="Ex: Unidade Centro" 
                                    value={formData.dsUnidadeTrabalho}
                                    onChange={handleChange}
                                />
                            </div>
                        </div>
                    </section>

                    {/* CONTATO */}
                    <section className="card" aria-labelledby="contato-title">
                        <h3 id="contato-title">Dados de Contato</h3>
                        <div className="form-grid">
                            <div className="form-group full-width">
                                <label htmlFor="nmEmailUsuario">Email Corporativo <span className="required">*</span></label>
                                <input 
                                    id="nmEmailUsuario"
                                    name="nmEmailUsuario"
                                    type="email"
                                    placeholder="usuario@empresa.com" 
                                    value={formData.nmEmailUsuario}
                                    onChange={handleChange}
                                    required
                                />
                            </div>

                            <div className="form-group full-width">
                                <label htmlFor="nrTelefone">Telefone / Celular</label>
                                <input 
                                    id="nrTelefone"
                                    name="nrTelefone"
                                    placeholder="(00) 00000-0000" 
                                    value={formData.nrTelefone}
                                    onChange={handleChange}
                                />
                            </div>
                        </div>
                    </section>

                    {/* ENDEREÇO */}
                    <section className="card" aria-labelledby="endereco-title">
                        <h3 id="endereco-title">Endereço</h3>
                        <div className="form-grid">
                            <div className="form-group">
                                <label htmlFor="nrCep">CEP</label>
                                <input 
                                    id="nrCep"
                                    name="nrCep"
                                    placeholder="00000-000" 
                                    value={formData.nrCep}
                                    onChange={handleChange}
                                    onBlur={handleCepBlur}
                                />
                            </div>

                            <div className="form-group full-width">
                                <label htmlFor="nmRua">Rua / Avenida</label>
                                <input 
                                    id="nmRua"
                                    name="nmRua"
                                    placeholder="Nome da rua" 
                                    value={formData.nmRua}
                                    onChange={handleChange}
                                />
                            </div>

                            <div className="form-group">
                                <label htmlFor="nrEndereco">Número</label>
                                <input 
                                    id="nrEndereco"
                                    name="nrEndereco"
                                    placeholder="123" 
                                    value={formData.nrEndereco}
                                    onChange={handleChange}
                                />
                            </div>

                            <div className="form-group">
                                <label htmlFor="nmBairro">Bairro</label>
                                <input 
                                    id="nmBairro"
                                    name="nmBairro"
                                    placeholder="Bairro" 
                                    value={formData.nmBairro}
                                    onChange={handleChange}
                                />
                            </div>

                            <div className="form-group">
                                <label htmlFor="nmCidade">Cidade</label>
                                <input 
                                    id="nmCidade"
                                    name="nmCidade"
                                    placeholder="Cidade" 
                                    value={formData.nmCidade}
                                    onChange={handleChange}
                                />
                            </div>

                            <div className="form-group">
                                <label htmlFor="sgEstado">Estado (UF)</label>
                                <input 
                                    id="sgEstado"
                                    name="sgEstado"
                                    placeholder="SP" 
                                    value={formData.sgEstado}
                                    onChange={handleChange}
                                    maxLength={2}
                                />
                            </div>
                        </div>
                    </section>

                    {/* ACESSO */}
                    <section className="card" aria-labelledby="acesso-title">
                        <h3 id="acesso-title">Dados de Acesso</h3>
                        <div className="form-grid">
                            <div className="form-group">
                                <label htmlFor="dsSenhaUsuario">Senha {id && <span style={{fontWeight:'normal', fontSize:'0.8rem', color:'#666'}}>(Deixe em branco para manter)</span>} {!id && <span className="required">*</span>}</label>
                                <input 
                                    id="dsSenhaUsuario"
                                    name="dsSenhaUsuario"
                                    type="password" 
                                    placeholder="******" 
                                    value={formData.dsSenhaUsuario}
                                    onChange={handleChange}
                                    required={!id}
                                />
                            </div>

                            <div className="form-group">
                                <label htmlFor="dsSenhaConfirmacao">Confirmar Senha {!id && <span className="required">*</span>}</label>
                                <input 
                                    id="dsSenhaConfirmacao"
                                    name="dsSenhaConfirmacao"
                                    type="password" 
                                    placeholder="******" 
                                    value={formData.dsSenhaConfirmacao}
                                    onChange={handleChange}
                                    required={!id}
                                />
                            </div>
                        </div>
                    </section>

                    {/* ACTIONS */}
                    <div className="actions">
                        <button type="button" className="btn-bio">
                            <ScanFace size={20} />
                            Cadastrar biometria facial
                        </button>

                        <button type="submit" className="btn-salvar" disabled={saving}>
                            <Save size={20} style={{marginRight: '8px'}} />
                            {saving ? 'Salvando...' : id ? 'Atualizar Cadastro' : 'Salvar Cadastro'}
                        </button>
                    </div>
                </form>
            </main>

            {confirmarSalvarAberto && (
                <div className="modal-overlay" role="dialog" aria-modal="true" aria-label="Confirmar salvamento">
                    <div className="modal-card">
                        <h2>Confirmar</h2>
                        <p>Deseja salvar este cadastro agora?</p>
                        <div className="modal-actions">
                            <button type="button" className="btn-modal-secondary" onClick={() => setConfirmarSalvarAberto(false)}>
                                Voltar
                            </button>
                            <button type="button" className="btn-modal-primary" onClick={salvarCadastro}>
                                Sim, salvar
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {retornoSalvar.aberto && (
                <div className="modal-overlay" role="dialog" aria-modal="true" aria-label="Resultado do salvamento">
                    <div className={`modal-card ${retornoSalvar.sucesso ? 'modal-success' : 'modal-error'}`}>
                        <div className="modal-emoji" aria-hidden="true">{retornoSalvar.sucesso ? '✅' : '⚠️'}</div>
                        <h2>{retornoSalvar.titulo}</h2>
                        <p>{retornoSalvar.mensagem}</p>
                        <div className="modal-actions single">
                            <button type="button" className="btn-modal-primary" onClick={fecharRetornoSalvar}>
                                Entendi
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}
