package com.aguasarah.relatorio;

// "Envase" agora conta tanto os galoes vendidos direto no PDV quanto os
// carregados nos caminhoes (os dois saem da fonte) - por isso o total vem
// junto com o detalhamento de cada origem. As avarias tambem juntam as duas
// origens: avaria em venda (cliente/producao) e avaria em carregamento de caminhao.
public record RelatorioEnvaseDTO(
        int totalGaloesEnvasados,          // totalGaloesVendidosPdv + totalGaloesCarregadosCaminhoes
        int totalGaloesVendidosPdv,
        int totalGaloesCarregadosCaminhoes,
        int totalAvarias,                  // soma de todas as avarias abaixo
        int totalGaloesBonificados,        // avaria de producao em vendas (com bonificacao)
        int totalGaloesAvariaClientePdv,   // avaria de cliente em vendas (sem bonificacao)
        int totalGaloesAvariaCaminhoes     // avaria registrada na prestacao de contas dos caminhoes
) {}
