import { api } from '../client';

export const produtosTerceirosApi = {
  listar: (somenteAtivos = true) => api.get('/terceiros/produtos', { somenteAtivos }),
  criar: (dto) => api.post('/terceiros/produtos', dto),
  atualizar: (id, dto) => api.put(`/terceiros/produtos/${id}`, dto),
  inativar: (id) => api.del(`/terceiros/produtos/${id}`),
};
