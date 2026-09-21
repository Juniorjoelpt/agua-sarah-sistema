import { api, baixarArquivo } from '../client';

export const frotaTerceirosApi = {
  listarCaminhoes: () => api.get('/terceiros/frota/caminhoes'),
  criarCaminhao: (dto) => api.post('/terceiros/frota/caminhoes', dto),
  atualizarCaminhao: (id, dto) => api.put(`/terceiros/frota/caminhoes/${id}`, dto),
  inativarCaminhao: (id) => api.del(`/terceiros/frota/caminhoes/${id}`),
  abrirCarregamento: (dto) => api.post('/terceiros/frota/carregamentos', dto),
  historicoCarregamentos: (caminhaoId) => api.get(`/terceiros/frota/caminhoes/${caminhaoId}/carregamentos`),
  registrarDespesaCarregamento: (carregamentoId, dto) => api.post(`/terceiros/frota/carregamentos/${carregamentoId}/despesas`, dto),
  listarDespesasCarregamento: (carregamentoId) => api.get(`/terceiros/frota/carregamentos/${carregamentoId}/despesas`),
  registrarPrestacaoContas: (dto) => api.post('/terceiros/frota/prestacoes-contas', dto),
  listarPrestacoes: (inicio, fim, caminhaoId) => api.get('/terceiros/frota/prestacoes-contas', { inicio, fim, caminhaoId }),
  baixarPdfPrestacoes: (inicio, fim, caminhaoId) =>
    baixarArquivo('/terceiros/frota/prestacoes-contas/pdf', { inicio, fim, caminhaoId }, 'prestacoes-frota-terceiros.pdf'),
};
