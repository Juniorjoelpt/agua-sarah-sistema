import React, { useEffect, useState } from 'react';
import { Truck, User, Pencil, History, CalendarDays } from 'lucide-react';
import { Card, Field, TextInput, ErrorBanner, Loading, Badge, EmptyState, SearchableSelect } from '../../components/ui';
import Modal from '../../components/Modal';
import { C, DISPLAY_FONT } from '../../theme';
import { frotaTerceirosApi } from '../../api/terceiros/frota';
import { clientesTerceirosApi } from '../../api/terceiros/clientes';
import { produtosTerceirosApi } from '../../api/terceiros/produtos';

const CAMINHAO_VAZIO = { clienteId: '', placa: '', motorista: '', ativo: true };
const CARREGAMENTO_VAZIO = { produtoId: '', rota: '', quantidadeCarregada: '', precoVenda: '' };
const PRESTACAO_VAZIA = { quantidadeAvaria: '0', quantidadeDevolvida: '0' };

const moeda = (v) => (v ?? 0).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
const dataCurta = (iso) => new Date(iso).toLocaleDateString('pt-BR', { day: '2-digit', month: '2-digit', year: 'numeric' });

function agruparPorData(prestacoes) {
  const grupos = new Map();
  for (const p of prestacoes) {
    const chave = dataCurta(p.dataPrestacao);
    if (!grupos.has(chave)) grupos.set(chave, []);
    grupos.get(chave).push(p);
  }
  return Array.from(grupos.entries());
}

export default function FrotaTerceiros() {
  const [caminhoes, setCaminhoes] = useState([]);
  const [clientes, setClientes] = useState([]);
  const [produtos, setProdutos] = useState([]);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState(null);
  const [salvando, setSalvando] = useState(false);

  const [modalCaminhao, setModalCaminhao] = useState(false);
  const [editandoId, setEditandoId] = useState(null);
  const [form, setForm] = useState(CAMINHAO_VAZIO);

  const [caminhaoCarregamento, setCaminhaoCarregamento] = useState(null);
  const [carregamentoForm, setCarregamentoForm] = useState(CARREGAMENTO_VAZIO);

  const [caminhaoPrestacao, setCaminhaoPrestacao] = useState(null);
  const [carregamentoAtivo, setCarregamentoAtivo] = useState(null);
  const [prestacaoForm, setPrestacaoForm] = useState(PRESTACAO_VAZIA);
  const [despesasCarregamento, setDespesasCarregamento] = useState([]);
  const [novaDespesa, setNovaDespesa] = useState({ descricao: '', valor: '' });
  const [salvandoDespesa, setSalvandoDespesa] = useState(false);

  const [caminhaoHistorico, setCaminhaoHistorico] = useState(null);
  const [historico, setHistorico] = useState([]);
  const [carregandoHistorico, setCarregandoHistorico] = useState(false);

  const [modalPrestacoes, setModalPrestacoes] = useState(false);
  const [prestacoesPeriodo, setPrestacoesPeriodo] = useState([]);
  const [carregandoPrestacoes, setCarregandoPrestacoes] = useState(false);
  const [caminhaoFiltro, setCaminhaoFiltro] = useState('');
  const [baixandoPdfPrestacoes, setBaixandoPdfPrestacoes] = useState(false);
  const [inicioPeriodo, setInicioPeriodo] = useState(() => {
    const hoje = new Date();
    return new Date(hoje.getFullYear(), hoje.getMonth(), 1).toISOString().slice(0, 10);
  });
  const [fimPeriodo, setFimPeriodo] = useState(() => new Date().toISOString().slice(0, 10));

  async function carregar() {
    setCarregando(true);
    try {
      const [cam, cli, prod] = await Promise.all([frotaTerceirosApi.listarCaminhoes(), clientesTerceirosApi.listar(), produtosTerceirosApi.listar()]);
      setCaminhoes(cam);
      setClientes(cli);
      setProdutos(prod);
    } catch (e) {
      setErro(e.message);
    }
    setCarregando(false);
  }

  useEffect(() => { carregar(); }, []);

  function abrirNovo() {
    setEditandoId(null);
    setForm(CAMINHAO_VAZIO);
    setModalCaminhao(true);
  }

  function abrirEdicao(c) {
    setEditandoId(c.id);
    setForm({ clienteId: c.cliente?.id || '', placa: c.placa, motorista: c.motorista || '', ativo: c.ativo });
    setModalCaminhao(true);
  }

  async function salvarCaminhao() {
    setSalvando(true);
    setErro(null);
    try {
      const dto = { ...form, clienteId: Number(form.clienteId) };
      if (editandoId) {
        await frotaTerceirosApi.atualizarCaminhao(editandoId, dto);
      } else {
        await frotaTerceirosApi.criarCaminhao(dto);
      }
      setModalCaminhao(false);
      await carregar();
    } catch (e) {
      setErro(e.message);
    } finally {
      setSalvando(false);
    }
  }

  function abrirModalCarregamento(caminhao) {
    setErro(null);
    setCaminhaoCarregamento(caminhao);
    setCarregamentoForm(CARREGAMENTO_VAZIO);
  }

  function escolherProdutoCarregamento(produtoId) {
    const produto = produtos.find((p) => String(p.id) === String(produtoId));
    setCarregamentoForm({ ...carregamentoForm, produtoId, precoVenda: produto ? String(produto.preco) : carregamentoForm.precoVenda });
  }

  async function salvarCarregamento() {
    setSalvando(true);
    setErro(null);
    try {
      await frotaTerceirosApi.abrirCarregamento({
        caminhaoId: caminhaoCarregamento.id,
        produtoId: Number(carregamentoForm.produtoId),
        rota: carregamentoForm.rota,
        quantidadeCarregada: Number(carregamentoForm.quantidadeCarregada),
        precoVenda: Number(carregamentoForm.precoVenda),
      });
      setCaminhaoCarregamento(null);
    } catch (e) {
      setErro(e.message);
    } finally {
      setSalvando(false);
    }
  }

  async function abrirModalPrestacao(caminhao) {
    setErro(null);
    setCaminhaoPrestacao(caminhao);
    setCarregamentoAtivo(null);
    setPrestacaoForm(PRESTACAO_VAZIA);
    setDespesasCarregamento([]);
    setNovaDespesa({ descricao: '', valor: '' });
    try {
      const carregamentos = await frotaTerceirosApi.historicoCarregamentos(caminhao.id);
      const pendente = carregamentos.find((c) => c.status === 'PENDENTE');
      if (pendente) {
        setCarregamentoAtivo(pendente);
        setDespesasCarregamento(await frotaTerceirosApi.listarDespesasCarregamento(pendente.id));
      } else {
        setErro('Este caminhão não tem carregamento pendente de prestação de contas.');
      }
    } catch (e) {
      setErro(e.message);
    }
  }

  async function adicionarDespesa() {
    setSalvandoDespesa(true);
    setErro(null);
    try {
      await frotaTerceirosApi.registrarDespesaCarregamento(carregamentoAtivo.id, {
        descricao: novaDespesa.descricao,
        valor: Number(novaDespesa.valor),
      });
      setNovaDespesa({ descricao: '', valor: '' });
      setDespesasCarregamento(await frotaTerceirosApi.listarDespesasCarregamento(carregamentoAtivo.id));
    } catch (e) {
      setErro(e.message);
    } finally {
      setSalvandoDespesa(false);
    }
  }

  async function confirmarPrestacao() {
    setSalvando(true);
    setErro(null);
    try {
      await frotaTerceirosApi.registrarPrestacaoContas({
        carregamentoId: carregamentoAtivo.id,
        quantidadeAvaria: Number(prestacaoForm.quantidadeAvaria),
        quantidadeDevolvida: Number(prestacaoForm.quantidadeDevolvida),
      });
      setCaminhaoPrestacao(null);
      setCarregamentoAtivo(null);
    } catch (e) {
      setErro(e.message);
    } finally {
      setSalvando(false);
    }
  }

  async function abrirHistorico(caminhao) {
    setErro(null);
    setCaminhaoHistorico(caminhao);
    setCarregandoHistorico(true);
    try {
      setHistorico(await frotaTerceirosApi.historicoCarregamentos(caminhao.id));
    } catch (e) {
      setErro(e.message);
    } finally {
      setCarregandoHistorico(false);
    }
  }

  async function abrirPrestacoesPeriodo() {
    setModalPrestacoes(true);
    await carregarPrestacoesPeriodo();
  }

  async function carregarPrestacoesPeriodo() {
    setErro(null);
    setCarregandoPrestacoes(true);
    try {
      setPrestacoesPeriodo(await frotaTerceirosApi.listarPrestacoes(inicioPeriodo, fimPeriodo, caminhaoFiltro || undefined));
    } catch (e) {
      setErro(e.message);
    } finally {
      setCarregandoPrestacoes(false);
    }
  }

  async function baixarPdfPrestacoes() {
    setErro(null);
    setBaixandoPdfPrestacoes(true);
    try {
      await frotaTerceirosApi.baixarPdfPrestacoes(inicioPeriodo, fimPeriodo, caminhaoFiltro || undefined);
    } catch (e) {
      setErro(e.message);
    } finally {
      setBaixandoPdfPrestacoes(false);
    }
  }

  if (carregando) return <Loading />;

  const algumModalAberto = modalCaminhao || caminhaoCarregamento || caminhaoPrestacao || caminhaoHistorico || modalPrestacoes;

  const totalDespesasPreview = despesasCarregamento.reduce((s, d) => s + d.valor, 0);
  const quantidadeVendidaPreview = carregamentoAtivo
    ? Math.max(carregamentoAtivo.quantidadeCarregada - Number(prestacaoForm.quantidadeAvaria || 0) - Number(prestacaoForm.quantidadeDevolvida || 0), 0)
    : 0;
  const valorVendaPreview = carregamentoAtivo ? quantidadeVendidaPreview * carregamentoAtivo.precoVenda : 0;
  const lucroPreview = valorVendaPreview - totalDespesasPreview;

  return (
    <div>
      <div className="flex items-start justify-between mb-6">
        <div>
          <h2 style={{ fontFamily: DISPLAY_FONT, fontSize: 22, fontWeight: 500, color: C.onDark }}>Frota</h2>
          <p style={{ fontSize: 13, color: C.onDarkMuted, marginTop: 2 }}>Caminhões das empresas terceiras</p>
        </div>
        <div className="flex gap-2" style={{ flexShrink: 0 }}>
          <button onClick={abrirPrestacoesPeriodo} className="flex items-center gap-1.5 px-4 py-2 rounded text-sm" style={{ background: C.bg, color: C.textDark, border: `1px solid ${C.border}` }}>
            <CalendarDays size={14} /> Prestações por data
          </button>
          <button onClick={abrirNovo} className="px-4 py-2 rounded text-sm font-medium" style={{ background: C.red, color: '#fff' }}>+ Novo caminhão</button>
        </div>
      </div>
      {!algumModalAberto && <ErrorBanner message={erro} />}

      <Modal open={modalCaminhao} title={editandoId ? 'Editar caminhão' : 'Novo caminhão'} onClose={() => setModalCaminhao(false)} maxWidth={480}>
        <ErrorBanner message={erro} />
        <Field label="Cliente (empresa terceira)">
          <SearchableSelect
            options={clientes.map((c) => ({ value: c.id, label: c.nome }))}
            value={form.clienteId}
            onChange={(v) => setForm({ ...form, clienteId: v })}
            emptyLabel="Selecione"
            placeholder="Buscar cliente por nome..."
          />
        </Field>
        <div className="grid grid-cols-2 gap-4 mt-3">
          <Field label="Placa"><TextInput value={form.placa} onChange={(e) => setForm({ ...form, placa: e.target.value })} /></Field>
          <Field label="Motorista"><TextInput value={form.motorista} onChange={(e) => setForm({ ...form, motorista: e.target.value })} /></Field>
        </div>
        <div className="flex gap-2 mt-6">
          <button type="button" disabled={salvando || !form.clienteId || !form.placa} onClick={salvarCaminhao} className="px-4 py-2 rounded text-sm font-medium disabled:opacity-60" style={{ background: C.red, color: '#fff' }}>
            {salvando ? 'Salvando...' : 'Salvar'}
          </button>
          <button type="button" onClick={() => setModalCaminhao(false)} className="px-4 py-2 rounded text-sm" style={{ background: C.bg, color: C.textDark }}>Cancelar</button>
        </div>
      </Modal>

      <Modal open={!!caminhaoCarregamento} title={`Novo carregamento — ${caminhaoCarregamento?.placa || ''}`} onClose={() => setCaminhaoCarregamento(null)}>
        <ErrorBanner message={erro} />
        <Field label="Produto">
          <select className="w-full px-3 py-2 rounded border" style={{ borderColor: C.border, fontSize: 14 }} value={carregamentoForm.produtoId} onChange={(e) => escolherProdutoCarregamento(e.target.value)}>
            <option value="">Selecione (galão, garrafa...)</option>
            {produtos.map((p) => <option key={p.id} value={p.id}>{p.nome} — {moeda(p.preco)}</option>)}
          </select>
        </Field>
        <div className="grid grid-cols-2 gap-4 mt-3">
          <Field label="Rota"><TextInput placeholder="Ex: Zona Norte" value={carregamentoForm.rota} onChange={(e) => setCarregamentoForm({ ...carregamentoForm, rota: e.target.value })} /></Field>
          <Field label="Quantidade carregada"><TextInput type="number" value={carregamentoForm.quantidadeCarregada} onChange={(e) => setCarregamentoForm({ ...carregamentoForm, quantidadeCarregada: e.target.value })} /></Field>
        </div>
        <div className="mt-3">
          <Field label="Valor de venda (por unidade)">
            <TextInput type="number" step="0.01" value={carregamentoForm.precoVenda} onChange={(e) => setCarregamentoForm({ ...carregamentoForm, precoVenda: e.target.value })} />
          </Field>
        </div>
        <div className="mt-3 px-3 py-2 rounded" style={{ background: C.amberLight, color: '#7A4A1F', fontSize: 12 }}>
          O valor total desse carregamento vai ser debitado do caixa de terceiros agora, como se a empresa tivesse comprado da Água Sarah na hora.
        </div>
        <div className="flex gap-2 mt-6">
          <button type="button" disabled={salvando || !carregamentoForm.produtoId} onClick={salvarCarregamento} className="px-4 py-2 rounded text-sm font-medium disabled:opacity-60" style={{ background: C.red, color: '#fff' }}>
            {salvando ? 'Salvando...' : 'Salvar carregamento'}
          </button>
          <button type="button" onClick={() => setCaminhaoCarregamento(null)} className="px-4 py-2 rounded text-sm" style={{ background: C.bg, color: C.textDark }}>Cancelar</button>
        </div>
      </Modal>

      <Modal open={!!caminhaoPrestacao} title={`Prestação de contas — ${caminhaoPrestacao?.placa || ''}`} onClose={() => setCaminhaoPrestacao(null)} maxWidth={560}>
        <ErrorBanner message={erro} />
        {carregamentoAtivo && (
          <>
            <div className="grid grid-cols-2 gap-3 mb-4">
              <div className="rounded-lg p-3" style={{ background: C.bg }}>
                <div style={{ fontSize: 11, color: C.textMuted }}>Carregado em {dataCurta(carregamentoAtivo.dataCarregamento)}</div>
                <div style={{ fontSize: 16, fontWeight: 600, color: C.textDark, fontFamily: DISPLAY_FONT }}>{carregamentoAtivo.quantidadeCarregada}x {carregamentoAtivo.produto?.nome}</div>
                <div style={{ fontSize: 11, color: C.textMuted }}>Rota: {carregamentoAtivo.rota}</div>
              </div>
              <div className="rounded-lg p-3" style={{ background: C.bg }}>
                <div style={{ fontSize: 11, color: C.textMuted }}>Valor debitado do caixa</div>
                <div style={{ fontSize: 16, fontWeight: 600, color: C.textDark, fontFamily: DISPLAY_FONT }}>{moeda(carregamentoAtivo.valorCarregamento)}</div>
                <div style={{ fontSize: 11, color: C.textMuted }}>{moeda(carregamentoAtivo.precoVenda)} por unidade</div>
              </div>
            </div>
            <div className="grid grid-cols-2 gap-4">
              <Field label="Quantidade de avaria"><TextInput type="number" value={prestacaoForm.quantidadeAvaria} onChange={(e) => setPrestacaoForm({ ...prestacaoForm, quantidadeAvaria: e.target.value })} /></Field>
              <Field label="Devolvidos"><TextInput type="number" value={prestacaoForm.quantidadeDevolvida} onChange={(e) => setPrestacaoForm({ ...prestacaoForm, quantidadeDevolvida: e.target.value })} /></Field>
            </div>

            <div className="mt-4 pt-4" style={{ borderTop: `1px solid ${C.border}` }}>
              <div style={{ fontSize: 12, fontWeight: 500, color: C.textDark, marginBottom: 8 }}>Despesas do carregamento</div>
              {despesasCarregamento.length > 0 && (
                <div className="flex flex-col gap-1 mb-3">
                  {despesasCarregamento.map((d) => (
                    <div key={d.id} className="flex justify-between" style={{ fontSize: 12, color: C.textMuted }}>
                      <span>{d.descricao}</span>
                      <span style={{ color: C.textDark }}>{moeda(d.valor)}</span>
                    </div>
                  ))}
                </div>
              )}
              <div className="flex gap-2 items-end">
                <div className="flex-1">
                  <Field label="Descrição"><TextInput placeholder="Ex: Combustível" value={novaDespesa.descricao} onChange={(e) => setNovaDespesa({ ...novaDespesa, descricao: e.target.value })} /></Field>
                </div>
                <div style={{ width: 110 }}>
                  <Field label="Valor"><TextInput type="number" step="0.01" value={novaDespesa.valor} onChange={(e) => setNovaDespesa({ ...novaDespesa, valor: e.target.value })} /></Field>
                </div>
                <button
                  type="button"
                  disabled={salvandoDespesa || !novaDespesa.descricao || !novaDespesa.valor}
                  onClick={adicionarDespesa}
                  className="px-3 py-2 rounded text-xs disabled:opacity-60"
                  style={{ background: C.bg, color: C.textDark, border: `1px solid ${C.border}`, height: 38 }}
                >
                  + Adicionar
                </button>
              </div>
            </div>

            <div className="flex flex-col gap-1 mt-4 px-3 py-2 rounded" style={{ background: C.blueLight, color: C.ink, fontSize: 12 }}>
              <div className="flex justify-between"><span>Quantidade vendida</span><span>{quantidadeVendidaPreview}x ({moeda(valorVendaPreview)})</span></div>
              <div className="flex justify-between"><span>Despesas do carregamento</span><span>− {moeda(totalDespesasPreview)}</span></div>
              <div className="flex justify-between pt-1" style={{ borderTop: '1px solid rgba(21,49,107,0.15)', fontWeight: 600 }}><span>Lucro (vai creditar no caixa)</span><span>{moeda(lucroPreview)}</span></div>
            </div>
          </>
        )}
        <div className="flex gap-2 mt-6">
          <button type="button" disabled={salvando || !carregamentoAtivo} onClick={confirmarPrestacao} className="px-4 py-2 rounded text-sm font-medium disabled:opacity-60" style={{ background: C.red, color: '#fff' }}>
            {salvando ? 'Salvando...' : 'Confirmar prestação'}
          </button>
          <button type="button" onClick={() => setCaminhaoPrestacao(null)} className="px-4 py-2 rounded text-sm" style={{ background: C.bg, color: C.textDark }}>Cancelar</button>
        </div>
      </Modal>

      <Modal open={!!caminhaoHistorico} title={`Histórico — ${caminhaoHistorico?.placa || ''}`} onClose={() => setCaminhaoHistorico(null)} maxWidth={560}>
        <ErrorBanner message={erro} />
        {carregandoHistorico ? (
          <div style={{ fontSize: 13, color: C.textMuted }}>Carregando...</div>
        ) : historico.length === 0 ? (
          <EmptyState message="Nenhum carregamento registrado ainda para este caminhão." />
        ) : (
          <div className="flex flex-col gap-2" style={{ maxHeight: 400, overflowY: 'auto', paddingRight: 4 }}>
            {historico.map((c) => (
              <div key={c.id} className="flex items-center justify-between px-3 py-2.5 rounded-lg" style={{ border: `1px solid ${C.border}` }}>
                <div>
                  <div style={{ fontSize: 13, fontWeight: 500, color: C.textDark }}>{dataCurta(c.dataCarregamento)} · {c.rota}</div>
                  <div style={{ fontSize: 11, color: C.textMuted }}>{c.quantidadeCarregada}x {c.produto?.nome} · {moeda(c.valorCarregamento)}</div>
                </div>
                <Badge tone={c.status === 'PENDENTE' ? 'amber' : 'blue'}>{c.status === 'PENDENTE' ? 'Pendente' : 'Prestado'}</Badge>
              </div>
            ))}
          </div>
        )}
      </Modal>

      {/* ---- Modal: prestacoes por data (relatorio da frota de terceiros) ---- */}
      <Modal open={modalPrestacoes} title="Prestações por data" onClose={() => setModalPrestacoes(false)} maxWidth={680}>
        <ErrorBanner message={erro} />
        <div className="flex gap-3 items-end flex-wrap mb-4">
          <Field label="De"><TextInput type="date" value={inicioPeriodo} onChange={(e) => setInicioPeriodo(e.target.value)} style={{ width: 150 }} /></Field>
          <Field label="Até"><TextInput type="date" value={fimPeriodo} onChange={(e) => setFimPeriodo(e.target.value)} style={{ width: 150 }} /></Field>
          <Field label="Caminhão">
            <select className="px-3 py-2 rounded border" style={{ borderColor: C.border, fontSize: 14, width: 160 }} value={caminhaoFiltro} onChange={(e) => setCaminhaoFiltro(e.target.value)}>
              <option value="">Todos</option>
              {caminhoes.map((cm) => <option key={cm.id} value={cm.id}>{cm.placa}</option>)}
            </select>
          </Field>
          <button onClick={carregarPrestacoesPeriodo} className="px-4 py-2 rounded text-sm font-medium" style={{ background: C.ink, color: '#fff' }}>Buscar</button>
          <button onClick={baixarPdfPrestacoes} disabled={baixandoPdfPrestacoes} className="px-4 py-2 rounded text-sm font-medium disabled:opacity-60" style={{ background: C.red, color: '#fff' }}>
            {baixandoPdfPrestacoes ? 'Gerando...' : 'Baixar PDF'}
          </button>
        </div>

        {carregandoPrestacoes ? (
          <div style={{ fontSize: 13, color: C.textMuted }}>Carregando...</div>
        ) : prestacoesPeriodo.length === 0 ? (
          <EmptyState message="Nenhuma prestação de contas nesse período." />
        ) : (
          <div className="flex flex-col gap-4" style={{ maxHeight: 440, overflowY: 'auto', paddingRight: 4 }}>
            {agruparPorData(prestacoesPeriodo).map(([data, itens]) => (
              <div key={data}>
                <div style={{ fontSize: 12, fontWeight: 600, color: C.textMuted, marginBottom: 6 }}>{data}</div>
                <div className="flex flex-col gap-2">
                  {itens.map((p) => (
                    <div key={p.id} className="rounded-lg p-3" style={{ border: `1px solid ${C.border}` }}>
                      <div className="flex justify-between items-start mb-2">
                        <div>
                          <div style={{ fontSize: 13, fontWeight: 600, color: C.textDark, fontFamily: DISPLAY_FONT }}>{p.carregamento?.caminhao?.placa}</div>
                          <div style={{ fontSize: 11, color: C.textMuted }}>{p.carregamento?.produto?.nome} · {p.carregamento?.rota}</div>
                        </div>
                      </div>
                      <div className="flex flex-col gap-1" style={{ fontSize: 12 }}>
                        <div className="flex justify-between"><span style={{ color: C.textMuted }}>Quantidade vendida</span><span style={{ color: C.textDark }}>{p.quantidadeVendida}x</span></div>
                        <div className="flex justify-between"><span style={{ color: C.textMuted }}>Avaria</span><span style={{ color: C.amber }}>{p.quantidadeAvaria} ({moeda(p.valorAvaria)})</span></div>
                        <div className="flex justify-between"><span style={{ color: C.textMuted }}>Despesas</span><span style={{ color: C.amber }}>{moeda(p.totalDespesas)}</span></div>
                        <div className="flex justify-between pt-1" style={{ borderTop: `1px solid ${C.border}`, fontWeight: 600 }}>
                          <span style={{ color: C.textDark }}>Lucro</span><span style={{ color: C.textDark }}>{moeda(p.lucro)}</span>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            ))}
          </div>
        )}
      </Modal>

      {caminhoes.length === 0 ? (
        <EmptyState message="Nenhum caminhão cadastrado ainda." />
      ) : (
        <div className="grid grid-cols-2 gap-4">
          {caminhoes.map((cm) => (
            <Card key={cm.id} hover style={{ position: 'relative' }}>
              <div className="flex gap-1.5" style={{ position: 'absolute', top: 12, right: 12 }}>
                <button onClick={() => abrirHistorico(cm)} aria-label="Ver histórico" className="flex items-center justify-center" style={{ width: 28, height: 28, borderRadius: '50%', background: C.bg, color: C.textMuted }}>
                  <History size={13} />
                </button>
                <button onClick={() => abrirEdicao(cm)} aria-label="Editar caminhão" className="flex items-center justify-center" style={{ width: 28, height: 28, borderRadius: '50%', background: C.bg, color: C.textMuted }}>
                  <Pencil size={13} />
                </button>
              </div>
              <div className="flex items-start gap-3" style={{ paddingRight: 64 }}>
                <div className="flex items-center justify-center rounded-full" style={{ width: 40, height: 40, background: C.blueLight, flexShrink: 0 }}>
                  <Truck size={18} color={C.blue} />
                </div>
                <div>
                  <div style={{ fontSize: 15, fontWeight: 600, color: C.textDark, fontFamily: DISPLAY_FONT }}>{cm.placa}</div>
                  <div style={{ fontSize: 12, color: C.textMuted }}>{cm.cliente?.nome}</div>
                  {cm.motorista && (
                    <div className="flex items-center gap-1" style={{ fontSize: 12, color: C.textMuted }}>
                      <User size={12} /> {cm.motorista}
                    </div>
                  )}
                </div>
              </div>
              <div className="flex gap-2 mt-4">
                <button onClick={() => abrirModalCarregamento(cm)} className="px-3 py-1.5 rounded text-xs" style={{ background: C.bg, color: C.textDark, border: `1px solid ${C.border}` }}>Novo carregamento</button>
                <button onClick={() => abrirModalPrestacao(cm)} className="px-3 py-1.5 rounded text-xs font-medium" style={{ background: C.red, color: '#fff' }}>Prestação de contas</button>
              </div>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}
