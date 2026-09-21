import React from 'react';
import { NavLink, useLocation, useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import {
  LayoutDashboard, Wallet, HandCoins, CalendarClock, ShoppingCart, FileText, Users, Package,
  Archive, Boxes, Receipt, Truck, FileBarChart, Building2, LogOut, UserCog, Landmark,
} from 'lucide-react';
import { C } from '../theme';
import { useAuth } from '../context/AuthContext';
import WaterBackground from './WaterBackground';

const NAV = [
  { to: '/', label: 'Painel', icon: LayoutDashboard, end: true },
  { to: '/caixa', label: 'Caixa', icon: Wallet },
  { to: '/vendas', label: 'Vendas', icon: ShoppingCart },
  { to: '/orcamentos', label: 'Orçamentos', icon: FileText },
  { to: '/clientes', label: 'Clientes', icon: Users },
  { to: '/produtos', label: 'Produtos', icon: Package },
  { to: '/estoque', label: 'Estoque', icon: Archive },
  { to: '/frota', label: 'Frota', icon: Truck },
];

// visiveis so pra perfil ADMIN
const NAV_ADMIN = [
  { to: '/contas-receber', label: 'Contas a Receber', icon: HandCoins },
  { to: '/contas-pagar', label: 'Contas a Pagar', icon: CalendarClock },
  { to: '/fornecedores', label: 'Fornecedores', icon: Boxes },
  { to: '/despesas', label: 'Despesas', icon: Receipt },
  { to: '/fluxo-caixa', label: 'Fluxo de Caixa', icon: Landmark },
  { to: '/relatorios', label: 'Relatórios', icon: FileBarChart },
  { to: '/usuarios', label: 'Usuários', icon: UserCog },
  { to: '/terceiros', label: 'Compra de terceiros', icon: Building2 },
];

export default function Sidebar() {
  const { usuario, logout, isAdmin } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const items = isAdmin ? [...NAV, ...NAV_ADMIN] : NAV;

  function sair() {
    logout();
    navigate('/login');
  }

  function isActivePath(item) {
    return item.end ? location.pathname === item.to : location.pathname.startsWith(item.to);
  }

  const perfilLabel = isAdmin ? 'Administrador' : 'Operador';
  // evita mostrar "Administrador · Administrador" quando o nome cadastrado do usuario
  // e literalmente igual ao rotulo do perfil (caso do admin padrao criado pelo seed)
  const linhaUsuario = usuario?.nome && usuario.nome !== perfilLabel
    ? `${usuario.nome} · ${perfilLabel}`
    : perfilLabel;

  return (
    <aside
      className="ocean-gradient"
      style={{ position: 'sticky', top: 0, width: 224, height: '100vh', color: '#fff', display: 'flex', flexDirection: 'column', overflow: 'hidden', flexShrink: 0 }}
    >
      <WaterBackground count={10} opacity={0.08} />

      <div className="flex items-center justify-center px-5 py-6" style={{ position: 'relative', borderBottom: '1px solid rgba(255,255,255,0.12)' }}>
        <img
          src="/logo-agua-sarah-branca.png"
          alt="Água Sarah"
          style={{ width: '100%', maxWidth: 170 }}
        />
      </div>

      <nav
        className="flex-1 py-3 px-3 sidebar-nav-scroll"
        style={{ position: 'relative', display: 'flex', flexDirection: 'column', gap: 2, overflowY: 'auto', minHeight: 0 }}
      >
        {items.map((item) => {
          const Icon = item.icon;
          const active = isActivePath(item);
          return (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.end}
              className="flex items-center gap-3 px-3 py-2.5 rounded-xl hover:bg-white/10 transition-colors"
              style={{
                position: 'relative',
                color: active ? C.ink : 'rgba(255,255,255,0.75)',
                fontSize: 14,
                fontWeight: active ? 600 : 400,
                textDecoration: 'none',
                transition: 'color 0.15s ease',
              }}
            >
              {active && (
                <motion.div
                  layoutId="sidebar-active"
                  transition={{ type: 'spring', stiffness: 420, damping: 34 }}
                  style={{
                    position: 'absolute', inset: 0, borderRadius: 12,
                    background: '#fff',
                    boxShadow: '0 6px 16px rgba(11, 46, 99, 0.25)',
                  }}
                />
              )}
              <Icon size={17} style={{ position: 'relative', color: active ? C.red : 'currentColor' }} />
              <span style={{ position: 'relative' }}>{item.label}</span>
            </NavLink>
          );
        })}
      </nav>

      <div style={{ position: 'relative', borderTop: '1px solid rgba(255,255,255,0.12)' }}>
        <div className="px-5 pt-4" style={{ fontSize: 11, color: 'rgba(255,255,255,0.65)' }}>{linhaUsuario}</div>
        <button
          onClick={sair}
          className="flex items-center gap-2 px-5 pt-2 pb-4 text-left w-full"
          style={{ color: 'rgba(255,255,255,0.7)', fontSize: 13 }}
        >
          <LogOut size={15} /> Sair
        </button>
      </div>
    </aside>
  );
}
