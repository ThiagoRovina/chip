import React, { createContext, useState, useContext, ReactNode, useEffect } from 'react';
import axios from 'axios';
import { withTenantHeader } from '../utils/tenant';
import { buildApiUrl } from '../utils/api';

interface User {
    idUsuario: string;
    usuarioId?: string;
    nmUsuario: string;
    nmEmailUsuario: string;
    fotoPerfilUsuario?: string;
    cargo?: string; // Adicionado campo cargo vindo do backend
    authorities?: { authority: string }[]; // Mantido para compatibilidade se necessário
}

const normalizeUser = (rawUser: any): User | null => {
    if (!rawUser) {
        return null;
    }

    const normalizedUser: User = {
        ...rawUser,
        idUsuario: rawUser.idUsuario ?? rawUser.usuarioId ?? ''
    };

    if (normalizedUser.cargo) {
        normalizedUser.cargo = normalizedUser.cargo.toUpperCase();
    }

    return normalizedUser;
};

// Interface para o valor do contexto de autenticação
interface AuthContextType {
    user: User | null;
    loading: boolean;
    isAuthenticated: boolean;
    login: (email: string, senha: string) => Promise<User>;
    logout: () => void;
    hasRole: (role: string) => boolean;
}

const AuthContext = createContext<AuthContextType | null>(null);

export const AuthProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
    const [user, setUser] = useState<User | null>(() => {
        const savedUser = localStorage.getItem('user');
        return savedUser ? normalizeUser(JSON.parse(savedUser)) : null;
    });
    const [loading, setLoading] = useState<boolean>(false);

    useEffect(() => {
        if (user) {
            localStorage.setItem('user', JSON.stringify(user));
        } else {
            localStorage.removeItem('user');
        }
    }, [user]);

    const login = async (email: string, senha: string): Promise<User> => {
        setLoading(true);
        try {
            const response = await axios.post<User>(buildApiUrl('/api/usuario/login'), {
                email: email,
                senha: senha
            }, {
                headers: withTenantHeader({
                    'Content-Type': 'application/json',
                })
            });

            const userData = normalizeUser(response.data);

            setUser(userData);
            setLoading(false);
            return userData as User;
        } catch (error: any) {
            setLoading(false);
            throw error.response ? error.response.data : new Error('Erro de rede ou servidor indisponível');
        }
    };

    const logout = () => {
        setUser(null);
    };

    const hasRole = (role: string): boolean => {
        if (!user) return false;
        // Verifica tanto no campo cargo direto quanto em authorities (legado)
        if (user.cargo === role) return true;
        return user.authorities ? user.authorities.some(auth => auth.authority === role) : false;
    };

    const value: AuthContextType = {
        user,
        loading,
        login,
        logout,
        isAuthenticated: !!user,
        hasRole
    };

    return (
        <AuthContext.Provider value={value}>
            {children}
        </AuthContext.Provider>
    );
};

export const useAuth = (): AuthContextType => {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error('useAuth deve ser usado dentro de um AuthProvider');
    }
    return context;
};
