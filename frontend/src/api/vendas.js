import { api } from './client';

export const vendasApi = {
  registrar: (dto) => api.post('/vendas', dto),
  listarPorCaixa: (caixaId) => api.get(`/vendas/caixa/${caixaId}`),
  listarPorCliente: (clienteId) => api.get(`/vendas/cliente/${clienteId}`),
};
