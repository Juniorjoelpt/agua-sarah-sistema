import { api } from './client';

export const estoqueApi = {
  listarInsumos: () => api.get('/estoque/insumos'),
  criarInsumo: (dto) => api.post('/estoque/insumos', dto),
  registrarMovimentacao: (dto) => api.post('/estoque/movimentacoes', dto),
  historico: (insumoId) => api.get(`/estoque/insumos/${insumoId}/historico`),
};
