import { api } from '../client';

export const vendasTerceirosApi = {
  registrar: (dto) => api.post('/terceiros/vendas', dto),
  listarPorCaixa: (caixaId) => api.get(`/terceiros/vendas/caixa/${caixaId}`),
  listarPorCliente: (clienteId) => api.get(`/terceiros/vendas/cliente/${clienteId}`),
};
