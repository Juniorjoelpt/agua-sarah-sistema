import React from 'react';
import { createPortal } from 'react-dom';

// Recibo de venda formatado pra impressora termica de 80mm (ESC/POS via
// driver comum do Windows). Fica escondido na tela e so aparece quando o
// navegador entra em modo de impressao (ver regra @media print no
// styles/index.css) - por isso o layout aqui e todo em preto e branco,
// fonte monoespacada e sem nenhum elemento visual que uma termica nao
// consiga imprimir (sombra, gradiente, icone, etc.).
//
// Renderizado via portal direto no <body>, FORA da arvore do #root: se
// ficasse dentro da tela normal (so escondido com visibility:hidden pra
// nao quebrar o "esconde tudo, mostra so o recibo" do CSS de impressao),
// a altura inteira da tela do sistema continuaria contando pra paginacao
// da impressao - e e exatamente isso que fazia o recibo sair espremido
// num canto e "estourar" pra uma segunda folha em branco.
function moeda(v) {
  return (v ?? 0).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
}

function formatarDataHora(iso) {
  if (!iso) return '';
  return new Date(iso).toLocaleString('pt-BR', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' });
}

const OCORRENCIA_LABEL = {
  AVARIA_CLIENTE: 'Avaria (cliente)',
  AVARIA_PRODUCAO: 'Avaria (produção)',
};

export default function ReciboVenda({ venda, troco }) {
  if (!venda) return null;

  const linha = { borderTop: '1px dashed #000', margin: '6px 0' };
  const base = { fontFamily: "'Courier New', monospace", fontSize: 12, color: '#000', padding: '4px 6px', lineHeight: 1.4 };

  return createPortal(
    <div id="recibo-impressao" style={base}>
      <div style={{ textAlign: 'center', marginBottom: 4 }}>
        <div style={{ fontSize: 15, fontWeight: 700 }}>ÁGUA SARAH</div>
        <div style={{ fontSize: 11 }}>Água mineral direto da fonte</div>
        <div style={{ fontSize: 11 }}>Recibo de venda</div>
      </div>

      <div style={linha} />

      <div>Venda #{venda.id}</div>
      <div>{formatarDataHora(venda.dataHora)}</div>
      {venda.cliente?.nome && <div>Cliente: {venda.cliente.nome}</div>}

      <div style={linha} />

      {venda.itens?.map((item) => (
        <div key={item.id} style={{ marginBottom: 3 }}>
          <div>{item.quantidade}x {item.produto?.nome}</div>
          <div style={{ display: 'flex', justifyContent: 'space-between' }}>
            <span>{moeda(item.precoUnitario)} un.</span>
            <span>{moeda(item.subtotal)}</span>
          </div>
        </div>
      ))}

      <div style={linha} />

      {(venda.valorAvaria > 0 || venda.valorBonificado > 0) && (
        <>
          <div style={{ display: 'flex', justifyContent: 'space-between' }}>
            <span>Subtotal</span>
            <span>{moeda(venda.valorBruto)}</span>
          </div>
          {venda.ocorrencia && venda.ocorrencia !== 'NENHUMA' && (
            <div style={{ fontSize: 11 }}>{OCORRENCIA_LABEL[venda.ocorrencia]}{venda.quantidadeAvarias ? ` - ${venda.quantidadeAvarias} galão(ões)` : ''}</div>
          )}
          {venda.valorAvaria > 0 && (
            <div style={{ display: 'flex', justifyContent: 'space-between' }}>
              <span>Desconto avaria</span>
              <span>-{moeda(venda.valorAvaria)}</span>
            </div>
          )}
          {venda.valorBonificado > 0 && (
            <div style={{ display: 'flex', justifyContent: 'space-between' }}>
              <span>Bonificação</span>
              <span>-{moeda(venda.valorBonificado)}</span>
            </div>
          )}
        </>
      )}

      <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 14, fontWeight: 700, marginTop: 4 }}>
        <span>TOTAL</span>
        <span>{moeda(venda.valorTotal)}</span>
      </div>

      <div style={linha} />

      {venda.valorRecebidoPix > 0 && (
        <div style={{ display: 'flex', justifyContent: 'space-between' }}>
          <span>PIX</span><span>{moeda(venda.valorRecebidoPix)}</span>
        </div>
      )}
      {venda.valorRecebidoEspecie > 0 && (
        <div style={{ display: 'flex', justifyContent: 'space-between' }}>
          <span>Espécie</span><span>{moeda(venda.valorRecebidoEspecie)}</span>
        </div>
      )}
      {venda.valorFiado > 0 && (
        <div style={{ display: 'flex', justifyContent: 'space-between' }}>
          <span>Fiado</span><span>{moeda(venda.valorFiado)}</span>
        </div>
      )}
      {troco > 0 && (
        <div style={{ display: 'flex', justifyContent: 'space-between' }}>
          <span>Troco</span><span>{moeda(troco)}</span>
        </div>
      )}

      {venda.observacao && (
        <>
          <div style={linha} />
          <div style={{ fontSize: 11 }}>Obs: {venda.observacao}</div>
        </>
      )}

      <div style={linha} />

      <div style={{ textAlign: 'center', fontSize: 11, marginTop: 6 }}>
        Obrigado pela preferência!
      </div>
    </div>,
    document.body
  );
}
