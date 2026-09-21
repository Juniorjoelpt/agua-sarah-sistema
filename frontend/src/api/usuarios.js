import { api } from './client';

export const usuariosApi = {
  listar: () => api.get('/usuarios'),
  criar: (dto) => api.post('/usuarios', dto),
  atualizar: (id, dto) => api.put(`/usuarios/${id}`, dto),
  inativar: (id) => api.del(`/usuarios/${id}`),
};
