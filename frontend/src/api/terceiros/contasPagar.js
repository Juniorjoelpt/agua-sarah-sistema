import { api } from '../client';

export const contasPagarTerceirosApi = {
  listar: (somenteEmAberto) => api.get('/terceiros/contas-pagar', { somenteEmAberto }),
  listarVencidas: () => api.get('/terceiros/contas-pagar/vencidas'),
  criar: (dto) => api.post('/terceiros/contas-pagar', dto),
  registrarPagamento: (id, dto) => api.post(`/terceiros/contas-pagar/${id}/pagamentos`, dto),
  excluir: (id) => api.del(`/terceiros/contas-pagar/${id}`),
};
