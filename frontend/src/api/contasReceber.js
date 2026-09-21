import { api } from './client';

export const contasReceberApi = {
  listar: (clienteId, somenteEmAberto) => api.get('/contas-receber', { clienteId, somenteEmAberto }),
  buscar: (id) => api.get(`/contas-receber/${id}`),
  criar: (dto) => api.post('/contas-receber', dto),
  registrarPagamento: (id, dto) => api.post(`/contas-receber/${id}/pagamentos`, dto),
};
