import React, { useEffect, useState } from 'react';
import { Clock } from 'lucide-react';
import { PageHeader, Card, Field, TextInput, Segmented, ErrorBanner, Loading, Badge, SearchableSelect, MoneyInput } from '../../components/ui';
import { C, DISPLAY_FONT } from '../../theme';
import { clientesTerceirosApi } from '../../api/terceiros/clientes';
import { produtosTerceirosApi } from '../../api/terceiros/produtos';
import { vendasTerceirosApi } from '../../api/terceiros/vendas';
import { caixaTerceirosApi } from '../../api/terceiros/caixa';

const moeda = (v) => (v ?? 0).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });

export default function VendasTerceiros() {
  const [clientes, setClientes] = useState([]);
  const [produtos, setProdutos] = useState([]);
  const [caixa, setCaixa] = useState(null);
  const [vendasHoje, setVendasHoje] = useState([]);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState(null);
  const [sucesso, setSucesso] = useState(null);
  const [salvando, setSalvando] = useState(false);

  const [clienteId, setClienteId] = useState('');
  const [placaCaminhao, setPlacaCaminhao] = useState('');
  const [itens, setItens] = useState([{ produtoId: '', quantidade: 1 }]);
  const [modoPagamento, setModoPagamento] = useState('PIX'); // 'PIX' | 'ESPECIE' | 'FIADO' | 'DIVIDIDO'
  const [valorEspecieInput, setValorEspecieInput] = useState('');
  const [valorPixInput, setValorPixInput] = useState('');
  const [valorFiadoInput, setValorFiadoInput] = useState('');

  async function carregar() {
    setCarregando(true);
    try {
      const [cli, prod] = await Promise.all([clientesTerceirosApi.listar(), produtosTerceirosApi.listar()]);
      setClientes(cli);
      setProdutos(prod);
      try {
        const c = await caixaTerceirosApi.atual();
        setCaixa(c);
        if (c?.id) setVendasHoje(await vendasTerceirosApi.listarPorCaixa(c.id));
      } catch {
        setCaixa(null);
      }
    } catch (e) {
      setErro(e.message);
    }
    setCarregando(false);
  }

  useEffect(() => { carregar(); }, []);

  function produtoPorId(id) {
    return produtos.find((p) => String(p.id) === String(id));
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

  const total = itens.reduce((soma, it) => {
    const produto = produtoPorId(it.produtoId);
    return produto ? soma + produto.preco * Number(it.quantidade || 0) : soma;
  }, 0);

  const valorEspecieFinal = modoPagamento === 'ESPECIE' ? total : modoPagamento === 'DIVIDIDO' ? Number(valorEspecieInput || 0) : 0;
  const valorPixFinal = modoPagamento === 'PIX' ? total : modoPagamento === 'DIVIDIDO' ? Number(valorPixInput || 0) : 0;
  const valorFiadoFinal = modoPagamento === 'FIADO' ? total : modoPagamento === 'DIVIDIDO' ? Number(valorFiadoInput || 0) : 0;
  const diferenca = total - (valorEspecieFinal + valorPixFinal + valorFiadoFinal);
  const pagamentoValido = Math.abs(diferenca) < 0.01;

  async function finalizar() {
    setErro(null);
    setSucesso(null);
    if (!clienteId) {
      setErro('Selecione um cliente.');
      return;
    }
    if (!pagamentoValido) {
      setErro('A soma dos valores (espécie + PIX) precisa bater com o total da venda.');
      return;
    }
    setSalvando(true);
    try {
      const dto = {
        clienteId: Number(clienteId),
        placaCaminhao: placaCaminhao || null,
        valorRecebidoEspecie: valorEspecieFinal,
        valorRecebidoPix: valorPixFinal,
        valorFiado: valorFiadoFinal,
        itens: itens.filter((it) => it.produtoId).map((it) => ({ produtoId: Number(it.produtoId), quantidade: Number(it.quantidade) })),
      };
      const venda = await vendasTerceirosApi.registrar(dto);
      setSucesso(`Venda #${venda.id} registrada com sucesso.${valorFiadoFinal > 0 ? ' Uma conta a receber foi criada para o cliente.' : ''}`);
      setItens([{ produtoId: '', quantidade: 1 }]);
      setPlacaCaminhao('');
      setValorEspecieInput('');
      setValorPixInput('');
      setValorFiadoInput('');
      if (caixa?.id) setVendasHoje(await vendasTerceirosApi.listarPorCaixa(caixa.id));
    } catch (e) {
      setErro(e.message);
    } finally {
      setSalvando(false);
    }
  }

  if (carregando) return <Loading />;

  if (!caixa) {
    return (
      <div>
        <PageHeader title="Vendas de Terceiros" />
        <Card>
          <div style={{ fontSize: 13, color: C.textMuted }}>Abra o caixa de terceiros antes de registrar uma venda.</div>
        </Card>
      </div>
    );
  }

  return (
    <div>
      <PageHeader title="Vendas de Terceiros" />
      {sucesso && <div className="mb-4 px-4 py-3 rounded" style={{ background: C.blueLight, color: C.ink, fontSize: 13 }}>{sucesso}</div>}
      <ErrorBanner message={erro} />

      <div className="grid grid-cols-3 gap-4">
        <div style={{ gridColumn: 'span 2' }}>
          <Card>
            <div className="grid grid-cols-2 gap-4 mb-4">
              <Field label="Cliente (empresa terceira)">
                <SearchableSelect
                  options={clientes.map((c) => ({ value: c.id, label: c.nome }))}
                  value={clienteId}
                  onChange={setClienteId}
                  emptyLabel="Selecione"
                  placeholder="Buscar cliente por nome..."
                />
              </Field>
              <Field label="Placa do caminhão (opcional)"><TextInput value={placaCaminhao} onChange={(e) => setPlacaCaminhao(e.target.value)} /></Field>
            </div>

            <div className="flex flex-col gap-2">
              {itens.map((item, index) => (
                <div key={index} className="flex gap-2 items-center">
                  <select
                    className="flex-1 px-3 py-2 rounded border"
                    style={{ borderColor: C.border, fontSize: 14 }}
                    value={item.produtoId}
                    onChange={(e) => atualizarItem(index, 'produtoId', e.target.value)}
                  >
                    <option value="">Selecione um produto</option>
                    {produtos.map((p) => <option key={p.id} value={p.id}>{p.nome} — {moeda(p.preco)}</option>)}
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
          </Card>
        </div>

        <Card>
          <div style={{ fontSize: 12, color: C.textMuted }}>Total da venda</div>
          <div style={{ fontSize: 28, fontWeight: 600, color: C.textDark, fontFamily: DISPLAY_FONT, marginBottom: 16 }}>{moeda(total)}</div>

          <div style={{ fontSize: 12, color: C.textMuted, marginBottom: 6 }}>Forma de pagamento</div>
          <Segmented options={[['PIX', 'PIX'], ['ESPECIE', 'Espécie'], ['FIADO', 'Fiado'], ['DIVIDIDO', 'Dividido']]} value={modoPagamento} onChange={setModoPagamento} />

          {modoPagamento === 'FIADO' && (
            <div className="mt-3 px-3 py-2 rounded" style={{ background: C.amberLight, color: '#7A4A1F', fontSize: 12 }}>
              Vai virar uma conta a receber para este cliente — não entra no caixa agora.
            </div>
          )}

          {modoPagamento === 'DIVIDIDO' && (
            <div className="mt-3">
              <div className="grid grid-cols-3 gap-2">
                <Field label="Espécie"><MoneyInput value={valorEspecieInput} onChange={(v) => setValorEspecieInput(v)} /></Field>
                <Field label="PIX"><MoneyInput value={valorPixInput} onChange={(v) => setValorPixInput(v)} /></Field>
                <Field label="Fiado"><MoneyInput value={valorFiadoInput} onChange={(v) => setValorFiadoInput(v)} /></Field>
              </div>
              {!pagamentoValido && (
                <div className="mt-2 px-3 py-2 rounded" style={{ background: C.amberLight, color: '#7A4A1F', fontSize: 12 }}>
                  {diferenca > 0 ? `Falta ${moeda(diferenca)}` : `Passou ${moeda(Math.abs(diferenca))}`}
                </div>
              )}
            </div>
          )}

          <button onClick={finalizar} disabled={salvando || !pagamentoValido} className="w-full mt-5 py-2 rounded text-sm font-medium disabled:opacity-60" style={{ background: C.red, color: '#fff' }}>
            {salvando ? 'Finalizando...' : 'Finalizar venda'}
          </button>
        </Card>
      </div>

      <Card className="mt-4">
        <div className="flex items-center gap-1.5 mb-3" style={{ fontSize: 13, fontWeight: 500, color: C.textDark }}>
          <Clock size={14} /> Vendas de hoje neste caixa
        </div>
        {vendasHoje.length === 0 ? (
          <div style={{ fontSize: 13, color: C.textMuted }}>Nenhuma venda registrada ainda neste caixa.</div>
        ) : (
          <div className="flex flex-col gap-2" style={{ maxHeight: 260, overflowY: 'auto' }}>
            {vendasHoje.map((v) => (
              <div key={v.id} className="flex justify-between items-center py-2" style={{ borderBottom: `1px solid ${C.border}`, fontSize: 13 }}>
                <div>
                  <div style={{ color: C.textDark }}>{v.cliente?.nome}{v.placaCaminhao ? ` · ${v.placaCaminhao}` : ''}</div>
                  <div style={{ fontSize: 11, color: C.textMuted }}>{new Date(v.dataHora).toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })}</div>
                </div>
                <div style={{ fontWeight: 600, color: C.textDark }}>{moeda(v.valorTotal)}</div>
              </div>
            ))}
          </div>
        )}
      </Card>
    </div>
  );
}
