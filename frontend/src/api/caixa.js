import { api } from './client';

export const caixaApi = {
  atual: () => api.get('/caixa/atual'),
  abrir: (dto) => api.post('/caixa/abrir', dto),
  fechar: (id) => api.post(`/caixa/${id}/fechar`),
  resumo: (id) => api.get(`/caixa/${id}/resumo`),
  historico: (inicio, fim) => api.get('/caixa', { inicio, fim }),
};
