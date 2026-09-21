import React, { useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { X } from 'lucide-react';
import { C, DISPLAY_FONT } from '../theme';

// Modal reutilizavel, no mesmo padrao visual usado nos formularios
// "Novo Agendamento" / detalhes de comanda da Araca Beach: fundo escurecido,
// painel branco central com entrada suave (fade + leve escala).
export default function Modal({ open, title, onClose, children, maxWidth = 480 }) {
  useEffect(() => {
    function onKeyDown(e) {
      if (e.key === 'Escape') onClose();
    }
    if (open) document.addEventListener('keydown', onKeyDown);
    return () => document.removeEventListener('keydown', onKeyDown);
  }, [open, onClose]);

  return (
    <AnimatePresence>
      {open && (
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          exit={{ opacity: 0 }}
          transition={{ duration: 0.15 }}
          onClick={onClose}
          style={{
            position: 'fixed', inset: 0, zIndex: 100,
            background: 'rgba(11, 46, 99, 0.55)', backdropFilter: 'blur(2px)',
            display: 'flex', alignItems: 'center', justifyContent: 'center', padding: 24,
          }}
        >
          <motion.div
            initial={{ opacity: 0, y: 24, scale: 0.97 }}
            animate={{ opacity: 1, y: 0, scale: 1 }}
            exit={{ opacity: 0, y: 12, scale: 0.98 }}
            transition={{ duration: 0.2, ease: 'easeOut' }}
            onClick={(e) => e.stopPropagation()}
            style={{
              background: C.paper, borderRadius: 16, width: '100%', maxWidth,
              maxHeight: '85vh', overflowY: 'auto', boxShadow: '0 24px 60px rgba(11,46,99,0.35)',
            }}
          >
            <div className="flex items-center justify-between px-6 py-4" style={{ borderBottom: `1px solid ${C.border}`, position: 'sticky', top: 0, background: C.paper, borderRadius: '16px 16px 0 0' }}>
              <h3 style={{ fontFamily: DISPLAY_FONT, fontSize: 18, fontWeight: 600, color: C.textDark }}>{title}</h3>
              <button onClick={onClose} aria-label="Fechar" style={{ color: C.textMuted, padding: 4 }}>
                <X size={18} />
              </button>
            </div>
            <div className="p-6">{children}</div>
          </motion.div>
        </motion.div>
      )}
    </AnimatePresence>
  );
}
