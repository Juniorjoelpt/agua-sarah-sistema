import { api } from '../client';

export const caixaTerceirosApi = {
  atual: () => api.get('/terceiros/caixa/atual'),
  abrir: (dto) => api.post('/terceiros/caixa/abrir', dto),
  fechar: (id) => api.post(`/terceiros/caixa/${id}/fechar`),
  resumo: (id) => api.get(`/terceiros/caixa/${id}/resumo`),
};
