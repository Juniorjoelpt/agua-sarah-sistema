import { api } from './client';

export const fornecedoresApi = {
  listar: (somenteAtivos = true) => api.get('/fornecedores', { somenteAtivos }),
  buscar: (id) => api.get(`/fornecedores/${id}`),
  criar: (dto) => api.post('/fornecedores', dto),
  atualizar: (id, dto) => api.put(`/fornecedores/${id}`, dto),
  inativar: (id) => api.del(`/fornecedores/${id}`),
  registrarCompra: (id, dto) => api.post(`/fornecedores/${id}/compras`, dto),
  listarCompras: (id) => api.get(`/fornecedores/${id}/compras`),
  listarFornecimentos: (id) => api.get(`/fornecedores/${id}/fornecimentos`),
};
