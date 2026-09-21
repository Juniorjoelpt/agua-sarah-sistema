import { api } from './client';

export const produtosApi = {
  listar: (somenteAtivos = true) => api.get('/produtos', { somenteAtivos }),
  criar: (dto) => api.post('/produtos', dto),
  atualizar: (id, dto) => api.put(`/produtos/${id}`, dto),
  inativar: (id) => api.del(`/produtos/${id}`),
};
