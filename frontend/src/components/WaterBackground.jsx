import React, { useMemo } from 'react';
import { motion } from 'framer-motion';

// Assinatura visual do sistema: bolhas subindo, no mesmo espirito do
// padrao de fundo da Araca Beach (la, o padrao vinha da raquete; aqui,
// da propria agua). Decorativo, aria-hidden, respeita reduced-motion via CSS.
export default function WaterBackground({ count = 18, opacity = 0.12 }) {
  const bolhas = useMemo(() => {
    return Array.from({ length: count }).map((_, i) => ({
      id: i,
      left: Math.random() * 100,
      size: 6 + Math.random() * 18,
      duration: 10 + Math.random() * 14,
      delay: Math.random() * 10,
    }));
  }, [count]);

  return (
    <div
      aria-hidden="true"
      className="water-bg"
      style={{ position: 'absolute', inset: 0, overflow: 'hidden', pointerEvents: 'none' }}
    >
      {bolhas.map((b) => (
        <motion.span
          key={b.id}
          initial={{ y: '110%', opacity: 0 }}
          animate={{ y: '-20%', opacity: [0, opacity, opacity, 0] }}
          transition={{ duration: b.duration, delay: b.delay, repeat: Infinity, ease: 'linear' }}
          style={{
            position: 'absolute',
            left: `${b.left}%`,
            width: b.size,
            height: b.size,
            borderRadius: '9999px',
            background: 'rgba(255,255,255,0.9)',
          }}
        />
      ))}
    </div>
  );
}
