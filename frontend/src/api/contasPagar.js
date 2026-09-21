import { api } from './client';

export const contasPagarApi = {
  listar: (fornecedorId, somenteEmAberto) => api.get('/contas-pagar', { fornecedorId, somenteEmAberto }),
  listarVencidas: () => api.get('/contas-pagar/vencidas'),
  buscar: (id) => api.get(`/contas-pagar/${id}`),
  criar: (dto) => api.post('/contas-pagar', dto),
  registrarPagamento: (id, dto) => api.post(`/contas-pagar/${id}/pagamentos`, dto),
  excluir: (id) => api.del(`/contas-pagar/${id}`),
};
