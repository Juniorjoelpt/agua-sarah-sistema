import { api, baixarArquivo } from './client';

export const caixaApi = {
  atual: () => api.get('/caixa/atual'),
  abrir: (dto) => api.post('/caixa/abrir', dto),
  fechar: (id) => api.post(`/caixa/${id}/fechar`),
  resumo: (id) => api.get(`/caixa/${id}/resumo`),
  resumos: (inicio, fim) => api.get('/caixa/resumos', { inicio, fim }),
  baixarPdf: (id, data) => baixarArquivo(`/caixa/${id}/pdf`, {}, `caixa-${id}_${data}.pdf`),
  detalhe: (id) => api.get(`/caixa/${id}/detalhe`),
  historico: (inicio, fim) => api.get('/caixa', { inicio, fim }),
};
