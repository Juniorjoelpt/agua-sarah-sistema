import React, { useEffect, useState } from 'react';
import { PageHeader, Card, Field, TextInput, Segmented, ErrorBanner, Loading, Badge, SearchableSelect } from '../components/ui';
import ReciboVenda from '../components/ReciboVenda';
import { C, DISPLAY_FONT } from '../theme';
import { clientesApi } from '../api/clientes';
import { produtosApi } from '../api/produtos';
import { precosClienteApi } from '../api/precosCliente';
import { vendasApi } from '../api/vendas';
import { caixaApi } from '../api/caixa';
import { rotuloPagamento } from '../utils/pagamento';
import { AlertTriangle, Clock } from 'lucide-react';

export default function Vendas() {
  const [clientes, setClientes] = useState([]);
  const [produtos, setProdutos] = useState([]);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState(null);
  const [sucesso, setSucesso] = useState(null);
  const [salvando, setSalvando] = useState(false);

  const [clienteId, setClienteId] = useState('');
  const [precosCliente, setPrecosCliente] = useState({}); // { [produtoId]: precoPersonalizado }
  const [itens, setItens] = useState([{ produtoId: '', quantidade: 1 }]);
  const [modoPagamento, setModoPagamento] = useState('PIX'); // 'PIX' | 'ESPECIE' | 'FIADO' | 'DIVIDIDO'
  const [valorEspecieInput, setValorEspecieInput] = useState('');
  const [valorPixInput, setValorPixInput] = useState('');
  const [valorFiadoInput, setValorFiadoInput] = useState('');
  const [dinheiroEntregue, setDinheiroEntregue] = useState('');
  const [ocorrencia, setOcorrencia] = useState('NENHUMA');
  const [quantidadeAvarias, setQuantidadeAvarias] = useState('1');
  const [quantidadeBonificados, setQuantidadeBonificados] = useState('1');
  const [observacao, setObservacao] = useState('');

  const [vendasHoje, setVendasHoje] = useState([]);
  const [carregandoVendasHoje, setCarregandoVendasHoje] = useState(true);

  // ultima venda registrada, guardada so pra alimentar o recibo de impressao
  // (a tela em si ja limpa o formulario pra proxima venda)
  const [vendaParaImprimir, setVendaParaImprimir] = useState(null);
  const [trocoParaImprimir, setTrocoParaImprimir] = useState(0);
  const [impressaoAutomatica, setImpressaoAutomatica] = useState(() => {
    try { return localStorage.getItem('aguaSarah.impressaoAutomatica') !== 'false'; } catch { return true; }
  });

  useEffect(() => {
    async function carregar() {
      setCarregando(true);
      try {
        const [c, p] = await Promise.all([clientesApi.listar(), produtosApi.listar()]);
        setClientes(c);
        setProdutos(p);
      } catch (e) {
        setErro(e.message);
      }
      setCarregando(false);
    }
    carregar();
    carregarVendasHoje();
  }, []);

  async function carregarVendasHoje() {
    setCarregandoVendasHoje(true);
    try {
      const caixa = await caixaApi.atual();
      setVendasHoje(caixa?.id ? await vendasApi.listarPorCaixa(caixa.id) : []);
    } catch {
      setVendasHoje([]); // sem caixa aberto - nao e erro, so nao ha vendas pra mostrar ainda
    } finally {
      setCarregandoVendasHoje(false);
    }
  }

  useEffect(() => {
    if (!clienteId) {
      setPrecosCliente({});
      return;
    }
    precosClienteApi.listar(clienteId)
      .then((lista) => {
        const mapa = {};
        lista.forEach((p) => { mapa[p.produto.id] = p.preco; });
        setPrecosCliente(mapa);
      })
      .catch(() => setPrecosCliente({}));
  }, [clienteId]);

  // dispara a impressao assim que uma nova venda fica disponivel pro recibo -
  // o pequeno atraso garante que o React ja atualizou o #recibo-impressao
  // no DOM antes do navegador montar a pagina de impressao
  useEffect(() => {
    if (!vendaParaImprimir || !impressaoAutomatica) return;
    const t = setTimeout(() => window.print(), 150);
    return () => clearTimeout(t);
  }, [vendaParaImprimir, impressaoAutomatica]);

  function alternarImpressaoAutomatica() {
    setImpressaoAutomatica((prev) => {
      const novo = !prev;
      try { localStorage.setItem('aguaSarah.impressaoAutomatica', String(novo)); } catch { /* ignora */ }
      return novo;
    });
  }

  function imprimirNovamente() {
    if (vendaParaImprimir) window.print();
  }

  function atualizarItem(index, campo, valor) {
    setItens((prev) => prev.map((it, i) => (i === index ? { ...it, [campo]: valor } : it)));
  }

  function adicionarItem() {
    setItens((prev) => [...prev, { produtoId: '', quantidade: 1 }]);
  }

  function removerItem(index) {
    setItens((prev) => prev.filter((_, i) => i !== index));
  }

  function produtoPorId(id) {
    return produtos.find((p) => String(p.id) === String(id));
  }

  // preco personalizado desse cliente pra esse produto, se existir - senao cai no preco padrao
  function precoEfetivo(produto) {
    if (!produto) return 0;
    const personalizado = precosCliente[produto.id];
    return personalizado !== undefined ? personalizado : produto.preco;
  }

  const totalBruto = itens.reduce((soma, it) => {
    const produto = produtoPorId(it.produtoId);
    return produto ? soma + precoEfetivo(produto) * Number(it.quantidade || 0) : soma;
  }, 0);

  // mesma regra do backend: avaria e bonificacao = galoes x preco do produto de envase presente no carrinho
  const produtoEnvaseNoCarrinho = itens.map((it) => produtoPorId(it.produtoId)).find((p) => p?.contaComoEnvase);
  const valorAvaria = ocorrencia !== 'NENHUMA' && produtoEnvaseNoCarrinho
    ? precoEfetivo(produtoEnvaseNoCarrinho) * Number(quantidadeAvarias || 0)
    : 0;
  const valorBonificado = ocorrencia === 'AVARIA_PRODUCAO' && produtoEnvaseNoCarrinho
    ? precoEfetivo(produtoEnvaseNoCarrinho) * Number(quantidadeBonificados || 0)
    : 0;
  const total = Math.max(totalBruto - valorAvaria - valorBonificado, 0);

  // no modo PIX/Especie/Fiado, o valor cheio vai pro metodo escolhido; no dividido, o operador digita os tres
  const valorEspecieFinal = modoPagamento === 'ESPECIE' ? total : modoPagamento === 'DIVIDIDO' ? Number(valorEspecieInput || 0) : 0;
  const valorPixFinal = modoPagamento === 'PIX' ? total : modoPagamento === 'DIVIDIDO' ? Number(valorPixInput || 0) : 0;
  const valorFiadoFinal = modoPagamento === 'FIADO' ? total : modoPagamento === 'DIVIDIDO' ? Number(valorFiadoInput || 0) : 0;
  const diferencaPagamento = total - (valorEspecieFinal + valorPixFinal + valorFiadoFinal);
  const precisaClienteParaFiado = valorFiadoFinal > 0 && !clienteId;
  const pagamentoValido = Math.abs(diferencaPagamento) < 0.01 && !precisaClienteParaFiado;

  // troco e so uma calculadora de apoio pro operador - o valor que entra no
  // caixa continua sendo valorEspecieFinal (o que era devido), nunca o valor
  // que o cliente entregou na mao
  const troco = Number(dinheiroEntregue || 0) - valorEspecieFinal;

  async function finalizar() {
    setErro(null);
    setSucesso(null);
    if (precisaClienteParaFiado) {
      setErro('Venda fiado exige um cliente cadastrado — selecione um cliente.');
      return;
    }
    if (!pagamentoValido) {
      setErro('A soma dos valores (espécie + PIX + fiado) precisa bater com o total da venda.');
      return;
    }
    setSalvando(true);
    try {
      const dto = {
        clienteId: clienteId || null,
        valorRecebidoEspecie: valorEspecieFinal,
        valorRecebidoPix: valorPixFinal,
        valorFiado: valorFiadoFinal,
        ocorrencia,
        quantidadeAvarias: ocorrencia !== 'NENHUMA' ? Number(quantidadeAvarias) : null,
        quantidadeBonificados: ocorrencia === 'AVARIA_PRODUCAO' ? Number(quantidadeBonificados) : null,
        observacao: observacao || null,
        itens: itens
          .filter((it) => it.produtoId)
          .map((it) => ({ produtoId: Number(it.produtoId), quantidade: Number(it.quantidade) })),
      };
      const venda = await vendasApi.registrar(dto);
      setSucesso(`Venda #${venda.id} registrada com sucesso.${valorFiadoFinal > 0 ? ' Uma conta a receber foi criada para o cliente.' : ''}`);
      setVendaParaImprimir(venda);
      setTrocoParaImprimir(troco > 0 ? troco : 0);
      setItens([{ produtoId: '', quantidade: 1 }]);
      setOcorrencia('NENHUMA');
      setQuantidadeAvarias('1');
      setQuantidadeBonificados('1');
      setObservacao('');
      setValorEspecieInput('');
      setValorPixInput('');
      setValorFiadoInput('');
      setDinheiroEntregue('');
      carregarVendasHoje();
    } catch (e) {
      setErro(e.message);
    } finally {
      setSalvando(false);
    }
  }

  if (carregando) return <Loading />;

  return (
    <div>
      <div className="flex items-start justify-between gap-3">
        <PageHeader title="Nova venda" />
        <label className="flex items-center gap-1.5 mt-1" style={{ fontSize: 12, color: C.textMuted, whiteSpace: 'nowrap' }}>
          <input type="checkbox" checked={impressaoAutomatica} onChange={alternarImpressaoAutomatica} />
          Imprimir recibo automaticamente
        </label>
      </div>
      <ErrorBanner message={erro} />
      {sucesso && (
        <div className="flex items-center justify-between gap-3 px-3 py-2 rounded mb-4" style={{ background: C.blueLight, color: C.ink, fontSize: 13 }}>
          <span>{sucesso}</span>
          {vendaParaImprimir && (
            <button type="button" onClick={imprimirNovamente} className="px-3 py-1 rounded text-xs font-medium flex-shrink-0" style={{ background: C.blue, color: '#fff' }}>
              Imprimir recibo
            </button>
          )}
        </div>
      )}
      <div className="grid grid-cols-3 gap-4">
        <Card style={{ gridColumn: 'span 2' }}>
          <Field label="Cliente (opcional)">
            <SearchableSelect
              options={clientes.map((c) => ({ value: c.id, label: c.nome }))}
              value={clienteId}
              onChange={setClienteId}
              emptyLabel="Consumidor não identificado"
              placeholder="Buscar cliente por nome..."
            />
          </Field>

          <div className="mt-4 flex flex-col gap-2">
            {itens.map((item, index) => (
              <div key={index} className="flex gap-2 items-center">
                <select
                  className="flex-1 px-3 py-2 rounded border"
                  style={{ borderColor: C.border, fontSize: 14 }}
                  value={item.produtoId}
                  onChange={(e) => atualizarItem(index, 'produtoId', e.target.value)}
                >
                  <option value="">Selecione um produto</option>
                  {produtos.map((p) => (
                    <option key={p.id} value={p.id}>
                      {p.nome} — {precoEfetivo(p).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })}
                      {precosCliente[p.id] !== undefined ? ' (preço deste cliente)' : ''}
                    </option>
                  ))}
                </select>
                <input
                  type="number"
                  min="1"
                  className="px-3 py-2 rounded border"
                  style={{ borderColor: C.border, fontSize: 14, width: 80 }}
                  value={item.quantidade}
                  onChange={(e) => atualizarItem(index, 'quantidade', e.target.value)}
                />
                {itens.length > 1 && (
                  <button type="button" onClick={() => removerItem(index)} className="px-2 text-xs" style={{ color: C.red }}>Remover</button>
                )}
              </div>
            ))}
            <button type="button" onClick={adicionarItem} className="text-xs mt-1 self-start" style={{ color: C.blue }}>+ Adicionar produto</button>
          </div>

          <div className="mt-5">
            <div style={{ fontSize: 12, color: C.textMuted, marginBottom: 6 }}>Ocorrência de avaria</div>
            <Segmented
              options={[['NENHUMA', 'Sem avaria'], ['AVARIA_CLIENTE', 'Avaria - cliente'], ['AVARIA_PRODUCAO', 'Avaria - produção']]}
              value={ocorrencia}
              onChange={setOcorrencia}
            />
            {ocorrencia !== 'NENHUMA' && (
              <div className="mt-3">
                <Field label="Quantidade de galões com avaria">
                  <TextInput type="number" min="1" style={{ maxWidth: 140 }} value={quantidadeAvarias} onChange={(e) => setQuantidadeAvarias(e.target.value)} />
                </Field>
                {ocorrencia === 'AVARIA_PRODUCAO' ? (
                  <>
                    <div className="mt-3">
                      <Field label="Quantidade de galões bonificados">
                        <TextInput type="number" min="1" style={{ maxWidth: 140 }} value={quantidadeBonificados} onChange={(e) => setQuantidadeBonificados(e.target.value)} />
                      </Field>
                    </div>
                    <div className="flex items-center gap-2 mt-2 px-3 py-2 rounded" style={{ background: C.amberLight, color: '#7A4A1F', fontSize: 12 }}>
                      <AlertTriangle size={14} /> A bonificação sai do total da venda automaticamente — não precisa ser igual à quantidade de avaria
                    </div>
                  </>
                ) : (
                  <div className="flex items-center gap-2 mt-2 px-3 py-2 rounded" style={{ background: C.amberLight, color: '#7A4A1F', fontSize: 12 }}>
                    <AlertTriangle size={14} /> Esses galões saem do total da venda automaticamente
                  </div>
                )}
              </div>
            )}
          </div>

          <div className="mt-5">
            <Field label="Observação (opcional)">
              <textarea
                className="w-full px-3 py-2 rounded border"
                style={{ borderColor: C.border, fontSize: 14, minHeight: 64, fontFamily: 'inherit', resize: 'vertical' }}
                placeholder="Qualquer detalhe sobre essa venda..."
                value={observacao}
                onChange={(e) => setObservacao(e.target.value)}
              />
            </Field>
          </div>
        </Card>

        <Card>
          <div style={{ fontSize: 12, color: C.textMuted }}>Total da venda</div>
          <div style={{ fontSize: 26, fontWeight: 500, color: C.textDark, fontFamily: DISPLAY_FONT, margin: '6px 0 4px' }}>
            {total.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })}
          </div>
          {(valorAvaria > 0 || valorBonificado > 0) && (
            <div style={{ fontSize: 12, color: C.textMuted, marginBottom: 12 }}>
              {totalBruto.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })} bruto
              {valorAvaria > 0 && <> − {valorAvaria.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })} avaria</>}
              {valorBonificado > 0 && <> − {valorBonificado.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })} bonificado</>}
            </div>
          )}
          <div style={{ fontSize: 12, color: C.textMuted, marginBottom: 6, marginTop: (valorAvaria > 0 || valorBonificado > 0) ? 0 : 16 }}>Forma de pagamento</div>
          <Segmented
            options={[['PIX', 'PIX'], ['ESPECIE', 'Espécie'], ['FIADO', 'Fiado'], ['DIVIDIDO', 'Dividido']]}
            value={modoPagamento}
            onChange={(v) => { setModoPagamento(v); setDinheiroEntregue(''); }}
          />

          {modoPagamento === 'FIADO' && (
            <div className="flex items-center gap-2 mt-3 px-3 py-2 rounded" style={{ background: C.amberLight, color: '#7A4A1F', fontSize: 12 }}>
              <AlertTriangle size={14} /> Vai virar uma conta a receber para o cliente selecionado — não entra no caixa agora.
            </div>
          )}

          {modoPagamento === 'DIVIDIDO' && (
            <div className="mt-3">
              <div className="grid grid-cols-3 gap-2">
                <Field label="Espécie">
                  <TextInput type="number" step="0.01" value={valorEspecieInput} onChange={(e) => setValorEspecieInput(e.target.value)} />
                </Field>
                <Field label="PIX">
                  <TextInput type="number" step="0.01" value={valorPixInput} onChange={(e) => setValorPixInput(e.target.value)} />
                </Field>
                <Field label="Fiado">
                  <TextInput type="number" step="0.01" value={valorFiadoInput} onChange={(e) => setValorFiadoInput(e.target.value)} />
                </Field>
              </div>
              {Math.abs(diferencaPagamento) >= 0.01 && (
                <div className="flex items-center gap-2 mt-2 px-3 py-2 rounded" style={{ background: C.amberLight, color: '#7A4A1F', fontSize: 12 }}>
                  <AlertTriangle size={14} />
                  {diferencaPagamento > 0
                    ? `Falta ${diferencaPagamento.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })} para completar o total`
                    : `Passou ${Math.abs(diferencaPagamento).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })} do total`}
                </div>
              )}
            </div>
          )}

          {precisaClienteParaFiado && (
            <div className="flex items-center gap-2 mt-2 px-3 py-2 rounded" style={{ background: C.amberLight, color: '#7A4A1F', fontSize: 12 }}>
              <AlertTriangle size={14} /> Selecione um cliente cadastrado para vender fiado.
            </div>
          )}

          {valorEspecieFinal > 0 && (
            <div className="mt-3 p-3 rounded" style={{ background: C.bg, border: `1px solid ${C.border}` }}>
              <Field label="Dinheiro entregue pelo cliente (opcional)">
                <TextInput
                  type="number"
                  step="0.01"
                  placeholder={valorEspecieFinal.toFixed(2)}
                  value={dinheiroEntregue}
                  onChange={(e) => setDinheiroEntregue(e.target.value)}
                />
              </Field>
              {dinheiroEntregue !== '' && (
                troco >= 0 ? (
                  <div className="flex justify-between items-center mt-2 px-3 py-2 rounded" style={{ background: C.blueLight }}>
                    <span style={{ fontSize: 13, color: C.ink }}>Troco</span>
                    <span style={{ fontSize: 18, fontWeight: 600, color: C.ink, fontFamily: DISPLAY_FONT }}>
                      {troco.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })}
                    </span>
                  </div>
                ) : (
                  <div className="flex items-center gap-2 mt-2 px-3 py-2 rounded" style={{ background: C.amberLight, color: '#7A4A1F', fontSize: 12 }}>
                    <AlertTriangle size={14} /> Falta {Math.abs(troco).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })} — valor entregue é menor que o devido em espécie
                  </div>
                )
              )}
            </div>
          )}

          <button onClick={finalizar} disabled={salvando || !pagamentoValido} className="w-full mt-5 py-2 rounded text-sm font-medium disabled:opacity-60" style={{ background: C.red, color: '#fff' }}>
            {salvando ? 'Finalizando...' : 'Finalizar venda'}
          </button>
        </Card>
      </div>

      <Card style={{ marginTop: 16 }}>
        <div className="flex items-center gap-2 mb-3">
          <Clock size={15} color={C.textMuted} />
          <div style={{ fontSize: 13, fontWeight: 500, color: C.textDark }}>Vendas de hoje</div>
        </div>
        {carregandoVendasHoje ? (
          <div style={{ fontSize: 13, color: C.textMuted, padding: '8px 0' }}>Carregando...</div>
        ) : vendasHoje.length === 0 ? (
          <div className="py-6 text-center" style={{ fontSize: 13, color: C.textMuted }}>Nenhuma venda registrada ainda no caixa de hoje.</div>
        ) : (
          <div className="flex flex-col gap-2" style={{ maxHeight: 300, overflowY: 'auto', paddingRight: 4 }}>
            {[...vendasHoje].reverse().map((v) => (
              <div key={v.id} className="py-2" style={{ borderBottom: `1px solid ${C.border}`, fontSize: 12 }}>
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-3 min-w-0">
                    <span style={{ color: C.textMuted, flexShrink: 0 }}>
                      {new Date(v.dataHora).toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })}
                    </span>
                    <span style={{ color: C.textDark }} className="truncate">
                      {v.cliente?.nome || 'Consumidor não identificado'}
                    </span>
                    <span style={{ color: C.textMuted }} className="truncate">
                      {v.itens?.map((i) => `${i.quantidade}x ${i.produto?.nome}`).join(', ')}
                    </span>
                  </div>
                  <div className="flex items-center gap-2 flex-shrink-0">
                    <Badge tone="neutral">{rotuloPagamento(v)}</Badge>
                    {v.ocorrencia === 'AVARIA_PRODUCAO' && <Badge tone="amber">Avaria produção</Badge>}
                    {v.ocorrencia === 'AVARIA_CLIENTE' && <Badge tone="neutral">Avaria cliente</Badge>}
                    <span style={{ fontWeight: 600, color: C.textDark, minWidth: 64, textAlign: 'right' }}>
                      {v.valorTotal.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })}
                    </span>
                  </div>
                </div>
                {v.observacao && (
                  <div style={{ color: C.textMuted, fontSize: 11, marginTop: 3, paddingLeft: 2, fontStyle: 'italic' }} className="truncate">
                    "{v.observacao}"
                  </div>
                )}
              </div>
            ))}
          </div>
        )}
      </Card>

      <ReciboVenda venda={vendaParaImprimir} troco={trocoParaImprimir} />
    </div>
  );
}
