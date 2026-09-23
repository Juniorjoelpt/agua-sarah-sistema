import React, { useEffect, useState } from 'react';
import { AlertTriangle } from 'lucide-react';
import { SectionHeaderWithAction, Card, Field, TextInput, Segmented, ErrorBanner, Loading, Badge, EmptyState, Avatar, MoneyInput } from '../components/ui';
import Modal from '../components/Modal';
import { C, DISPLAY_FONT } from '../theme';
import { contasPagarApi } from '../api/contasPagar';
import { fornecedoresApi } from '../api/fornecedores';
import { caixaApi } from '../api/caixa';

const NOVA_CONTA_VAZIA = { fornecedorId: '', descricao: '', categoria: 'OUTROS', valorOriginal: '', dataVencimento: '', observacao: '' };
const CATEGORIA_LABEL = { FROTA: 'Frota', PRODUCAO: 'Produção', INSUMOS: 'Insumos', OUTROS: 'Outros' };

const moeda = (v) => (v ?? 0).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
const dataCurta = (iso) => new Date(iso + 'T00:00:00').toLocaleDateString('pt-BR', { day: '2-digit', month: '2-digit', year: 'numeric' });

const STATUS_LABEL = { ABERTA: 'Em aberto', PARCIAL: 'Parcial', PAGA: 'Paga' };

function hoje() {
  return new Date().toISOString().slice(0, 10);
}

function situacao(conta) {
  if (conta.status === 'PAGA') return { label: 'Paga', tone: 'blue' };
  const dias = (new Date(conta.dataVencimento + 'T00:00:00') - new Date(hoje() + 'T00:00:00')) / 86400000;
  if (dias < 0) return { label: 'Vencida', tone: 'red' };
  if (dias <= 3) return { label: 'Vence em breve', tone: 'amber' };
  return { label: STATUS_LABEL[conta.status], tone: 'neutral' };
}

export default function ContasPagar() {
  const [contas, setContas] = useState([]);
  const [fornecedores, setFornecedores] = useState([]);
  const [caixaAberto, setCaixaAberto] = useState(null);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState(null);
  const [somenteEmAberto, setSomenteEmAberto] = useState(true);
  const [salvando, setSalvando] = useState(false);

  const [modalNovaConta, setModalNovaConta] = useState(false);
  const [novaConta, setNovaConta] = useState(NOVA_CONTA_VAZIA);

  const [contaPagamento, setContaPagamento] = useState(null);
  const [valorPagamentoInput, setValorPagamentoInput] = useState('');
  const [saiuDoCaixa, setSaiuDoCaixa] = useState(false);
  const [salvandoPagamento, setSalvandoPagamento] = useState(false);

  const [paraExcluir, setParaExcluir] = useState(null);
  const [excluindo, setExcluindo] = useState(false);

  async function carregar() {
    setCarregando(true);
    try {
      const [c, f] = await Promise.all([
        contasPagarApi.listar(undefined, somenteEmAberto),
        fornecedoresApi.listar(),
      ]);
      setContas(c);
      setFornecedores(f);
      try {
        setCaixaAberto(await caixaApi.atual());
      } catch {
        setCaixaAberto(null);
      }
    } catch (e) {
      setErro(e.message);
    }
    setCarregando(false);
  }

  useEffect(() => { carregar(); }, [somenteEmAberto]);

  const totalEmAberto = contas
    .filter((c) => c.status !== 'PAGA')
    .reduce((soma, c) => soma + (c.valorOriginal - c.valorPago), 0);
  const totalVencidas = contas
    .filter((c) => c.status !== 'PAGA' && c.dataVencimento < hoje())
    .reduce((soma, c) => soma + (c.valorOriginal - c.valorPago), 0);

  function abrirNovaConta() {
    setNovaConta(NOVA_CONTA_VAZIA);
    setModalNovaConta(true);
  }

  async function salvarNovaConta() {
    setSalvando(true);
    setErro(null);
    try {
      await contasPagarApi.criar({
        fornecedorId: novaConta.fornecedorId ? Number(novaConta.fornecedorId) : null,
        descricao: novaConta.descricao,
        categoria: novaConta.categoria,
        valorOriginal: Number(novaConta.valorOriginal),
        dataVencimento: novaConta.dataVencimento,
        observacao: novaConta.observacao || null,
      });
      setModalNovaConta(false);
      await carregar();
    } catch (e) {
      setErro(e.message);
    } finally {
      setSalvando(false);
    }
  }

  function abrirPagamento(conta) {
    setErro(null);
    setContaPagamento(conta);
    setValorPagamentoInput('');
    setSaiuDoCaixa(false);
  }

  const saldoDevedorPagamento = contaPagamento ? contaPagamento.valorOriginal - contaPagamento.valorPago : 0;

  async function confirmarPagamento() {
    setSalvandoPagamento(true);
    setErro(null);
    try {
      await contasPagarApi.registrarPagamento(contaPagamento.id, {
        valor: Number(valorPagamentoInput || 0),
        caixaId: saiuDoCaixa && caixaAberto?.id ? caixaAberto.id : null,
      });
      setContaPagamento(null);
      await carregar();
    } catch (e) {
      setErro(e.message);
    } finally {
      setSalvandoPagamento(false);
    }
  }

  async function confirmarExclusao() {
    setExcluindo(true);
    setErro(null);
    try {
      await contasPagarApi.excluir(paraExcluir.id);
      setParaExcluir(null);
      await carregar();
    } catch (e) {
      setErro(e.message);
    } finally {
      setExcluindo(false);
    }
  }

  if (carregando) return <Loading />;

  return (
    <div>
      <SectionHeaderWithAction title="Contas a Pagar" subtitle="Boletos e obrigações com vencimento" actionLabel="+ Nova conta" onAction={abrirNovaConta} />

      <div className="grid grid-cols-3 gap-4 mb-4">
        <Card>
          <div style={{ fontSize: 12, color: C.textMuted }}>Total em aberto</div>
          <div style={{ fontSize: 20, fontWeight: 600, color: C.textDark, fontFamily: DISPLAY_FONT }}>{moeda(totalEmAberto)}</div>
        </Card>
        <Card style={totalVencidas > 0 ? { background: C.redLight } : {}}>
          <div style={{ fontSize: 12, color: totalVencidas > 0 ? '#7A1E17' : C.textMuted }}>Total vencido</div>
          <div style={{ fontSize: 20, fontWeight: 600, color: totalVencidas > 0 ? '#7A1E17' : C.textDark, fontFamily: DISPLAY_FONT }}>{moeda(totalVencidas)}</div>
        </Card>
        <Card>
          <div style={{ fontSize: 12, color: C.textMuted, marginBottom: 8 }}>Mostrar</div>
          <Segmented options={[['true', 'Em aberto'], ['false', 'Todas']]} value={String(somenteEmAberto)} onChange={(v) => setSomenteEmAberto(v === 'true')} />
        </Card>
      </div>

      {/* ---- Modal: nova conta ---- */}
      <Modal open={modalNovaConta} title="Nova conta a pagar" onClose={() => setModalNovaConta(false)} maxWidth={480}>
        <ErrorBanner message={erro} />
        <Field label="Fornecedor (opcional)">
          <select className="w-full px-3 py-2 rounded border" style={{ borderColor: C.border, fontSize: 14 }} value={novaConta.fornecedorId} onChange={(e) => setNovaConta({ ...novaConta, fornecedorId: e.target.value })}>
            <option value="">Nenhum</option>
            {fornecedores.map((f) => <option key={f.id} value={f.id}>{f.nome}</option>)}
          </select>
        </Field>
        <div className="mt-3">
          <Field label="Descrição"><TextInput placeholder="Ex: Conta de energia" value={novaConta.descricao} onChange={(e) => setNovaConta({ ...novaConta, descricao: e.target.value })} /></Field>
        </div>
        <div className="mt-3">
          <Field label="Categoria">
            <Segmented options={[['FROTA', 'Frota'], ['PRODUCAO', 'Produção'], ['INSUMOS', 'Insumos'], ['OUTROS', 'Outros']]} value={novaConta.categoria} onChange={(v) => setNovaConta({ ...novaConta, categoria: v })} />
          </Field>
        </div>
        <div className="grid grid-cols-2 gap-4 mt-3">
          <Field label="Valor"><MoneyInput value={novaConta.valorOriginal} onChange={(v) => setNovaConta({ ...novaConta, valorOriginal: v })} /></Field>
          <Field label="Vencimento"><TextInput type="date" value={novaConta.dataVencimento} onChange={(e) => setNovaConta({ ...novaConta, dataVencimento: e.target.value })} /></Field>
        </div>
        <div className="mt-3">
          <Field label="Observação (opcional)"><TextInput value={novaConta.observacao} onChange={(e) => setNovaConta({ ...novaConta, observacao: e.target.value })} /></Field>
        </div>
        <div className="flex gap-2 mt-6">
          <button type="button" disabled={salvando || !novaConta.descricao || !novaConta.valorOriginal || !novaConta.dataVencimento} onClick={salvarNovaConta} className="px-4 py-2 rounded text-sm font-medium disabled:opacity-60" style={{ background: C.red, color: '#fff' }}>
            {salvando ? 'Salvando...' : 'Salvar'}
          </button>
          <button type="button" onClick={() => setModalNovaConta(false)} className="px-4 py-2 rounded text-sm" style={{ background: C.bg, color: C.textDark }}>Cancelar</button>
        </div>
      </Modal>

      {/* ---- Modal: registrar pagamento ---- */}
      <Modal open={!!contaPagamento} title={`Pagar — ${contaPagamento?.descricao || ''}`} onClose={() => setContaPagamento(null)} maxWidth={460}>
        <ErrorBanner message={erro} />
        {contaPagamento && (
          <>
            <div className="rounded-lg p-3 mb-4" style={{ background: C.bg }}>
              {contaPagamento.fornecedor && <div style={{ fontSize: 12, color: C.textMuted }}>{contaPagamento.fornecedor.nome}</div>}
              <div className="flex justify-between mt-1" style={{ fontSize: 13 }}>
                <span style={{ color: C.textMuted }}>Valor original</span>
                <span style={{ color: C.textDark }}>{moeda(contaPagamento.valorOriginal)}</span>
              </div>
              <div className="flex justify-between" style={{ fontSize: 13 }}>
                <span style={{ color: C.textMuted }}>Já pago</span>
                <span style={{ color: C.textDark }}>{moeda(contaPagamento.valorPago)}</span>
              </div>
              <div className="flex justify-between pt-1" style={{ borderTop: `1px solid ${C.border}`, fontWeight: 600, fontSize: 13 }}>
                <span>Saldo devedor</span>
                <span>{moeda(saldoDevedorPagamento)}</span>
              </div>
            </div>

            <Field label="Valor a pagar agora">
              <MoneyInput value={valorPagamentoInput} onChange={setValorPagamentoInput} />
            </Field>
            <button type="button" onClick={() => setValorPagamentoInput(String(saldoDevedorPagamento))} className="text-xs mt-1" style={{ color: C.blue }}>Preencher com o saldo devedor</button>

            {caixaAberto?.id ? (
              <label className="flex items-center gap-2 mt-4" style={{ fontSize: 13, color: C.textDark }}>
                <input type="checkbox" checked={saiuDoCaixa} onChange={(e) => setSaiuDoCaixa(e.target.checked)} />
                Saiu do caixa aberto hoje (dinheiro físico)
              </label>
            ) : (
              <div className="flex items-center gap-2 mt-4 px-3 py-2 rounded" style={{ background: C.bg, color: C.textMuted, fontSize: 12 }}>
                <AlertTriangle size={14} /> Nenhum caixa aberto — este pagamento não vai debitar do caixa (ex: pago por transferência).
              </div>
            )}

            <div className="flex gap-2 mt-5">
              <button
                type="button"
                disabled={salvandoPagamento || Number(valorPagamentoInput || 0) <= 0}
                onClick={confirmarPagamento}
                className="px-4 py-2 rounded text-sm font-medium disabled:opacity-60"
                style={{ background: C.red, color: '#fff' }}
              >
                {salvandoPagamento ? 'Registrando...' : 'Registrar pagamento'}
              </button>
              <button type="button" onClick={() => setContaPagamento(null)} className="px-4 py-2 rounded text-sm" style={{ background: C.bg, color: C.textDark }}>Cancelar</button>
            </div>
          </>
        )}
      </Modal>

      {/* ---- Modal: confirmar exclusao ---- */}
      <Modal open={!!paraExcluir} title="Excluir conta a pagar" onClose={() => setParaExcluir(null)} maxWidth={400}>
        <ErrorBanner message={erro} />
        <p style={{ fontSize: 14, color: C.textDark }}>Tem certeza que deseja excluir "{paraExcluir?.descricao}"?</p>
        <div className="flex gap-2 mt-6">
          <button type="button" disabled={excluindo} onClick={confirmarExclusao} className="px-4 py-2 rounded text-sm font-medium disabled:opacity-60" style={{ background: '#B3261E', color: '#fff' }}>
            {excluindo ? 'Excluindo...' : 'Excluir'}
          </button>
          <button type="button" onClick={() => setParaExcluir(null)} className="px-4 py-2 rounded text-sm" style={{ background: C.bg, color: C.textDark }}>Cancelar</button>
        </div>
      </Modal>

      {!modalNovaConta && !contaPagamento && !paraExcluir && <ErrorBanner message={erro} />}

      {contas.length === 0 ? (
        <EmptyState message={somenteEmAberto ? 'Nenhuma conta em aberto no momento.' : 'Nenhuma conta a pagar registrada ainda.'} />
      ) : (
        <div className="flex flex-col gap-3">
          {contas.map((conta) => {
            const saldoDevedor = conta.valorOriginal - conta.valorPago;
            const sit = situacao(conta);
            return (
              <Card key={conta.id}>
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-3 min-w-0">
                    <Avatar name={conta.fornecedor?.nome || conta.descricao} color={C.blue} />
                    <div className="min-w-0">
                      <div style={{ fontSize: 14, fontWeight: 600, color: C.textDark }} className="truncate">{conta.descricao}</div>
                      <div style={{ fontSize: 12, color: C.textMuted }} className="truncate">
                        {conta.fornecedor?.nome ? `${conta.fornecedor.nome} · ` : ''}{CATEGORIA_LABEL[conta.categoria]} · vence em {dataCurta(conta.dataVencimento)}
                      </div>
                    </div>
                  </div>
                  <div className="flex items-center gap-4 flex-shrink-0">
                    <Badge tone={sit.tone}>{sit.label}</Badge>
                    <div style={{ textAlign: 'right' }}>
                      <div style={{ fontSize: 11, color: C.textMuted }}>Saldo devedor</div>
                      <div style={{ fontSize: 16, fontWeight: 600, color: C.textDark, fontFamily: DISPLAY_FONT }}>{moeda(saldoDevedor)}</div>
                    </div>
                    {conta.status !== 'PAGA' && (
                      <button onClick={() => abrirPagamento(conta)} className="px-3 py-1.5 rounded text-xs font-medium" style={{ background: C.red, color: '#fff' }}>
                        Pagar
                      </button>
                    )}
                    {conta.valorPago === 0 && (
                      <button onClick={() => setParaExcluir(conta)} className="px-2 py-1.5 rounded text-xs" style={{ color: '#B3261E' }}>
                        Excluir
                      </button>
                    )}
                  </div>
                </div>
              </Card>
            );
          })}
        </div>
      )}
    </div>
  );
}
