import { api } from './client';

export const fluxoCaixaApi = {
  listarContas: (somenteAtivas = true) => api.get('/fluxo-caixa/contas', { somenteAtivas }),
  criarConta: (dto) => api.post('/fluxo-caixa/contas', dto),
  atualizarConta: (id, dto) => api.put(`/fluxo-caixa/contas/${id}`, dto),
  inativarConta: (id) => api.del(`/fluxo-caixa/contas/${id}`),
  saldoConta: (id) => api.get(`/fluxo-caixa/contas/${id}/saldo`),
  listarLancamentos: (contaId, inicio, fim) => api.get(`/fluxo-caixa/contas/${contaId}/lancamentos`, { inicio, fim }),
  registrarLancamento: (dto) => api.post('/fluxo-caixa/lancamentos', dto),
  excluirLancamento: (id) => api.del(`/fluxo-caixa/lancamentos/${id}`),
};
