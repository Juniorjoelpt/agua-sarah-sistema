import { api } from '../client';

export const clientesTerceirosApi = {
  listar: (somenteAtivos = true) => api.get('/terceiros/clientes', { somenteAtivos }),
  buscar: (id) => api.get(`/terceiros/clientes/${id}`),
  criar: (dto) => api.post('/terceiros/clientes', dto),
  atualizar: (id, dto) => api.put(`/terceiros/clientes/${id}`, dto),
  inativar: (id) => api.del(`/terceiros/clientes/${id}`),
};
