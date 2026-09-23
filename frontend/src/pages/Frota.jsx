import React, { useEffect, useState } from 'react';
import { Truck, User, Pencil, History, CalendarDays } from 'lucide-react';
import { Card, Field, TextInput, Segmented, ErrorBanner, Loading, Badge, EmptyState, MoneyInput } from '../components/ui';
import Modal from '../components/Modal';
import { C, DISPLAY_FONT } from '../theme';
import { frotaApi } from '../api/frota';
import { clientesApi } from '../api/clientes';

const CAMINHAO_VAZIO = { placa: '', nomeMotorista: '', telefoneMotorista: '', tipoRota: 'FIXA', clienteIdsRotaFixa: [] };
const CARREGAMENTO_VAZIO = { rota: '', quantidadeCarregada: '', precoVenda: '' };
const PRESTACAO_VAZIA = { quantidadeAvaria: '0', quantidadeDevolvida: '0', valorRecebidoEspecie: '', valorRecebidoPix: '' };
const DESPESA_VAZIA = { descricao: '', valor: '' };

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

export default function Frota() {
  const [caminhoes, setCaminhoes] = useState([]);
  const [clientes, setClientes] = useState([]);
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
  const [novaDespesa, setNovaDespesa] = useState(DESPESA_VAZIA);
  const [salvandoDespesa, setSalvandoDespesa] = useState(false);

  const [caminhaoHistorico, setCaminhaoHistorico] = useState(null);
  const [historico, setHistorico] = useState([]);
  const [carregandoHistorico, setCarregandoHistorico] = useState(false);
  const [expandidoId, setExpandidoId] = useState(null);
  const [prestacaoExpandida, setPrestacaoExpandida] = useState(null);

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
      const [cam, cli] = await Promise.all([frotaApi.listarCaminhoes(), clientesApi.listar()]);
      setCaminhoes(cam);
      setClientes(cli);
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

  async function abrirEdicao(caminhao) {
    setErro(null);
    setEditandoId(caminhao.id);
    let clienteIdsRotaFixa = [];
    if (caminhao.tipoRota === 'FIXA') {
      try {
        const clientesRota = await frotaApi.clientesRotaFixa(caminhao.id);
        clienteIdsRotaFixa = clientesRota.map((c) => c.id);
      } catch (e) {
        setErro(e.message);
      }
    }
    setForm({
      placa: caminhao.placa,
      nomeMotorista: caminhao.motorista?.nome || '',
      telefoneMotorista: caminhao.motorista?.telefone || '',
      tipoRota: caminhao.tipoRota,
      clienteIdsRotaFixa,
    });
    setModalCaminhao(true);
  }

  async function salvarCaminhao() {
    setSalvando(true);
    setErro(null);
    try {
      if (editandoId) {
        await frotaApi.atualizarCaminhao(editandoId, form);
      } else {
        await frotaApi.criarCaminhao(form);
      }
      setModalCaminhao(false);
      await carregar();
    } catch (e) {
      setErro(e.message);
    } finally {
      setSalvando(false);
    }
  }

  function toggleClienteRota(id) {
    setForm((f) => ({
      ...f,
      clienteIdsRotaFixa: f.clienteIdsRotaFixa.includes(id)
        ? f.clienteIdsRotaFixa.filter((c) => c !== id)
        : [...f.clienteIdsRotaFixa, id],
    }));
  }

  function abrirModalCarregamento(caminhao) {
    setErro(null);
    setCaminhaoCarregamento(caminhao);
    setCarregamentoForm(CARREGAMENTO_VAZIO);
  }

  async function salvarCarregamento() {
    setSalvando(true);
    setErro(null);
    try {
      await frotaApi.abrirCarregamento({
        caminhaoId: caminhaoCarregamento.id,
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
    setNovaDespesa(DESPESA_VAZIA);
    try {
      const carregamentos = await frotaApi.historicoCarregamentos(caminhao.id);
      const pendente = carregamentos.find((c) => c.status === 'PENDENTE');
      if (pendente) {
        setCarregamentoAtivo(pendente);
        setDespesasCarregamento(await frotaApi.listarDespesasCarregamento(pendente.id));
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
      await frotaApi.registrarDespesaCarregamento(carregamentoAtivo.id, {
        descricao: novaDespesa.descricao,
        valor: Number(novaDespesa.valor),
      });
      setNovaDespesa(DESPESA_VAZIA);
      setDespesasCarregamento(await frotaApi.listarDespesasCarregamento(carregamentoAtivo.id));
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
      await frotaApi.registrarPrestacaoContas({
        carregamentoId: carregamentoAtivo.id,
        quantidadeAvaria: Number(prestacaoForm.quantidadeAvaria),
        quantidadeDevolvida: Number(prestacaoForm.quantidadeDevolvida),
        valorRecebidoEspecie: Number(prestacaoForm.valorRecebidoEspecie || 0),
        valorRecebidoPix: Number(prestacaoForm.valorRecebidoPix || 0),
      });
      setCaminhaoPrestacao(null);
      setCarregamentoAtivo(null);
      setDespesasCarregamento([]);
    } catch (e) {
      setErro(e.message);
    } finally {
      setSalvando(false);
    }
  }

  async function abrirHistorico(caminhao) {
    setErro(null);
    setCaminhaoHistorico(caminhao);
    setExpandidoId(null);
    setPrestacaoExpandida(null);
    setCarregandoHistorico(true);
    try {
      setHistorico(await frotaApi.historicoCarregamentos(caminhao.id));
    } catch (e) {
      setErro(e.message);
    } finally {
      setCarregandoHistorico(false);
    }
  }

  async function alternarExpandido(c) {
    if (expandidoId === c.id) {
      setExpandidoId(null);
      setPrestacaoExpandida(null);
      return;
    }
    setExpandidoId(c.id);
    setPrestacaoExpandida(null);
    if (c.status === 'PRESTADO') {
      try {
        setPrestacaoExpandida(await frotaApi.prestacaoPorCarregamento(c.id));
      } catch (e) {
        setErro(e.message);
      }
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
      setPrestacoesPeriodo(await frotaApi.listarPrestacoes(inicioPeriodo, fimPeriodo, caminhaoFiltro || undefined));
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
      await frotaApi.baixarPdfPrestacoes(inicioPeriodo, fimPeriodo, caminhaoFiltro || undefined);
    } catch (e) {
      setErro(e.message);
    } finally {
      setBaixandoPdfPrestacoes(false);
    }
  }

  if (carregando) return <Loading />;

  const algumModalAberto = modalCaminhao || caminhaoCarregamento || caminhaoPrestacao || caminhaoHistorico || modalPrestacoes;

  const quantidadeVendidaPreview = carregamentoAtivo
    ? Math.max(carregamentoAtivo.quantidadeCarregada - Number(prestacaoForm.quantidadeAvaria || 0) - Number(prestacaoForm.quantidadeDevolvida || 0), 0)
    : 0;
  const valorVendaPreview = carregamentoAtivo ? quantidadeVendidaPreview * carregamentoAtivo.precoVenda : 0;
  const totalDespesasPreview = despesasCarregamento.reduce((s, d) => s + d.valor, 0);
  const lucroPreview = valorVendaPreview - totalDespesasPreview;

  return (
    <div>
      <div className="flex items-start justify-between mb-6">
        <div>
          <h2 style={{ fontFamily: DISPLAY_FONT, fontSize: 22, fontWeight: 500, color: C.onDark }}>Frota</h2>
        </div>
        <div className="flex gap-2" style={{ flexShrink: 0 }}>
          <button onClick={abrirPrestacoesPeriodo} className="flex items-center gap-1.5 px-4 py-2 rounded text-sm" style={{ background: C.bg, color: C.textDark, border: `1px solid ${C.border}` }}>
            <CalendarDays size={14} /> Prestações por data
          </button>
          <button onClick={abrirNovo} className="px-4 py-2 rounded text-sm font-medium" style={{ background: C.red, color: '#fff' }}>+ Novo caminhão</button>
        </div>
      </div>
      {!algumModalAberto && <ErrorBanner message={erro} />}

      {/* ---- Modal: novo/editar caminhao ---- */}
      <Modal open={modalCaminhao} title={editandoId ? 'Editar caminhão' : 'Novo caminhão'} onClose={() => setModalCaminhao(false)} maxWidth={560}>
        <ErrorBanner message={erro} />
        <div className="grid grid-cols-2 gap-4">
          <Field label="Placa"><TextInput value={form.placa} onChange={(e) => setForm({ ...form, placa: e.target.value })} /></Field>
          <Field label="Tipo de rota">
            <Segmented options={[['FIXA', 'Fixa'], ['VARIAVEL', 'Variável']]} value={form.tipoRota} onChange={(v) => setForm({ ...form, tipoRota: v })} />
          </Field>
          <Field label="Nome do motorista"><TextInput value={form.nomeMotorista} onChange={(e) => setForm({ ...form, nomeMotorista: e.target.value })} /></Field>
          <Field label="Telefone do motorista"><TextInput value={form.telefoneMotorista} onChange={(e) => setForm({ ...form, telefoneMotorista: e.target.value })} /></Field>
        </div>
        {form.tipoRota === 'FIXA' && (
          <div className="mt-3">
            <Field label="Clientes atendidos nesta rota">
              <div className="flex gap-2 flex-wrap">
                {clientes.map((c) => (
                  <button
                    type="button"
                    key={c.id}
                    onClick={() => toggleClienteRota(c.id)}
                    style={{
                      fontSize: 12,
                      background: form.clienteIdsRotaFixa.includes(c.id) ? C.blueLight : C.bg,
                      color: form.clienteIdsRotaFixa.includes(c.id) ? C.ink : C.textMuted,
                      border: `1px solid ${form.clienteIdsRotaFixa.includes(c.id) ? C.blue : C.border}`,
                      padding: '4px 10px', borderRadius: 999,
                    }}
                  >
                    {c.nome}
                  </button>
                ))}
              </div>
            </Field>
          </div>
        )}
        <div className="flex gap-2 mt-6">
          <button type="button" disabled={salvando} onClick={salvarCaminhao} className="px-4 py-2 rounded text-sm font-medium disabled:opacity-60" style={{ background: C.red, color: '#fff' }}>
            {salvando ? 'Salvando...' : 'Salvar'}
          </button>
          <button type="button" onClick={() => setModalCaminhao(false)} className="px-4 py-2 rounded text-sm" style={{ background: C.bg, color: C.textDark }}>Cancelar</button>
        </div>
      </Modal>

      {/* ---- Modal: novo carregamento ---- */}
      <Modal open={!!caminhaoCarregamento} title={`Novo carregamento — ${caminhaoCarregamento?.placa || ''}`} onClose={() => setCaminhaoCarregamento(null)}>
        <ErrorBanner message={erro} />
        <div className="grid grid-cols-2 gap-4">
          <Field label="Rota"><TextInput placeholder="Ex: Zona Norte" value={carregamentoForm.rota} onChange={(e) => setCarregamentoForm({ ...carregamentoForm, rota: e.target.value })} /></Field>
          <Field label="Quantidade carregada"><TextInput type="number" value={carregamentoForm.quantidadeCarregada} onChange={(e) => setCarregamentoForm({ ...carregamentoForm, quantidadeCarregada: e.target.value })} /></Field>
        </div>
        <div className="mt-3">
          <Field label="Valor de venda por galão (para o caminhão)">
            <MoneyInput style={{ maxWidth: 180 }} value={carregamentoForm.precoVenda} onChange={(v) => setCarregamentoForm({ ...carregamentoForm, precoVenda: v })} />
          </Field>
          <div style={{ fontSize: 11, color: C.textMuted, marginTop: 4 }}>
            O caminhão compra como um cliente final — esse valor pode ser diferente do preço de envase praticado no PDV.
          </div>
        </div>
        <div className="flex gap-2 mt-6">
          <button type="button" disabled={salvando} onClick={salvarCarregamento} className="px-4 py-2 rounded text-sm font-medium disabled:opacity-60" style={{ background: C.red, color: '#fff' }}>
            {salvando ? 'Salvando...' : 'Salvar carregamento'}
          </button>
          <button type="button" onClick={() => setCaminhaoCarregamento(null)} className="px-4 py-2 rounded text-sm" style={{ background: C.bg, color: C.textDark }}>Cancelar</button>
        </div>
      </Modal>

      {/* ---- Modal: prestacao de contas ---- */}
      <Modal open={!!caminhaoPrestacao} title={`Prestação de contas — ${caminhaoPrestacao?.placa || ''}`} onClose={() => setCaminhaoPrestacao(null)} maxWidth={560}>
        <ErrorBanner message={erro} />
        {carregamentoAtivo && (
          <>
            <div className="grid grid-cols-2 gap-3 mb-4">
              <div className="rounded-lg p-3" style={{ background: C.bg }}>
                <div style={{ fontSize: 11, color: C.textMuted }}>Carregado em {dataCurta(carregamentoAtivo.dataCarregamento)}</div>
                <div style={{ fontSize: 16, fontWeight: 600, color: C.textDark, fontFamily: DISPLAY_FONT }}>{carregamentoAtivo.quantidadeCarregada} galões</div>
                <div style={{ fontSize: 11, color: C.textMuted }}>Rota: {carregamentoAtivo.rota}</div>
              </div>
              <div className="rounded-lg p-3" style={{ background: C.bg }}>
                <div style={{ fontSize: 11, color: C.textMuted }}>Valor total da venda ao caminhão</div>
                <div style={{ fontSize: 16, fontWeight: 600, color: C.textDark, fontFamily: DISPLAY_FONT }}>{moeda(carregamentoAtivo.valorCarregamento)}</div>
                <div style={{ fontSize: 11, color: C.textMuted }}>{moeda(carregamentoAtivo.precoVenda)} por galão</div>
              </div>
            </div>
            <div className="grid grid-cols-2 gap-4">
              <Field label="Quantidade de avaria"><TextInput type="number" value={prestacaoForm.quantidadeAvaria} onChange={(e) => setPrestacaoForm({ ...prestacaoForm, quantidadeAvaria: e.target.value })} /></Field>
              <Field label="Galões devolvidos"><TextInput type="number" value={prestacaoForm.quantidadeDevolvida} onChange={(e) => setPrestacaoForm({ ...prestacaoForm, quantidadeDevolvida: e.target.value })} /></Field>
              <Field label="Recebido em espécie"><MoneyInput value={prestacaoForm.valorRecebidoEspecie} onChange={(v) => setPrestacaoForm({ ...prestacaoForm, valorRecebidoEspecie: v })} /></Field>
              <Field label="Recebido em PIX"><MoneyInput value={prestacaoForm.valorRecebidoPix} onChange={(v) => setPrestacaoForm({ ...prestacaoForm, valorRecebidoPix: v })} /></Field>
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
                  <Field label="Valor"><MoneyInput value={novaDespesa.valor} onChange={(v) => setNovaDespesa({ ...novaDespesa, valor: v })} /></Field>
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
              <div className="flex justify-between"><span>Quantidade vendida</span><span>{quantidadeVendidaPreview} galões ({moeda(valorVendaPreview)})</span></div>
              <div className="flex justify-between"><span>Despesas do carregamento</span><span>− {moeda(totalDespesasPreview)}</span></div>
              <div className="flex justify-between pt-1" style={{ borderTop: '1px solid rgba(21,49,107,0.15)', fontWeight: 600 }}><span>Lucro do caminhão</span><span>{moeda(lucroPreview)}</span></div>
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

      {/* ---- Modal: historico de carregamentos ---- */}
      <Modal open={!!caminhaoHistorico} title={`Histórico — ${caminhaoHistorico?.placa || ''}`} onClose={() => setCaminhaoHistorico(null)} maxWidth={640}>
        <ErrorBanner message={erro} />
        {carregandoHistorico ? (
          <div style={{ fontSize: 13, color: C.textMuted }}>Carregando...</div>
        ) : historico.length === 0 ? (
          <EmptyState message="Nenhum carregamento registrado ainda para este caminhão." />
        ) : (
          <div className="flex flex-col gap-2" style={{ maxHeight: 440, overflowY: 'auto', paddingRight: 4 }}>
            {historico.map((c) => (
              <div key={c.id} className="rounded-lg" style={{ border: `1px solid ${C.border}` }}>
                <button onClick={() => alternarExpandido(c)} className="w-full flex items-center justify-between px-3 py-2.5 text-left">
                  <div>
                    <div style={{ fontSize: 13, fontWeight: 500, color: C.textDark }}>{dataCurta(c.dataCarregamento)} · {c.rota}</div>
                    <div style={{ fontSize: 11, color: C.textMuted }}>{c.quantidadeCarregada} galões · {moeda(c.valorCarregamento)}</div>
                  </div>
                  <Badge tone={c.status === 'PENDENTE' ? 'amber' : 'blue'}>{c.status === 'PENDENTE' ? 'Pendente' : 'Prestado'}</Badge>
                </button>
                {expandidoId === c.id && (
                  <div className="px-3 pb-3" style={{ borderTop: `1px solid ${C.border}` }}>
                    {c.status === 'PENDENTE' ? (
                      <div style={{ fontSize: 12, color: C.textMuted, paddingTop: 10 }}>Ainda sem prestação de contas.</div>
                    ) : !prestacaoExpandida ? (
                      <div style={{ fontSize: 12, color: C.textMuted, paddingTop: 10 }}>Carregando...</div>
                    ) : (
                      <div className="flex flex-col gap-1.5 pt-3" style={{ fontSize: 12 }}>
                        <div className="flex justify-between"><span style={{ color: C.textMuted }}>Quantidade de avaria</span><span style={{ color: C.amber }}>{prestacaoExpandida.quantidadeAvaria} ({moeda(prestacaoExpandida.valorAvaria)})</span></div>
                        <div className="flex justify-between"><span style={{ color: C.textMuted }}>Galões devolvidos</span><span style={{ color: C.textDark }}>{prestacaoExpandida.quantidadeDevolvida}</span></div>
                        <div className="flex justify-between"><span style={{ color: C.textMuted }}>Quantidade vendida</span><span style={{ color: C.textDark }}>{prestacaoExpandida.quantidadeVendida}</span></div>
                        <div className="flex justify-between"><span style={{ color: C.textMuted }}>Recebido (espécie + PIX)</span><span style={{ color: C.textDark }}>{moeda(prestacaoExpandida.valorRecebidoEspecie + prestacaoExpandida.valorRecebidoPix)}</span></div>
                        <div className="flex justify-between"><span style={{ color: C.textMuted }}>Despesas do carregamento</span><span style={{ color: C.amber }}>{moeda(prestacaoExpandida.totalDespesas)}</span></div>
                        <div className="flex justify-between pt-1.5" style={{ borderTop: `1px solid ${C.border}`, fontWeight: 500 }}>
                          <span style={{ color: C.textDark }}>Lucro do caminhão</span>
                          <span style={{ color: C.textDark }}>{moeda(prestacaoExpandida.lucro)}</span>
                        </div>
                        <div className="flex justify-between">
                          <span style={{ color: C.textMuted }}>Diferença (conferência de caixa)</span>
                          <span style={{ color: Math.abs(prestacaoExpandida.diferenca) < 0.01 ? C.textDark : C.red }}>{moeda(prestacaoExpandida.diferenca)}</span>
                        </div>
                      </div>
                    )}
                  </div>
                )}
              </div>
            ))}
          </div>
        )}
      </Modal>

      {/* ---- Modal: prestacoes por data (todos os caminhoes) ---- */}
      <Modal open={modalPrestacoes} title="Prestações por data" onClose={() => setModalPrestacoes(false)} maxWidth={680}>
        <ErrorBanner message={erro} />
        <div className="flex gap-3 items-end flex-wrap mb-4">
          <Field label="De"><TextInput type="date" value={inicioPeriodo} onChange={(e) => setInicioPeriodo(e.target.value)} style={{ width: 150 }} /></Field>
          <Field label="Até"><TextInput type="date" value={fimPeriodo} onChange={(e) => setFimPeriodo(e.target.value)} style={{ width: 150 }} /></Field>
          <Field label="Caminhão">
            <select
              className="px-3 py-2 rounded border"
              style={{ borderColor: C.border, fontSize: 14, width: 160 }}
              value={caminhaoFiltro}
              onChange={(e) => setCaminhaoFiltro(e.target.value)}
            >
              <option value="">Todos</option>
              {caminhoes.map((cm) => <option key={cm.id} value={cm.id}>{cm.placa}</option>)}
            </select>
          </Field>
          <button onClick={carregarPrestacoesPeriodo} className="px-4 py-2 rounded text-sm font-medium" style={{ background: C.ink, color: '#fff' }}>Buscar</button>
          <button
            onClick={baixarPdfPrestacoes}
            disabled={baixandoPdfPrestacoes}
            className="px-4 py-2 rounded text-sm font-medium disabled:opacity-60"
            style={{ background: C.red, color: '#fff' }}
          >
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
                          <div style={{ fontSize: 11, color: C.textMuted }}>{p.carregamento?.rota}</div>
                        </div>
                        <Badge tone={Math.abs(p.diferenca) < 0.01 ? 'blue' : 'red'}>{Math.abs(p.diferenca) < 0.01 ? 'Confere' : 'Diferença'}</Badge>
                      </div>
                      <div className="flex flex-col gap-1" style={{ fontSize: 12 }}>
                        <div className="flex justify-between"><span style={{ color: C.textMuted }}>Quantidade vendida</span><span style={{ color: C.textDark }}>{p.quantidadeVendida} galões</span></div>
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

      {/* ---- Grid de caminhoes ---- */}
      {caminhoes.length === 0 ? (
        <EmptyState message="Nenhum caminhão cadastrado ainda." />
      ) : (
        <div className="grid grid-cols-2 gap-4">
          {caminhoes.map((cm) => (
            <Card key={cm.id} hover style={{ position: 'relative' }}>
              <div className="flex gap-1.5" style={{ position: 'absolute', top: 12, right: 12 }}>
                <button onClick={() => abrirHistorico(cm)} aria-label="Ver histórico de carregamentos" className="flex items-center justify-center" style={{ width: 28, height: 28, borderRadius: '50%', background: C.bg, color: C.textMuted }}>
                  <History size={13} />
                </button>
                <button onClick={() => abrirEdicao(cm)} aria-label="Editar caminhão" className="flex items-center justify-center" style={{ width: 28, height: 28, borderRadius: '50%', background: C.bg, color: C.textMuted }}>
                  <Pencil size={13} />
                </button>
              </div>
              <div className="flex items-start justify-between" style={{ paddingRight: 64 }}>
                <div className="flex items-center gap-3">
                  <div className="flex items-center justify-center rounded-full" style={{ width: 40, height: 40, background: C.blueLight, flexShrink: 0 }}>
                    <Truck size={18} color={C.blue} />
                  </div>
                  <div>
                    <div style={{ fontSize: 15, fontWeight: 600, color: C.textDark, fontFamily: DISPLAY_FONT }}>{cm.placa}</div>
                    <div className="flex items-center gap-1" style={{ fontSize: 12, color: C.textMuted }}>
                      <User size={12} /> {cm.motorista?.nome}
                    </div>
                  </div>
                </div>
              </div>
              <div className="mt-2">
                <Badge tone={cm.tipoRota === 'VARIAVEL' ? 'amber' : 'blue'}>Rota {cm.tipoRota === 'VARIAVEL' ? 'Variável' : 'Fixa'}</Badge>
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
