import { api } from '../client';

export const contasReceberTerceirosApi = {
  listar: (clienteId, somenteEmAberto) => api.get('/terceiros/contas-receber', { clienteId, somenteEmAberto }),
  criar: (dto) => api.post('/terceiros/contas-receber', dto),
  registrarPagamento: (id, dto) => api.post(`/terceiros/contas-receber/${id}/pagamentos`, dto),
};
