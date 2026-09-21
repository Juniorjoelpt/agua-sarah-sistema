import React, { useEffect, useState } from 'react';
import { Phone, MapPin, Pencil, History, AlertTriangle, Tag } from 'lucide-react';
import { SectionHeaderWithAction, Card, Field, TextInput, Segmented, ErrorBanner, Loading, Avatar, Badge, EmptyState } from '../components/ui';
import Modal from '../components/Modal';
import { C, DISPLAY_FONT } from '../theme';
import { clientesApi } from '../api/clientes';
import { vendasApi } from '../api/vendas';
import { contasReceberApi } from '../api/contasReceber';
import { produtosApi } from '../api/produtos';
import { precosClienteApi } from '../api/precosCliente';
import { rotuloPagamento } from '../utils/pagamento';

const VAZIO = { nome: '', tipo: 'REVENDEDOR', telefone: '', bairro: '', endereco: '', ativo: true };

const OCORRENCIA_LABEL = {
  NENHUMA: null,
  AVARIA_CLIENTE: 'Avaria - cliente',
  AVARIA_PRODUCAO: 'Avaria - produção (bonificada)',
};

function formatarDataHora(iso) {
  return new Date(iso).toLocaleString('pt-BR', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' });
}
function moeda(v) {
  return (v ?? 0).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
}

export default function Clientes() {
  const [clientes, setClientes] = useState([]);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState(null);
  const [salvando, setSalvando] = useState(false);
  const [modalAberto, setModalAberto] = useState(false);
  const [editandoId, setEditandoId] = useState(null);
  const [form, setForm] = useState(VAZIO);

  const [clienteHistorico, setClienteHistorico] = useState(null);
  const [vendas, setVendas] = useState([]);
  const [contasReceber, setContasReceber] = useState([]);
  const [carregandoHistorico, setCarregandoHistorico] = useState(false);
  const [erroHistorico, setErroHistorico] = useState(null);

  const [clientePrecos, setClientePrecos] = useState(null);
  const [produtos, setProdutos] = useState([]);
  const [precosForm, setPrecosForm] = useState({}); // { [produtoId]: '12.50' }
  const [carregandoPrecos, setCarregandoPrecos] = useState(false);
  const [salvandoPrecos, setSalvandoPrecos] = useState(false);

  async function carregar() {
    setCarregando(true);
    try {
      setClientes(await clientesApi.listar(false));
    } catch (e) {
      setErro(e.message);
    }
    setCarregando(false);
  }

  useEffect(() => { carregar(); }, []);

  function abrirNovo() {
    setEditandoId(null);
    setForm(VAZIO);
    setModalAberto(true);
  }

  function abrirEdicao(cliente) {
    setEditandoId(cliente.id);
    setForm({
      nome: cliente.nome,
      tipo: cliente.tipo,
      telefone: cliente.telefone || '',
      bairro: cliente.bairro || '',
      endereco: cliente.endereco || '',
      ativo: cliente.ativo,
    });
    setModalAberto(true);
  }

  async function salvar() {
    setSalvando(true);
    setErro(null);
    try {
      if (editandoId) {
        await clientesApi.atualizar(editandoId, form);
      } else {
        await clientesApi.criar(form);
      }
      setModalAberto(false);
      await carregar();
    } catch (e) {
      setErro(e.message);
    } finally {
      setSalvando(false);
    }
  }

  async function abrirHistorico(cliente) {
    setClienteHistorico(cliente);
    setVendas([]);
    setContasReceber([]);
    setErroHistorico(null);
    setCarregandoHistorico(true);
    try {
      const [v, cr] = await Promise.all([
        vendasApi.listarPorCliente(cliente.id),
        contasReceberApi.listar(cliente.id),
      ]);
      setVendas(v);
      setContasReceber(cr);
    } catch (e) {
      setErroHistorico(e.message);
    } finally {
      setCarregandoHistorico(false);
    }
  }

  async function abrirTabelaPrecos(cliente) {
    setErro(null);
    setClientePrecos(cliente);
    setCarregandoPrecos(true);
    try {
      const [prods, precos] = await Promise.all([
        produtos.length > 0 ? Promise.resolve(produtos) : produtosApi.listar(),
        precosClienteApi.listar(cliente.id),
      ]);
      if (produtos.length === 0) setProdutos(prods);
      const form = {};
      precos.forEach((p) => { form[p.produto.id] = String(p.preco); });
      setPrecosForm(form);
    } catch (e) {
      setErro(e.message);
    } finally {
      setCarregandoPrecos(false);
    }
  }

  async function salvarTabelaPrecos() {
    setSalvandoPrecos(true);
    setErro(null);
    try {
      const itens = Object.entries(precosForm)
        .filter(([, valor]) => valor !== '' && valor !== null && Number(valor) > 0)
        .map(([produtoId, valor]) => ({ produtoId: Number(produtoId), preco: Number(valor) }));
      await precosClienteApi.definirTabela(clientePrecos.id, itens);
      setClientePrecos(null);
    } catch (e) {
      setErro(e.message);
    } finally {
      setSalvandoPrecos(false);
    }
  }

  if (carregando) return <Loading />;

  const totalGasto = vendas.reduce((s, v) => s + v.valorTotal, 0);
  const totalGaloesBonificados = vendas.filter((v) => v.ocorrencia === 'AVARIA_PRODUCAO').reduce((s, v) => s + (v.quantidadeBonificados || 0), 0);
  const totalGaloesAvariaCliente = vendas.filter((v) => v.ocorrencia === 'AVARIA_CLIENTE').reduce((s, v) => s + (v.quantidadeAvarias || 0), 0);
  const saldoDevedor = contasReceber.filter((c) => c.status !== 'PAGA').reduce((s, c) => s + (c.valorOriginal - c.valorPago), 0);

  return (
    <div>
      <SectionHeaderWithAction
        title="Clientes"
        subtitle="Revendedores e consumidor final no mesmo cadastro"
        actionLabel="+ Novo cliente"
        onAction={abrirNovo}
      />

      {/* ---- Modal: novo/editar cliente ---- */}
      <Modal open={modalAberto} title={editandoId ? 'Editar cliente' : 'Novo cliente'} onClose={() => setModalAberto(false)} maxWidth={520}>
        <ErrorBanner message={erro} />
        <div className="grid grid-cols-2 gap-4">
          <Field label="Nome"><TextInput value={form.nome} onChange={(e) => setForm({ ...form, nome: e.target.value })} /></Field>
          <Field label="Tipo">
            <Segmented options={[['REVENDEDOR', 'Revendedor'], ['CONSUMIDOR_FINAL', 'Consumidor final']]} value={form.tipo} onChange={(v) => setForm({ ...form, tipo: v })} />
          </Field>
          <Field label="Telefone"><TextInput value={form.telefone} onChange={(e) => setForm({ ...form, telefone: e.target.value })} /></Field>
          <Field label="Bairro"><TextInput value={form.bairro} onChange={(e) => setForm({ ...form, bairro: e.target.value })} /></Field>
          <Field label="Endereço"><TextInput value={form.endereco} onChange={(e) => setForm({ ...form, endereco: e.target.value })} /></Field>
        </div>
        <div className="flex gap-2 mt-6">
          <button type="button" disabled={salvando} onClick={salvar} className="px-4 py-2 rounded text-sm font-medium disabled:opacity-60" style={{ background: C.red, color: '#fff' }}>
            {salvando ? 'Salvando...' : 'Salvar'}
          </button>
          <button type="button" onClick={() => setModalAberto(false)} className="px-4 py-2 rounded text-sm" style={{ background: C.bg, color: C.textDark }}>Cancelar</button>
        </div>
      </Modal>

      {/* ---- Modal: historico de compras e bonificacoes ---- */}
      <Modal
        open={!!clienteHistorico}
        title={`Histórico — ${clienteHistorico?.nome || ''}`}
        onClose={() => setClienteHistorico(null)}
        maxWidth={640}
      >
        <ErrorBanner message={erroHistorico} />
        {!carregandoHistorico && saldoDevedor > 0.01 && (
          <div className="flex justify-between items-center mb-4 px-3 py-2 rounded" style={{ background: C.redLight, color: '#7A1E17' }}>
            <span style={{ fontSize: 13, fontWeight: 500 }}>Saldo devedor (fiado)</span>
            <span style={{ fontSize: 16, fontWeight: 600, fontFamily: DISPLAY_FONT }}>{moeda(saldoDevedor)}</span>
          </div>
        )}
        {carregandoHistorico ? (
          <Loading />
        ) : vendas.length === 0 ? (
          <EmptyState message="Este cliente ainda não tem compras registradas." />
        ) : (
          <>
            <div className="grid grid-cols-3 gap-3 mb-4">
              <div className="rounded-lg p-3" style={{ background: C.bg }}>
                <div style={{ fontSize: 11, color: C.textMuted }}>Total gasto</div>
                <div style={{ fontSize: 18, fontWeight: 600, color: C.textDark, fontFamily: DISPLAY_FONT }}>{moeda(totalGasto)}</div>
              </div>
              <div className="rounded-lg p-3" style={{ background: C.amberLight }}>
                <div style={{ fontSize: 11, color: '#7A4A1F' }}>Galões bonificados</div>
                <div style={{ fontSize: 18, fontWeight: 600, color: '#7A4A1F', fontFamily: DISPLAY_FONT }}>{totalGaloesBonificados}</div>
              </div>
              <div className="rounded-lg p-3" style={{ background: C.bg }}>
                <div style={{ fontSize: 11, color: C.textMuted }}>Avarias do cliente</div>
                <div style={{ fontSize: 18, fontWeight: 600, color: C.textDark, fontFamily: DISPLAY_FONT }}>{totalGaloesAvariaCliente}</div>
              </div>
            </div>

            {/* lista com scroll proprio, para o modal nao tomar a tela toda */}
            <div className="flex flex-col gap-3" style={{ maxHeight: 420, overflowY: 'auto', paddingRight: 4 }}>
              {vendas.map((v) => (
                <div key={v.id} className="rounded-lg p-3" style={{ border: `1px solid ${C.border}` }}>
                  <div className="flex justify-between items-start mb-2">
                    <div style={{ fontSize: 12, color: C.textMuted }}>{formatarDataHora(v.dataHora)}</div>
                    <div className="text-right">
                      <div style={{ fontSize: 14, fontWeight: 600, color: C.textDark }}>{moeda(v.valorTotal)}</div>
                      {(v.valorBonificado > 0 || v.valorAvaria > 0) && (
                        <div style={{ fontSize: 11, color: C.textMuted }}>{moeda(v.valorBruto)} bruto</div>
                      )}
                    </div>
                  </div>
                  <div className="flex flex-col gap-1 mb-2">
                    {v.itens?.map((item) => (
                      <div key={item.id} className="flex justify-between" style={{ fontSize: 12, color: C.textDark }}>
                        <span>{item.quantidade}x {item.produto?.nome}</span>
                        <span style={{ color: C.textMuted }}>{moeda(item.subtotal)}</span>
                      </div>
                    ))}
                  </div>
                  <div className="flex items-center gap-2 flex-wrap">
                    <Badge tone="neutral">{rotuloPagamento(v)}</Badge>
                    {OCORRENCIA_LABEL[v.ocorrencia] && (
                      <Badge tone={v.ocorrencia === 'AVARIA_PRODUCAO' ? 'amber' : 'neutral'}>
                        {OCORRENCIA_LABEL[v.ocorrencia]}
                      </Badge>
                    )}
                  </div>
                  {v.ocorrencia === 'AVARIA_PRODUCAO' && (
                    <div className="flex items-center gap-2 mt-2 px-2 py-1.5 rounded" style={{ background: C.amberLight, color: '#7A4A1F', fontSize: 12 }}>
                      <AlertTriangle size={13} />
                      {v.quantidadeAvarias} galões avariados ({moeda(v.valorAvaria)}) · {v.quantidadeBonificados} bonificados ({moeda(v.valorBonificado)})
                    </div>
                  )}
                  {v.ocorrencia === 'AVARIA_CLIENTE' && (
                    <div className="flex items-center gap-2 mt-2 px-2 py-1.5 rounded" style={{ background: C.bg, color: C.textMuted, fontSize: 12 }}>
                      <AlertTriangle size={13} />
                      {v.quantidadeAvarias} galões com avaria causada pelo cliente ({moeda(v.valorAvaria)} descontado)
                    </div>
                  )}
                </div>
              ))}
            </div>
          </>
        )}
      </Modal>

      {!modalAberto && !clienteHistorico && !clientePrecos && <ErrorBanner message={erro} />}

      {/* ---- Modal: tabela de precos personalizados ---- */}
      <Modal
        open={!!clientePrecos}
        title={`Tabela de preços — ${clientePrecos?.nome || ''}`}
        onClose={() => setClientePrecos(null)}
        maxWidth={480}
      >
        <ErrorBanner message={erro} />
        <p style={{ fontSize: 12, color: C.textMuted, marginBottom: 12 }}>
          Deixe em branco para usar o preço padrão do produto. Só preencha os produtos que esse cliente compra por um preço diferente.
        </p>
        {carregandoPrecos ? (
          <Loading />
        ) : produtos.length === 0 ? (
          <EmptyState message="Nenhum produto cadastrado ainda." />
        ) : (
          <div className="flex flex-col gap-2" style={{ maxHeight: 360, overflowY: 'auto', paddingRight: 4 }}>
            {produtos.map((p) => (
              <div key={p.id} className="flex items-center justify-between gap-3">
                <div className="min-w-0">
                  <div style={{ fontSize: 13, color: C.textDark }} className="truncate">{p.nome}</div>
                  <div style={{ fontSize: 11, color: C.textMuted }}>Padrão: {moeda(p.preco)}</div>
                </div>
                <TextInput
                  type="number"
                  step="0.01"
                  placeholder="usar padrão"
                  style={{ width: 120, flexShrink: 0 }}
                  value={precosForm[p.id] ?? ''}
                  onChange={(e) => setPrecosForm({ ...precosForm, [p.id]: e.target.value })}
                />
              </div>
            ))}
          </div>
        )}
        <div className="flex gap-2 mt-6">
          <button type="button" disabled={salvandoPrecos || carregandoPrecos} onClick={salvarTabelaPrecos} className="px-4 py-2 rounded text-sm font-medium disabled:opacity-60" style={{ background: C.red, color: '#fff' }}>
            {salvandoPrecos ? 'Salvando...' : 'Salvar tabela'}
          </button>
          <button type="button" onClick={() => setClientePrecos(null)} className="px-4 py-2 rounded text-sm" style={{ background: C.bg, color: C.textDark }}>Cancelar</button>
        </div>
      </Modal>

      {clientes.length === 0 ? (
        <EmptyState message="Nenhum cliente cadastrado ainda." />
      ) : (
        <div className="grid grid-cols-3 gap-4">
          {clientes.map((c) => (
            <Card key={c.id} hover style={{ position: 'relative' }}>
              <div className="flex gap-1.5" style={{ position: 'absolute', top: 12, right: 12 }}>
                <button
                  onClick={() => abrirHistorico(c)}
                  aria-label="Ver histórico de compras"
                  className="flex items-center justify-center"
                  style={{ width: 28, height: 28, borderRadius: '50%', background: C.bg, color: C.textMuted }}
                >
                  <History size={13} />
                </button>
                <button
                  onClick={() => abrirTabelaPrecos(c)}
                  aria-label="Tabela de preços personalizados"
                  className="flex items-center justify-center"
                  style={{ width: 28, height: 28, borderRadius: '50%', background: C.bg, color: C.textMuted }}
                >
                  <Tag size={13} />
                </button>
                <button
                  onClick={() => abrirEdicao(c)}
                  aria-label="Editar cliente"
                  className="flex items-center justify-center"
                  style={{ width: 28, height: 28, borderRadius: '50%', background: C.bg, color: C.textMuted }}
                >
                  <Pencil size={13} />
                </button>
              </div>
              <div className="flex items-start gap-3">
                <Avatar name={c.nome} color={c.tipo === 'REVENDEDOR' ? C.blue : C.amber} />
                <div className="flex-1 min-w-0" style={{ paddingRight: 92 }}>
                  <div style={{ fontSize: 14, fontWeight: 600, color: C.textDark }} className="truncate">{c.nome}</div>
                  <Badge tone={c.tipo === 'REVENDEDOR' ? 'blue' : 'amber'}>{c.tipo === 'REVENDEDOR' ? 'Revendedor' : 'Consumidor final'}</Badge>
                </div>
              </div>
              <div className="flex flex-col gap-1 mt-3" style={{ fontSize: 12, color: C.textMuted }}>
                <div className="flex items-center gap-1.5"><Phone size={12} /> {c.telefone || 'Não informado'}</div>
                <div className="flex items-center gap-1.5"><MapPin size={12} /> {c.bairro || 'Não informado'}</div>
              </div>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}
