import React, { useEffect, useState } from 'react';
import { FileText, Pencil, Trash2, CheckCircle2, XCircle, Download } from 'lucide-react';
import { SectionHeaderWithAction, Card, Field, TextInput, ErrorBanner, Loading, Badge, EmptyState, SearchableSelect } from '../../components/ui';
import Modal from '../../components/Modal';
import { C, DISPLAY_FONT } from '../../theme';
import { orcamentosTerceirosApi } from '../../api/terceiros/orcamentos';
import { clientesTerceirosApi } from '../../api/terceiros/clientes';
import { produtosTerceirosApi } from '../../api/terceiros/produtos';

const FORM_VAZIO = { clienteId: '', validoAte: '', observacoes: '' };

const moeda = (v) => (v ?? 0).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
const dataCurta = (iso) => new Date(iso).toLocaleDateString('pt-BR', { day: '2-digit', month: '2-digit', year: 'numeric' });

const STATUS_LABEL = { PENDENTE: 'Pendente', APROVADO: 'Aprovado', RECUSADO: 'Recusado' };
const STATUS_TONE = { PENDENTE: 'amber', APROVADO: 'blue', RECUSADO: 'red' };

export default function OrcamentosTerceiros() {
  const [orcamentos, setOrcamentos] = useState([]);
  const [clientes, setClientes] = useState([]);
  const [produtos, setProdutos] = useState([]);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState(null);
  const [salvando, setSalvando] = useState(false);

  const [modalAberto, setModalAberto] = useState(false);
  const [editandoId, setEditandoId] = useState(null);
  const [form, setForm] = useState(FORM_VAZIO);
  const [itens, setItens] = useState([{ produtoId: '', quantidade: 1 }]);

  const [paraExcluir, setParaExcluir] = useState(null);
  const [excluindo, setExcluindo] = useState(false);
  const [baixandoId, setBaixandoId] = useState(null);

  async function carregar() {
    setCarregando(true);
    try {
      const [o, c, p] = await Promise.all([orcamentosTerceirosApi.listar(), clientesTerceirosApi.listar(), produtosTerceirosApi.listar()]);
      setOrcamentos(o);
      setClientes(c);
      setProdutos(p);
    } catch (e) {
      setErro(e.message);
    }
    setCarregando(false);
  }

  useEffect(() => { carregar(); }, []);

  function produtoPorId(id) {
    return produtos.find((p) => String(p.id) === String(id));
  }

  const totalPreview = itens.reduce((soma, it) => {
    const produto = produtoPorId(it.produtoId);
    return produto ? soma + produto.preco * Number(it.quantidade || 0) : soma;
  }, 0);

  function abrirNovo() {
    setEditandoId(null);
    setForm(FORM_VAZIO);
    setItens([{ produtoId: '', quantidade: 1 }]);
    setModalAberto(true);
  }

  function abrirEdicao(orc) {
    setEditandoId(orc.id);
    setForm({
      clienteId: orc.cliente?.id || '',
      validoAte: orc.validoAte || '',
      observacoes: orc.observacoes || '',
    });
    setItens(orc.itens.map((i) => ({ produtoId: i.produto.id, quantidade: i.quantidade })));
    setModalAberto(true);
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

  async function salvar() {
    setSalvando(true);
    setErro(null);
    try {
      const dto = {
        clienteId: Number(form.clienteId),
        validoAte: form.validoAte || null,
        observacoes: form.observacoes || null,
        itens: itens.filter((it) => it.produtoId).map((it) => ({ produtoId: Number(it.produtoId), quantidade: Number(it.quantidade) })),
      };
      if (editandoId) {
        await orcamentosTerceirosApi.atualizar(editandoId, dto);
      } else {
        await orcamentosTerceirosApi.criar(dto);
      }
      setModalAberto(false);
      await carregar();
    } catch (e) {
      setErro(e.message);
    } finally {
      setSalvando(false);
    }
  }

  async function mudarStatus(orc, status) {
    setErro(null);
    try {
      await orcamentosTerceirosApi.atualizarStatus(orc.id, status);
      await carregar();
    } catch (e) {
      setErro(e.message);
    }
  }

  async function confirmarExclusao() {
    setExcluindo(true);
    setErro(null);
    try {
      await orcamentosTerceirosApi.excluir(paraExcluir.id);
      setParaExcluir(null);
      await carregar();
    } catch (e) {
      setErro(e.message);
    } finally {
      setExcluindo(false);
    }
  }

  async function baixarPdf(orc) {
    setErro(null);
    setBaixandoId(orc.id);
    try {
      await orcamentosTerceirosApi.baixarPdf(orc.id);
    } catch (e) {
      setErro(e.message);
    } finally {
      setBaixandoId(null);
    }
  }

  if (carregando) return <Loading />;

  return (
    <div>
      <SectionHeaderWithAction title="Orçamentos" subtitle="Propostas para empresas terceiras, com exportação em PDF" actionLabel="+ Novo orçamento" onAction={abrirNovo} />

      <Modal open={modalAberto} title={editandoId ? 'Editar orçamento' : 'Novo orçamento'} onClose={() => setModalAberto(false)} maxWidth={620}>
        <ErrorBanner message={erro} />

        <Field label="Cliente">
          <SearchableSelect
            options={clientes.map((c) => ({ value: c.id, label: c.nome }))}
            value={form.clienteId}
            onChange={(v) => setForm({ ...form, clienteId: v })}
            emptyLabel="Selecione"
            placeholder="Buscar cliente por nome..."
          />
        </Field>

        <div className="mt-4 flex flex-col gap-2">
          <div style={{ fontSize: 12, color: C.textMuted, marginBottom: 2 }}>Itens do orçamento</div>
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

        <div className="grid grid-cols-2 gap-4 mt-4">
          <Field label="Válido até (opcional)"><TextInput type="date" value={form.validoAte} onChange={(e) => setForm({ ...form, validoAte: e.target.value })} /></Field>
          <div className="rounded-lg p-3 flex flex-col justify-center" style={{ background: C.blueLight }}>
            <div style={{ fontSize: 11, color: C.ink }}>Valor total</div>
            <div style={{ fontSize: 18, fontWeight: 600, color: C.ink, fontFamily: DISPLAY_FONT }}>{moeda(totalPreview)}</div>
          </div>
        </div>

        <div className="mt-4">
          <Field label="Observações (opcional)">
            <textarea
              className="w-full px-3 py-2 rounded border"
              style={{ borderColor: C.border, fontSize: 14, minHeight: 70, fontFamily: 'inherit' }}
              value={form.observacoes}
              onChange={(e) => setForm({ ...form, observacoes: e.target.value })}
            />
          </Field>
        </div>

        <div className="flex gap-2 mt-6">
          <button type="button" disabled={salvando || !form.clienteId} onClick={salvar} className="px-4 py-2 rounded text-sm font-medium disabled:opacity-60" style={{ background: C.red, color: '#fff' }}>
            {salvando ? 'Salvando...' : 'Salvar'}
          </button>
          <button type="button" onClick={() => setModalAberto(false)} className="px-4 py-2 rounded text-sm" style={{ background: C.bg, color: C.textDark }}>Cancelar</button>
        </div>
      </Modal>

      <Modal open={!!paraExcluir} title="Excluir orçamento" onClose={() => setParaExcluir(null)} maxWidth={400}>
        <ErrorBanner message={erro} />
        <p style={{ fontSize: 14, color: C.textDark }}>Tem certeza que deseja excluir o orçamento #{paraExcluir?.id}?</p>
        <div className="flex gap-2 mt-6">
          <button type="button" disabled={excluindo} onClick={confirmarExclusao} className="px-4 py-2 rounded text-sm font-medium disabled:opacity-60" style={{ background: '#B3261E', color: '#fff' }}>
            {excluindo ? 'Excluindo...' : 'Excluir'}
          </button>
          <button type="button" onClick={() => setParaExcluir(null)} className="px-4 py-2 rounded text-sm" style={{ background: C.bg, color: C.textDark }}>Cancelar</button>
        </div>
      </Modal>

      {!modalAberto && !paraExcluir && <ErrorBanner message={erro} />}

      {orcamentos.length === 0 ? (
        <EmptyState message="Nenhum orçamento criado ainda." />
      ) : (
        <div className="grid grid-cols-2 gap-4">
          {orcamentos.map((orc) => (
            <Card key={orc.id} hover style={{ position: 'relative' }}>
              <div className="flex gap-1.5" style={{ position: 'absolute', top: 12, right: 12 }}>
                <button onClick={() => baixarPdf(orc)} disabled={baixandoId === orc.id} aria-label="Baixar PDF" className="flex items-center justify-center" style={{ width: 28, height: 28, borderRadius: '50%', background: C.bg, color: C.textMuted }}>
                  <Download size={13} />
                </button>
                <button onClick={() => abrirEdicao(orc)} aria-label="Editar orçamento" className="flex items-center justify-center" style={{ width: 28, height: 28, borderRadius: '50%', background: C.bg, color: C.textMuted }}>
                  <Pencil size={13} />
                </button>
                <button onClick={() => setParaExcluir(orc)} aria-label="Excluir orçamento" className="flex items-center justify-center" style={{ width: 28, height: 28, borderRadius: '50%', background: C.bg, color: '#B3261E' }}>
                  <Trash2 size={13} />
                </button>
              </div>

              <div className="flex items-start gap-3" style={{ paddingRight: 92 }}>
                <div className="flex items-center justify-center rounded-full" style={{ width: 40, height: 40, background: C.blueLight, flexShrink: 0 }}>
                  <FileText size={18} color={C.blue} />
                </div>
                <div className="min-w-0">
                  <div style={{ fontSize: 14, fontWeight: 600, color: C.textDark }} className="truncate">{orc.cliente?.nome}</div>
                  <div style={{ fontSize: 12, color: C.textMuted }}>
                    #{orc.id} · {dataCurta(orc.dataCriacao)}{orc.validoAte ? ` · válido até ${dataCurta(orc.validoAte)}` : ''}
                  </div>
                </div>
              </div>

              <div className="flex items-center justify-between mt-3">
                <Badge tone={STATUS_TONE[orc.status]}>{STATUS_LABEL[orc.status]}</Badge>
                <div style={{ fontSize: 18, fontWeight: 600, color: C.textDark, fontFamily: DISPLAY_FONT }}>{moeda(orc.valorTotal)}</div>
              </div>

              {orc.status === 'PENDENTE' && (
                <div className="flex gap-2 mt-3">
                  <button onClick={() => mudarStatus(orc, 'APROVADO')} className="flex items-center gap-1 px-3 py-1.5 rounded text-xs" style={{ background: C.blueLight, color: C.ink }}>
                    <CheckCircle2 size={13} /> Aprovar
                  </button>
                  <button onClick={() => mudarStatus(orc, 'RECUSADO')} className="flex items-center gap-1 px-3 py-1.5 rounded text-xs" style={{ background: C.bg, color: C.textMuted, border: `1px solid ${C.border}` }}>
                    <XCircle size={13} /> Recusar
                  </button>
                </div>
              )}
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}
