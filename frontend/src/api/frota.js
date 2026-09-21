import { api, baixarArquivo } from './client';

export const frotaApi = {
  listarCaminhoes: () => api.get('/frota/caminhoes'),
  criarCaminhao: (dto) => api.post('/frota/caminhoes', dto),
  atualizarCaminhao: (id, dto) => api.put(`/frota/caminhoes/${id}`, dto),
  inativarCaminhao: (id) => api.del(`/frota/caminhoes/${id}`),
  clientesRotaFixa: (caminhaoId) => api.get(`/frota/caminhoes/${caminhaoId}/clientes-rota-fixa`),
  abrirCarregamento: (dto) => api.post('/frota/carregamentos', dto),
  historicoCarregamentos: (caminhaoId) => api.get(`/frota/caminhoes/${caminhaoId}/carregamentos`),
  registrarPrestacaoContas: (dto) => api.post('/frota/prestacoes-contas', dto),
  prestacaoPorCarregamento: (carregamentoId) => api.get(`/frota/carregamentos/${carregamentoId}/prestacao-contas`),
  registrarDespesaCarregamento: (carregamentoId, dto) => api.post(`/frota/carregamentos/${carregamentoId}/despesas`, dto),
  listarDespesasCarregamento: (carregamentoId) => api.get(`/frota/carregamentos/${carregamentoId}/despesas`),
  listarPrestacoes: (inicio, fim, caminhaoId) => api.get('/frota/prestacoes-contas', { inicio, fim, caminhaoId }),
  baixarPdfPrestacoes: (inicio, fim, caminhaoId) =>
    baixarArquivo('/frota/prestacoes-contas/pdf', { inicio, fim, caminhaoId }, 'prestacoes-frota.pdf'),
};
