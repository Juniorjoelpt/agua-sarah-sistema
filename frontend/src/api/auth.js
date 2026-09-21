import { api } from './client';

export const authApi = {
  login: (login, senha) => api.post('/auth/login', { login, senha }),
};
