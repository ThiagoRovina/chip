import React from 'react';
import { Navigate } from 'react-router-dom';

// Mantém compatibilidade com rota antiga apontando para a tela de estoque atual.
export default function TelaEstoquePorCategoria() {
    return <Navigate to="/estoque" replace />;
}
