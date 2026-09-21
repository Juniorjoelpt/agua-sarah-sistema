import React, { useEffect, useRef, useState } from 'react';
import { C, DISPLAY_FONT } from '../theme';

export function PageHeader({ title, subtitle }) {
  return (
    <div className="mb-6">
      <h2 style={{ fontFamily: DISPLAY_FONT, fontSize: 22, fontWeight: 500, color: C.onDark }}>{title}</h2>
      {subtitle && <p style={{ fontSize: 13, color: C.onDarkMuted, marginTop: 2 }}>{subtitle}</p>}
    </div>
  );
}

export function SectionHeaderWithAction({ title, subtitle, actionLabel, onAction }) {
  return (
    <div className="flex items-start justify-between mb-6">
      <div>
        <h2 style={{ fontFamily: DISPLAY_FONT, fontSize: 22, fontWeight: 500, color: C.onDark }}>{title}</h2>
        {subtitle && <p style={{ fontSize: 13, color: C.onDarkMuted, marginTop: 2 }}>{subtitle}</p>}
      </div>
      {actionLabel && (
        <button onClick={onAction} className="px-4 py-2 rounded text-sm font-medium" style={{ background: C.red, color: '#fff', flexShrink: 0 }}>
          {actionLabel}
        </button>
      )}
    </div>
  );
}

export function Card({ children, style, hover = false }) {
  return (
    <div
      className={hover ? 'card-hover' : ''}
      style={{
        background: C.paper,
        border: `1px solid ${C.border}`,
        borderRadius: 12,
        boxShadow: '0 1px 2px rgba(15, 30, 51, 0.04), 0 8px 24px rgba(15, 30, 51, 0.05)',
        transition: 'transform 0.18s ease, box-shadow 0.18s ease',
        padding: 20,
        ...style,
      }}
    >
      {children}
    </div>
  );
}

export function TableCard({ columns, rows, emptyMessage = 'Nada por aqui ainda' }) {
  return (
    <Card style={{ padding: 0, overflow: 'hidden' }}>
      <table className="w-full" style={{ fontSize: 13, borderCollapse: 'collapse' }}>
        <thead>
          <tr style={{ background: C.bg }}>
            {columns.map((c) => (
              <th key={c} className="text-left px-4 py-2.5" style={{ color: C.textMuted, fontWeight: 500 }}>{c}</th>
            ))}
          </tr>
        </thead>
        <tbody>
          {rows.length === 0 && (
            <tr>
              <td colSpan={columns.length} className="px-4 py-6 text-center" style={{ color: C.textMuted }}>{emptyMessage}</td>
            </tr>
          )}
          {rows.map((r, i) => (
            <tr key={i} style={{ borderTop: `1px solid ${C.border}` }}>
              {r.map((cell, j) => <td key={j} className="px-4 py-2.5" style={{ color: C.textDark }}>{cell}</td>)}
            </tr>
          ))}
        </tbody>
      </table>
    </Card>
  );
}

export function Avatar({ name, color = C.blue }) {
  const initials = (name || '?')
    .split(' ')
    .filter(Boolean)
    .slice(0, 2)
    .map((w) => w[0])
    .join('')
    .toUpperCase();
  return (
    <div
      style={{
        width: 40, height: 40, borderRadius: '50%', flexShrink: 0,
        background: `${color}1F`, color, display: 'flex', alignItems: 'center', justifyContent: 'center',
        fontWeight: 600, fontSize: 14,
      }}
    >
      {initials}
    </div>
  );
}

const BADGE_TONES = {
  blue: { bg: C.blueLight, color: C.ink },
  amber: { bg: C.amberLight, color: '#7A4A1F' },
  red: { bg: C.redLight, color: '#7A1E17' },
  neutral: { bg: C.bg, color: C.textMuted },
};

export function Badge({ children, tone = 'blue' }) {
  const t = BADGE_TONES[tone] || BADGE_TONES.blue;
  return (
    <span style={{ fontSize: 11, fontWeight: 500, background: t.bg, color: t.color, padding: '3px 10px', borderRadius: 999, whiteSpace: 'nowrap' }}>
      {children}
    </span>
  );
}

export function EmptyState({ message }) {
  return (
    <div className="py-10 text-center" style={{ fontSize: 13, color: C.onDarkMuted }}>{message}</div>
  );
}

export function Field({ label, children }) {
  return (
    <div className="mb-1">
      <label className="block mb-1" style={{ fontSize: 12, color: C.textMuted }}>{label}</label>
      {children}
    </div>
  );
}

export function TextInput(props) {
  return <input className="w-full px-3 py-2 rounded border" style={{ borderColor: C.border, fontSize: 14, background: C.paper }} {...props} />;
}

export function Segmented({ options, value, onChange }) {
  return (
    <div className="flex gap-2 flex-wrap">
      {options.map(([id, label]) => (
        <button
          key={id}
          type="button"
          onClick={() => onChange(id)}
          className="px-3 py-1.5 rounded text-xs"
          style={{
            background: value === id ? C.blueLight : C.bg,
            color: value === id ? C.ink : C.textMuted,
            border: `1px solid ${value === id ? C.blue : C.border}`,
          }}
        >
          {label}
        </button>
      ))}
    </div>
  );
}

export function FormActions({ onCancel, onSave, saveLabel = 'Salvar', saving = false }) {
  return (
    <div className="flex gap-2 mt-4">
      <button type="button" disabled={saving} onClick={onSave} className="px-4 py-2 rounded text-sm font-medium disabled:opacity-60" style={{ background: C.red, color: '#fff' }}>
        {saving ? 'Salvando...' : saveLabel}
      </button>
      <button type="button" onClick={onCancel} className="px-4 py-2 rounded text-sm" style={{ background: C.bg, color: C.textDark }}>Cancelar</button>
    </div>
  );
}

export function FormCard({ title, style, children, onCancel, onSave, saveLabel, saving }) {
  return (
    <Card style={{ marginBottom: 16, ...style }}>
      <div style={{ fontSize: 13, fontWeight: 500, color: C.textDark, marginBottom: 14 }}>{title}</div>
      <div className="grid grid-cols-2 gap-4">{children}</div>
      <FormActions onCancel={onCancel} onSave={onSave} saveLabel={saveLabel} saving={saving} />
    </Card>
  );
}

export function ErrorBanner({ message }) {
  if (!message) return null;
  return (
    <div className="px-3 py-2 rounded mb-4" style={{ background: C.redLight, color: '#7A1E17', fontSize: 13 }}>
      {message}
    </div>
  );
}

export function Loading({ label = 'Carregando...' }) {
  return <div style={{ fontSize: 13, color: C.onDarkMuted, padding: '24px 0' }}>{label}</div>;
}

// Select com campo de busca embutido (para listas longas, como clientes).
// options: [{ value, label }]. emptyLabel é a opção "sem seleção" (ex: "Selecione" ou "Consumidor não identificado").
export function SearchableSelect({ options, value, onChange, emptyLabel = 'Selecione', placeholder = 'Buscar por nome...' }) {
  const [aberto, setAberto] = useState(false);
  const [busca, setBusca] = useState('');
  const containerRef = useRef(null);
  const inputRef = useRef(null);

  const selecionado = options.find((o) => String(o.value) === String(value));

  useEffect(() => {
    function aoClicarFora(e) {
      if (containerRef.current && !containerRef.current.contains(e.target)) {
        setAberto(false);
        setBusca('');
      }
    }
    document.addEventListener('mousedown', aoClicarFora);
    return () => document.removeEventListener('mousedown', aoClicarFora);
  }, []);

  useEffect(() => {
    if (aberto && inputRef.current) inputRef.current.focus();
  }, [aberto]);

  const termo = busca.trim().toLowerCase();
  const filtradas = termo ? options.filter((o) => o.label.toLowerCase().includes(termo)) : options;

  function escolher(opt) {
    onChange(opt ? String(opt.value) : '');
    setAberto(false);
    setBusca('');
  }

  return (
    <div ref={containerRef} style={{ position: 'relative' }}>
      <div
        onClick={() => setAberto((v) => !v)}
        className="w-full px-3 py-2 rounded border flex items-center justify-between cursor-pointer"
        style={{ borderColor: C.border, fontSize: 14, background: C.paper, color: selecionado ? C.textDark : C.textMuted }}
      >
        <span className="truncate">{selecionado ? selecionado.label : emptyLabel}</span>
        <span style={{ color: C.textMuted, fontSize: 10, marginLeft: 6, flexShrink: 0 }}>▾</span>
      </div>
      {aberto && (
        <div
          className="absolute left-0 right-0 mt-1 rounded border"
          style={{ background: C.paper, borderColor: C.border, boxShadow: '0 8px 24px rgba(15, 30, 51, 0.14)', zIndex: 30, overflow: 'hidden' }}
        >
          <input
            ref={inputRef}
            value={busca}
            onChange={(e) => setBusca(e.target.value)}
            placeholder={placeholder}
            className="w-full px-3 py-2 outline-none"
            style={{ borderBottom: `1px solid ${C.border}`, fontSize: 14 }}
          />
          <div style={{ maxHeight: 220, overflowY: 'auto' }}>
            <div
              onClick={() => escolher(null)}
              className="px-3 py-2 cursor-pointer"
              style={{ fontSize: 13, color: C.textMuted, background: !value ? C.blueLight : 'transparent' }}
            >
              {emptyLabel}
            </div>
            {filtradas.length === 0 && (
              <div className="px-3 py-2" style={{ fontSize: 13, color: C.textMuted }}>Nenhum cliente encontrado</div>
            )}
            {filtradas.map((o) => (
              <div
                key={o.value}
                onClick={() => escolher(o)}
                className="px-3 py-2 cursor-pointer truncate"
                style={{ fontSize: 13, color: C.textDark, background: String(value) === String(o.value) ? C.blueLight : 'transparent' }}
              >
                {o.label}
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
