import { api } from './client';

export const clientesApi = {
  listar: (somenteAtivos = true) => api.get('/clientes', { somenteAtivos }),
  buscar: (id) => api.get(`/clientes/${id}`),
  criar: (dto) => api.post('/clientes', dto),
  atualizar: (id, dto) => api.put(`/clientes/${id}`, dto),
  inativar: (id) => api.del(`/clientes/${id}`),
};
