import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { C, BODY_FONT } from '../theme';
import { useAuth } from '../context/AuthContext';
import { ErrorBanner } from '../components/ui';
import WaterBackground from '../components/WaterBackground';

export default function Login() {
  const { login, carregando, erro } = useAuth();
  const navigate = useNavigate();
  const [loginValue, setLoginValue] = useState('');
  const [senha, setSenha] = useState('');

  async function handleSubmit(e) {
    e.preventDefault();
    const ok = await login(loginValue, senha);
    if (ok) navigate('/');
  }

  return (
    <div className="ocean-gradient" style={{ position: 'relative', minHeight: '100vh', overflow: 'hidden', fontFamily: BODY_FONT }}>
      <WaterBackground count={22} opacity={0.14} />

      <div className="flex items-center justify-center" style={{ position: 'relative', minHeight: '100vh', padding: 24 }}>
        <div className="w-full" style={{ maxWidth: 360 }}>
          <motion.div
            initial={{ opacity: 0, y: -16 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5, ease: 'easeOut' }}
            className="flex flex-col items-center mb-8"
          >
            <motion.img
              src="/logo-agua-sarah-completa.png"
              alt="Água Sarah"
              animate={{ y: [0, -6, 0] }}
              transition={{ duration: 3.2, repeat: Infinity, ease: 'easeInOut' }}
              style={{ width: 220, marginBottom: 10 }}
            />
            <p style={{ color: 'rgba(255,255,255,0.7)', fontSize: 13, marginTop: 4 }}>Sistema de gestão</p>
          </motion.div>

          <motion.form
            onSubmit={handleSubmit}
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5, delay: 0.15, ease: 'easeOut' }}
            className="rounded-lg p-6"
            style={{ background: C.paper, boxShadow: '0 20px 60px rgba(11, 46, 99, 0.35)' }}
          >
            <ErrorBanner message={erro} />
            <label className="block mb-1" style={{ fontSize: 13, color: C.textMuted }}>Usuário</label>
            <input
              className="w-full mb-4 mt-1 px-3 py-2 rounded border transition-colors"
              style={{ borderColor: C.border, fontSize: 14 }}
              value={loginValue}
              onChange={(e) => setLoginValue(e.target.value)}
              autoFocus
            />
            <label className="block mb-1" style={{ fontSize: 13, color: C.textMuted }}>Senha</label>
            <input
              type="password"
              className="w-full mb-5 mt-1 px-3 py-2 rounded border transition-colors"
              style={{ borderColor: C.border, fontSize: 14 }}
              value={senha}
              onChange={(e) => setSenha(e.target.value)}
            />
            <motion.button
              whileHover={{ scale: 1.01 }}
              whileTap={{ scale: 0.98 }}
              type="submit"
              disabled={carregando}
              className="w-full py-2 rounded font-medium disabled:opacity-60"
              style={{ background: C.ocean, color: '#fff', fontSize: 14 }}
            >
              {carregando ? 'Entrando...' : 'Entrar'}
            </motion.button>
          </motion.form>
        </div>
      </div>
    </div>
  );
}
