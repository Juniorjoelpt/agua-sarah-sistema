import React, { createContext, useContext, useState, useCallback } from 'react';
import { authApi } from '../api/auth';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [usuario, setUsuario] = useState(() => {
    const raw = localStorage.getItem('usuario');
    return raw ? JSON.parse(raw) : null;
  });
  const [carregando, setCarregando] = useState(false);
  const [erro, setErro] = useState(null);

  const login = useCallback(async (loginValue, senha) => {
    setCarregando(true);
    setErro(null);
    try {
      const resposta = await authApi.login(loginValue, senha);
      localStorage.setItem('token', resposta.token);
      localStorage.setItem('usuario', JSON.stringify(resposta.usuario));
      setUsuario(resposta.usuario);
      return true;
    } catch (e) {
      setErro(e.message || 'Não foi possível entrar');
      return false;
    } finally {
      setCarregando(false);
    }
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem('token');
    localStorage.removeItem('usuario');
    setUsuario(null);
  }, []);

  const isAdmin = usuario?.perfil === 'ADMIN';

  return (
    <AuthContext.Provider value={{ usuario, login, logout, carregando, erro, isAdmin }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth deve ser usado dentro de AuthProvider');
  return ctx;
}
