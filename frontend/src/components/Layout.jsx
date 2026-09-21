import React from 'react';
import { Outlet, useLocation } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import Sidebar from './Sidebar';
import { BODY_FONT } from '../theme';

export default function Layout() {
  const location = useLocation();

  return (
    <div className="flex w-full ocean-gradient" style={{ minHeight: '100vh', fontFamily: BODY_FONT }}>
      <Sidebar />
      <main className="flex-1 p-8" style={{ position: 'relative', overflow: 'hidden' }}>
        {/* marca d'agua discreta da logo completa, no mesmo espirito da Araca Beach */}
        <img
          src="/logo-agua-sarah-branca.png"
          alt=""
          aria-hidden="true"
          style={{
            position: 'absolute', right: -30, bottom: -20, width: 480, height: 'auto',
            opacity: 0.05, pointerEvents: 'none', objectFit: 'contain',
          }}
        />
        <AnimatePresence mode="wait">
          <motion.div
            key={location.pathname}
            initial={{ opacity: 0, y: 8 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -8 }}
            transition={{ duration: 0.22, ease: 'easeOut' }}
            style={{ position: 'relative' }}
          >
            <Outlet />
          </motion.div>
        </AnimatePresence>
      </main>
    </div>
  );
}
