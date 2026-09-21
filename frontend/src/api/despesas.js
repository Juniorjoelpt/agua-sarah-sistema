import { api } from './client';

export const despesasApi = {
  listar: (inicio, fim) => api.get('/despesas', { inicio, fim }),
  // POST /api/despesas aceita caixaId como query param (nao no corpo)
  criar: (dto, caixaId) => {
    const query = caixaId ? `?caixaId=${caixaId}` : '';
    return api.post(`/despesas${query}`, dto);
  },
};
