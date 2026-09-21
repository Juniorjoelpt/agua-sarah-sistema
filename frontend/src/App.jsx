import React from 'react';
import { Routes, Route } from 'react-router-dom';
import Layout from './components/Layout';
import { ProtectedRoute, AdminRoute } from './components/ProtectedRoute';

import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import Caixa from './pages/Caixa';
import Vendas from './pages/Vendas';
import Orcamentos from './pages/Orcamentos';
import ContasReceber from './pages/ContasReceber';
import ContasPagar from './pages/ContasPagar';
import Clientes from './pages/Clientes';
import Produtos from './pages/Produtos';
import Estoque from './pages/Estoque';
import Fornecedores from './pages/Fornecedores';
import Despesas from './pages/Despesas';
import FluxoCaixa from './pages/FluxoCaixa';
import Frota from './pages/Frota';
import Relatorios from './pages/Relatorios';
import Usuarios from './pages/Usuarios';
import TerceirosLayout from './components/TerceirosLayout';
import ClientesTerceiros from './pages/terceiros/Clientes';
import ProdutosTerceiros from './pages/terceiros/Produtos';
import CaixaTerceiros from './pages/terceiros/Caixa';
import VendasTerceiros from './pages/terceiros/Vendas';
import ContasPagarTerceiros from './pages/terceiros/ContasPagar';
import ContasReceberTerceiros from './pages/terceiros/ContasReceber';
import DespesasTerceiros from './pages/terceiros/Despesas';
import OrcamentosTerceiros from './pages/terceiros/Orcamentos';
import FrotaTerceiros from './pages/terceiros/Frota';
import PainelTerceiros from './pages/terceiros/Painel';

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />

      <Route
        element={
          <ProtectedRoute>
            <Layout />
          </ProtectedRoute>
        }
      >
        <Route path="/" element={<Dashboard />} />
        <Route path="/caixa" element={<Caixa />} />
        <Route path="/vendas" element={<Vendas />} />
        <Route path="/orcamentos" element={<Orcamentos />} />
        <Route path="/contas-receber" element={<AdminRoute><ContasReceber /></AdminRoute>} />
        <Route path="/contas-pagar" element={<AdminRoute><ContasPagar /></AdminRoute>} />
        <Route path="/clientes" element={<Clientes />} />
        <Route path="/produtos" element={<Produtos />} />
        <Route path="/estoque" element={<Estoque />} />
        <Route path="/fornecedores" element={<AdminRoute><Fornecedores /></AdminRoute>} />
        <Route path="/despesas" element={<AdminRoute><Despesas /></AdminRoute>} />
        <Route path="/fluxo-caixa" element={<AdminRoute><FluxoCaixa /></AdminRoute>} />
        <Route path="/frota" element={<Frota />} />
        <Route path="/relatorios" element={<AdminRoute><Relatorios /></AdminRoute>} />
        <Route path="/usuarios" element={<AdminRoute><Usuarios /></AdminRoute>} />
        <Route path="/terceiros" element={<AdminRoute><TerceirosLayout /></AdminRoute>}>
          <Route index element={<PainelTerceiros />} />
          <Route path="caixa" element={<CaixaTerceiros />} />
          <Route path="vendas" element={<VendasTerceiros />} />
          <Route path="orcamentos" element={<OrcamentosTerceiros />} />
          <Route path="contas-receber" element={<ContasReceberTerceiros />} />
          <Route path="contas-pagar" element={<ContasPagarTerceiros />} />
          <Route path="clientes" element={<ClientesTerceiros />} />
          <Route path="produtos" element={<ProdutosTerceiros />} />
          <Route path="despesas" element={<DespesasTerceiros />} />
          <Route path="frota" element={<FrotaTerceiros />} />
        </Route>
      </Route>
    </Routes>
  );
}
