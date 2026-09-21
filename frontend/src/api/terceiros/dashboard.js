import { api } from '../client';

export const dashboardTerceirosApi = {
  resumo: () => api.get('/terceiros/dashboard/resumo'),
};
