import React from 'react';
import { NavLink, Outlet, useLocation } from 'react-router-dom';
import { motion } from 'framer-motion';
import {
  LayoutDashboard, Wallet, ShoppingCart, FileText, HandCoins, CalendarClock,
  Users, Package, Receipt, Truck,
} from 'lucide-react';
import { C } from '../theme';
import { PageHeader } from './ui';

const SUBNAV = [
  { to: '/terceiros', label: 'Painel', icon: LayoutDashboard, end: true },
  { to: '/terceiros/caixa', label: 'Caixa', icon: Wallet },
  { to: '/terceiros/vendas', label: 'Vendas', icon: ShoppingCart },
  { to: '/terceiros/orcamentos', label: 'Orçamentos', icon: FileText },
  { to: '/terceiros/contas-receber', label: 'Contas a Receber', icon: HandCoins },
  { to: '/terceiros/contas-pagar', label: 'Contas a Pagar', icon: CalendarClock },
  { to: '/terceiros/clientes', label: 'Clientes', icon: Users },
  { to: '/terceiros/produtos', label: 'Produtos', icon: Package },
  { to: '/terceiros/despesas', label: 'Despesas', icon: Receipt },
  { to: '/terceiros/frota', label: 'Frota', icon: Truck },
];

export default function TerceirosLayout() {
  const location = useLocation();

  function isActivePath(item) {
    return item.end ? location.pathname === item.to : location.pathname.startsWith(item.to);
  }

  return (
    <div>
      <PageHeader title="Compra de Terceiros" subtitle="Empresas com frota própria que compram galões prontos para revenda" />

      <div
        className="flex gap-1 mb-6 flex-wrap"
        style={{ background: 'rgba(255,255,255,0.08)', borderRadius: 12, padding: 6 }}
      >
        {SUBNAV.map((item) => {
          const Icon = item.icon;
          const active = isActivePath(item);
          return (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.end}
              className="flex items-center gap-1.5 px-3 py-2 rounded-lg"
              style={{ position: 'relative', fontSize: 13, textDecoration: 'none', color: active ? C.ink : 'rgba(255,255,255,0.8)' }}
            >
              {active && (
                <motion.div
                  layoutId="terceiros-subnav-active"
                  transition={{ type: 'spring', stiffness: 420, damping: 34 }}
                  style={{ position: 'absolute', inset: 0, borderRadius: 8, background: '#fff' }}
                />
              )}
              <Icon size={14} style={{ position: 'relative', color: active ? C.red : 'currentColor' }} />
              <span style={{ position: 'relative', fontWeight: active ? 600 : 400 }}>{item.label}</span>
            </NavLink>
          );
        })}
      </div>

      <Outlet />
    </div>
  );
}
