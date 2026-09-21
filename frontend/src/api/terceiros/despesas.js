import { api } from '../client';

export const despesasTerceirosApi = {
  listar: (inicio, fim) => api.get('/terceiros/despesas', { inicio, fim }),
  criar: (dto) => api.post('/terceiros/despesas', dto),
};
