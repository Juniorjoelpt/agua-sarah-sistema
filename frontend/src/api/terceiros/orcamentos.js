import { api, baixarArquivo } from '../client';

export const orcamentosTerceirosApi = {
  listar: (clienteId) => api.get('/terceiros/orcamentos', { clienteId }),
  buscar: (id) => api.get(`/terceiros/orcamentos/${id}`),
  criar: (dto) => api.post('/terceiros/orcamentos', dto),
  atualizar: (id, dto) => api.put(`/terceiros/orcamentos/${id}`, dto),
  atualizarStatus: (id, status) => api.patch(`/terceiros/orcamentos/${id}/status`, { status }),
  excluir: (id) => api.del(`/terceiros/orcamentos/${id}`),
  baixarPdf: (id) => baixarArquivo(`/terceiros/orcamentos/${id}/pdf`, {}, `orcamento-terceiro-${id}.pdf`),
};
