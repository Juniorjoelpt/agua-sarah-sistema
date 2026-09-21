import { api, baixarArquivo } from './client';

export const orcamentosApi = {
  listar: (clienteId) => api.get('/orcamentos', { clienteId }),
  buscar: (id) => api.get(`/orcamentos/${id}`),
  criar: (dto) => api.post('/orcamentos', dto),
  atualizar: (id, dto) => api.put(`/orcamentos/${id}`, dto),
  atualizarStatus: (id, status) => api.patch(`/orcamentos/${id}/status`, { status }),
  excluir: (id) => api.del(`/orcamentos/${id}`),
  baixarPdf: (id) => baixarArquivo(`/orcamentos/${id}/pdf`, {}, `orcamento-${id}.pdf`),
};
