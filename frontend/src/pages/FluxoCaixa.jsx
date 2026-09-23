import React, { useEffect, useState } from 'react';
import { Landmark, Pencil, Wallet2, Plug } from 'lucide-react';
import { SectionHeaderWithAction, Card, Field, TextInput, Segmented, ErrorBanner, Loading, Badge, EmptyState, MoneyInput } from '../components/ui';
import Modal from '../components/Modal';
import { C, DISPLAY_FONT } from '../theme';
import { fluxoCaixaApi } from '../api/fluxoCaixa';

const CONTA_VAZIA = { apelido: '', banco: '', agencia: '', numeroConta: '', chavePix: '', saldoInicial: '0', ativa: true };
const LANCAMENTO_VAZIO = { tipo: 'ENTRADA', descricao: '', categoria: '', valor: '', data: new Date().toISOString().slice(0, 10) };

const moeda = (v) => (v ?? 0).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
const dataCurta = (iso) => new Date(iso + 'T00:00:00').toLocaleDateString('pt-BR', { day: '2-digit', month: '2-digit', year: 'numeric' });

export default function FluxoCaixa() {
  const [contas, setContas] = useState([]);
  const [saldos, setSaldos] = useState({}); // { [contaId]: SaldoContaBancariaDTO }
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState(null);
  const [salvando, setSalvando] = useState(false);

  const [modalConta, setModalConta] = useState(false);
  const [editandoId, setEditandoId] = useState(null);
  const [form, setForm] = useState(CONTA_VAZIA);

  const [contaDetalhe, setContaDetalhe] = useState(null);
  const [lancamentos, setLancamentos] = useState([]);
  const [carregandoLancamentos, setCarregandoLancamentos] = useState(false);
  const [novoLancamento, setNovoLancamento] = useState(LANCAMENTO_VAZIO);
  const [salvandoLancamento, setSalvandoLancamento] = useState(false);

  async function carregar() {
    setCarregando(true);
    try {
      const lista = await fluxoCaixaApi.listarContas(false);
      setContas(lista);
      const entradasSaldo = await Promise.all(lista.map((c) => fluxoCaixaApi.saldoConta(c.id)));
      const mapa = {};
      entradasSaldo.forEach((s) => { mapa[s.contaBancariaId] = s; });
      setSaldos(mapa);
    } catch (e) {
      setErro(e.message);
    }
    setCarregando(false);
  }

  useEffect(() => { carregar(); }, []);

  function abrirNovaConta() {
    setEditandoId(null);
    setForm(CONTA_VAZIA);
    setModalConta(true);
  }

  function abrirEdicaoConta(c) {
    setEditandoId(c.id);
    setForm({
      apelido: c.apelido, banco: c.banco || '', agencia: c.agencia || '',
      numeroConta: c.numeroConta || '', chavePix: c.chavePix || '',
      saldoInicial: String(c.saldoInicial ?? 0), ativa: c.ativa,
    });
    setModalConta(true);
  }

  async function salvarConta() {
    setSalvando(true);
    setErro(null);
    try {
      const dto = { ...form, saldoInicial: Number(form.saldoInicial || 0) };
      if (editandoId) {
        await fluxoCaixaApi.atualizarConta(editandoId, dto);
      } else {
        await fluxoCaixaApi.criarConta(dto);
      }
      setModalConta(false);
      await carregar();
    } catch (e) {
      setErro(e.message);
    } finally {
      setSalvando(false);
    }
  }

  async function abrirDetalhe(conta) {
    setErro(null);
    setContaDetalhe(conta);
    setNovoLancamento(LANCAMENTO_VAZIO);
    setCarregandoLancamentos(true);
    try {
      setLancamentos(await fluxoCaixaApi.listarLancamentos(conta.id));
    } catch (e) {
      setErro(e.message);
    } finally {
      setCarregandoLancamentos(false);
    }
  }

  async function salvarLancamento() {
    setSalvandoLancamento(true);
    setErro(null);
    try {
      await fluxoCaixaApi.registrarLancamento({
        contaBancariaId: contaDetalhe.id,
        tipo: novoLancamento.tipo,
        descricao: novoLancamento.descricao,
        categoria: novoLancamento.categoria || null,
        valor: Number(novoLancamento.valor),
        data: novoLancamento.data,
      });
      setNovoLancamento(LANCAMENTO_VAZIO);
      setLancamentos(await fluxoCaixaApi.listarLancamentos(contaDetalhe.id));
      const saldo = await fluxoCaixaApi.saldoConta(contaDetalhe.id);
      setSaldos((prev) => ({ ...prev, [contaDetalhe.id]: saldo }));
    } catch (e) {
      setErro(e.message);
    } finally {
      setSalvandoLancamento(false);
    }
  }

  async function excluirLancamento(id) {
    setErro(null);
    try {
      await fluxoCaixaApi.excluirLancamento(id);
      setLancamentos(await fluxoCaixaApi.listarLancamentos(contaDetalhe.id));
      const saldo = await fluxoCaixaApi.saldoConta(contaDetalhe.id);
      setSaldos((prev) => ({ ...prev, [contaDetalhe.id]: saldo }));
    } catch (e) {
      setErro(e.message);
    }
  }

  if (carregando) return <Loading />;

  const totalGeral = Object.values(saldos).reduce((s, v) => s + (v?.saldoAtual || 0), 0);

  return (
    <div>
      <SectionHeaderWithAction title="Fluxo de Caixa" subtitle="Contas bancárias e movimentações da empresa" actionLabel="+ Nova conta" onAction={abrirNovaConta} />

      <Card style={{ marginBottom: 16 }}>
        <div style={{ fontSize: 12, color: C.textMuted }}>Saldo total (todas as contas)</div>
        <div style={{ fontSize: 24, fontWeight: 600, color: C.textDark, fontFamily: DISPLAY_FONT }}>{moeda(totalGeral)}</div>
      </Card>

      {/* ---- Modal: nova/editar conta ---- */}
      <Modal open={modalConta} title={editandoId ? 'Editar conta bancária' : 'Nova conta bancária'} onClose={() => setModalConta(false)} maxWidth={480}>
        <ErrorBanner message={erro} />
        <div className="grid grid-cols-2 gap-4">
          <Field label="Apelido"><TextInput placeholder="Ex: Conta principal" value={form.apelido} onChange={(e) => setForm({ ...form, apelido: e.target.value })} /></Field>
          <Field label="Banco"><TextInput value={form.banco} onChange={(e) => setForm({ ...form, banco: e.target.value })} /></Field>
          <Field label="Agência"><TextInput value={form.agencia} onChange={(e) => setForm({ ...form, agencia: e.target.value })} /></Field>
          <Field label="Número da conta"><TextInput value={form.numeroConta} onChange={(e) => setForm({ ...form, numeroConta: e.target.value })} /></Field>
          <Field label="Chave PIX (opcional)"><TextInput value={form.chavePix} onChange={(e) => setForm({ ...form, chavePix: e.target.value })} /></Field>
          <Field label="Saldo inicial"><MoneyInput value={form.saldoInicial} onChange={(v) => setForm({ ...form, saldoInicial: v })} /></Field>
        </div>
        <div className="flex gap-2 mt-6">
          <button type="button" disabled={salvando || !form.apelido} onClick={salvarConta} className="px-4 py-2 rounded text-sm font-medium disabled:opacity-60" style={{ background: C.red, color: '#fff' }}>
            {salvando ? 'Salvando...' : 'Salvar'}
          </button>
          <button type="button" onClick={() => setModalConta(false)} className="px-4 py-2 rounded text-sm" style={{ background: C.bg, color: C.textDark }}>Cancelar</button>
        </div>
      </Modal>

      {/* ---- Modal: detalhe da conta + lancamentos ---- */}
      <Modal open={!!contaDetalhe} title={contaDetalhe?.apelido || ''} onClose={() => setContaDetalhe(null)} maxWidth={620}>
        <ErrorBanner message={erro} />
        {contaDetalhe && (
          <>
            <div className="rounded-lg p-3 mb-4" style={{ background: C.blueLight }}>
              <div style={{ fontSize: 12, color: C.ink }}>Saldo atual</div>
              <div style={{ fontSize: 22, fontWeight: 600, color: C.ink, fontFamily: DISPLAY_FONT }}>{moeda(saldos[contaDetalhe.id]?.saldoAtual)}</div>
            </div>

            <div style={{ fontSize: 12, fontWeight: 500, color: C.textDark, marginBottom: 8 }}>Novo lançamento manual</div>
            <Segmented options={[['ENTRADA', 'Entrada'], ['SAIDA', 'Saída']]} value={novoLancamento.tipo} onChange={(v) => setNovoLancamento({ ...novoLancamento, tipo: v })} />
            <div className="grid grid-cols-2 gap-3 mt-3">
              <Field label="Descrição"><TextInput value={novoLancamento.descricao} onChange={(e) => setNovoLancamento({ ...novoLancamento, descricao: e.target.value })} /></Field>
              <Field label="Categoria (opcional)"><TextInput placeholder="Ex: Venda, Fornecedor..." value={novoLancamento.categoria} onChange={(e) => setNovoLancamento({ ...novoLancamento, categoria: e.target.value })} /></Field>
              <Field label="Valor"><MoneyInput value={novoLancamento.valor} onChange={(v) => setNovoLancamento({ ...novoLancamento, valor: v })} /></Field>
              <Field label="Data"><TextInput type="date" value={novoLancamento.data} onChange={(e) => setNovoLancamento({ ...novoLancamento, data: e.target.value })} /></Field>
            </div>
            <button
              type="button"
              disabled={salvandoLancamento || !novoLancamento.descricao || !novoLancamento.valor}
              onClick={salvarLancamento}
              className="mt-3 px-4 py-2 rounded text-sm font-medium disabled:opacity-60"
              style={{ background: C.red, color: '#fff' }}
            >
              {salvandoLancamento ? 'Salvando...' : '+ Adicionar lançamento'}
            </button>

            <div className="mt-5 pt-4" style={{ borderTop: `1px solid ${C.border}` }}>
              <div style={{ fontSize: 12, fontWeight: 500, color: C.textDark, marginBottom: 8 }}>Lançamentos</div>
              {carregandoLancamentos ? (
                <div style={{ fontSize: 13, color: C.textMuted }}>Carregando...</div>
              ) : lancamentos.length === 0 ? (
                <div style={{ fontSize: 13, color: C.textMuted }}>Nenhum lançamento registrado ainda nesta conta.</div>
              ) : (
                <div className="flex flex-col gap-1" style={{ maxHeight: 280, overflowY: 'auto' }}>
                  {lancamentos.map((l) => (
                    <div key={l.id} className="flex justify-between items-center py-2" style={{ borderBottom: `1px solid ${C.border}`, fontSize: 13 }}>
                      <div>
                        <div style={{ color: C.textDark }}>{l.descricao}{l.categoria ? ` · ${l.categoria}` : ''}</div>
                        <div style={{ fontSize: 11, color: C.textMuted }}>{dataCurta(l.data)}{l.origem === 'AUTOMATICO' ? ' · importado' : ''}</div>
                      </div>
                      <div className="flex items-center gap-2">
                        <span style={{ fontWeight: 600, color: l.tipo === 'ENTRADA' ? '#1E7A4C' : C.red }}>
                          {l.tipo === 'ENTRADA' ? '+ ' : '- '}{moeda(l.valor)}
                        </span>
                        {l.origem === 'MANUAL' && (
                          <button onClick={() => excluirLancamento(l.id)} className="text-xs" style={{ color: '#B3261E' }}>Excluir</button>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </>
        )}
      </Modal>

      {!modalConta && !contaDetalhe && <ErrorBanner message={erro} />}

      {contas.length === 0 ? (
        <EmptyState message="Nenhuma conta bancária cadastrada ainda." />
      ) : (
        <div className="grid grid-cols-3 gap-4">
          {contas.map((c) => (
            <div key={c.id} onClick={() => abrirDetalhe(c)} style={{ cursor: 'pointer' }}>
            <Card hover style={{ position: 'relative' }}>
              <button
                onClick={(e) => { e.stopPropagation(); abrirEdicaoConta(c); }}
                aria-label="Editar conta"
                className="flex items-center justify-center"
                style={{ position: 'absolute', top: 12, right: 12, width: 28, height: 28, borderRadius: '50%', background: C.bg, color: C.textMuted }}
              >
                <Pencil size={13} />
              </button>
              <div className="flex items-center gap-3" style={{ paddingRight: 32 }}>
                <div className="flex items-center justify-center rounded-full" style={{ width: 40, height: 40, background: C.blueLight, flexShrink: 0 }}>
                  <Landmark size={18} color={C.blue} />
                </div>
                <div className="min-w-0">
                  <div style={{ fontSize: 14, fontWeight: 600, color: C.textDark }} className="truncate">{c.apelido}</div>
                  <div style={{ fontSize: 12, color: C.textMuted }} className="truncate">{c.banco || 'Banco não informado'}</div>
                </div>
              </div>
              <div style={{ fontSize: 20, fontWeight: 600, color: C.textDark, fontFamily: DISPLAY_FONT, marginTop: 12 }}>
                {moeda(saldos[c.id]?.saldoAtual)}
              </div>
              <div className="flex items-center gap-1 mt-2">
                {c.conectadaAutomaticamente ? (
                  <Badge tone="blue"><Plug size={11} style={{ display: 'inline', marginRight: 3 }} />Conectada</Badge>
                ) : (
                  <Badge tone="neutral">Manual</Badge>
                )}
                {!c.ativa && <Badge tone="neutral">Inativa</Badge>}
              </div>
            </Card>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
