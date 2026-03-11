import React from 'react';
import './App.css';
import { BrowserRouter as Router, Route, Routes, Navigate } from 'react-router-dom';
import { AuthProvider } from './auth/AuthContext';
import TelaLogin from "./login/TelaLogin";
import TelaPrincipal from "./home/TelaPrincipal";
import TelaUsuario from "./usuario/TelaUsuario";
import TelaCadUsuario from "./usuario/telaCadUsuario";
import TelaEstoque from "./home/TelaEstoque";
import TelaCozinha from "./home/TelaCozinha";
import TelaPedidos from "./home/TelaPedidos";
import TelaEstoquePorCategoria from "./estoque/TelaEstoquePorCategoria";
import TelaRelatoriosEstoque from "./estoque/TelaRelatoriosEstoque";
import PrivateRoute from "./auth/PrivateRoute";

function App() {
  return (
    <Router>
      <AuthProvider>
        <div className="app-container">
          <Routes>
            <Route path="/" element={<Navigate to="/login" replace />} />
            <Route path="/login" element={<TelaLogin />} />
            <Route
              path="/home"
              element={
                <PrivateRoute>
                  <TelaPrincipal />
                </PrivateRoute>
              }
            />
            <Route
              path="/usuarios"
              element={
                <PrivateRoute>
                  <TelaUsuario />
                </PrivateRoute>
              }
            />
            <Route
              path="/usuarios/cadastro"
              element={
                <PrivateRoute>
                  <TelaCadUsuario />
                </PrivateRoute>
              }
            />
            <Route
              path="/usuarios/editar/:id"
              element={
                <PrivateRoute>
                  <TelaCadUsuario />
                </PrivateRoute>
              }
            />
            <Route
              path="/estoque"
              element={
                <PrivateRoute>
                  <TelaEstoque />
                </PrivateRoute>
              }
            />
            <Route
              path="/estoque/categorias"
              element={
                <PrivateRoute>
                  <TelaEstoquePorCategoria />
                </PrivateRoute>
              }
            />
            <Route
              path="/estoque/relatorios"
              element={
                <PrivateRoute>
                  <TelaRelatoriosEstoque />
                </PrivateRoute>
              }
            />
            <Route
              path="/cozinha"
              element={
                <PrivateRoute>
                  <TelaCozinha />
                </PrivateRoute>
              }
            />
            <Route
              path="/pedidos"
              element={
                <PrivateRoute>
                  <TelaPedidos />
                </PrivateRoute>
              }
            />
          </Routes>
        </div>
      </AuthProvider>
    </Router>

  );
}

export default App;
