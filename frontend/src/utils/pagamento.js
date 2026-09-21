// Determina o rotulo de pagamento (PIX, Especie, Fiado ou combinacoes) a
// partir dos valores recebidos na venda - o pagamento pode ser dividido
// entre os tres.
export function rotuloPagamento(venda) {
  const especie = venda.valorRecebidoEspecie || 0;
  const pix = venda.valorRecebidoPix || 0;
  const fiado = venda.valorFiado || 0;
  const moeda = (v) => v.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });

  const partes = [];
  if (pix > 0) partes.push(`PIX ${moeda(pix)}`);
  if (especie > 0) partes.push(`Espécie ${moeda(especie)}`);
  if (fiado > 0) partes.push(`Fiado ${moeda(fiado)}`);

  if (partes.length > 1) return partes.join(' + ');
  if (fiado > 0) return 'Fiado';
  return pix > 0 ? 'PIX' : 'Espécie';
}
