import { api } from './client';

export const dashboardApi = {
  resumo: () => api.get('/dashboard/resumo'),
};
