import { api } from './client';

export const precosClienteApi = {
  listar: (clienteId) => api.get(`/clientes/${clienteId}/precos`),
  definirTabela: (clienteId, itens) => api.put(`/clientes/${clienteId}/precos`, itens),
};
