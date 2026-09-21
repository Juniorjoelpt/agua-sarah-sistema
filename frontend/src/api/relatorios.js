import { api, baixarArquivo } from './client';

export const relatoriosApi = {
  despesas: (inicio, fim) => api.get('/relatorios/despesas', { inicio, fim }),
  estoque: () => api.get('/relatorios/estoque'),
  envase: (inicio, fim) => api.get('/relatorios/envase', { inicio, fim }),
  caixa: (inicio, fim) => api.get('/relatorios/caixa', { inicio, fim }),
  fluxoCaixa: (inicio, fim) => api.get('/relatorios/fluxo-caixa', { inicio, fim }),

  baixarPdfCaixa: (inicio, fim) => baixarArquivo('/relatorios/caixa/pdf', { inicio, fim }, `historico-caixa_${inicio}_a_${fim}.pdf`),
  baixarPdfDespesas: (inicio, fim) => baixarArquivo('/relatorios/despesas/pdf', { inicio, fim }, `despesas_${inicio}_a_${fim}.pdf`),
  baixarPdfEstoque: () => baixarArquivo('/relatorios/estoque/pdf', {}, `estoque_${new Date().toISOString().slice(0, 10)}.pdf`),
  baixarPdfEnvase: (inicio, fim) => baixarArquivo('/relatorios/envase/pdf', { inicio, fim }, `envase-agua_${inicio}_a_${fim}.pdf`),
  baixarPdfFluxoCaixa: (inicio, fim) => baixarArquivo('/relatorios/fluxo-caixa/pdf', { inicio, fim }, `fluxo-caixa_${inicio}_a_${fim}.pdf`),
};
