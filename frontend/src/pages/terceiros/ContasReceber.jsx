import React, { useEffect, useState } from 'react';
import { SectionHeaderWithAction, Card, Field, TextInput, Segmented, ErrorBanner, Loading, Badge, EmptyState, Avatar, SearchableSelect, MoneyInput } from '../../components/ui';
import Modal from '../../components/Modal';
import { C, DISPLAY_FONT } from '../../theme';
import { contasReceberTerceirosApi } from '../../api/terceiros/contasReceber';
import { clientesTerceirosApi } from '../../api/terceiros/clientes';

const NOVA_CONTA_VAZIA = { clienteId: '', descricao: '', valorOriginal: '', dataVencimento: '' };
const moeda = (v) => (v ?? 0).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
const dataCurta = (iso) => new Date(iso).toLocaleDateString('pt-BR', { day: '2-digit', month: '2-digit', year: 'numeric' });
const STATUS_LABEL = { ABERTA: 'Em aberto', PARCIAL: 'Parcial', PAGA: 'Quitada' };
const STATUS_TONE = { ABERTA: 'red', PARCIAL: 'amber', PAGA: 'blue' };

export default function ContasReceberTerceiros() {
  const [contas, setContas] = useState([]);
  const [clientes, setClientes] = useState([]);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState(null);
  const [somenteEmAberto, setSomenteEmAberto] = useState(true);
  const [salvando, setSalvando] = useState(false);

  const [modalNovaConta, setModalNovaConta] = useState(false);
  const [novaConta, setNovaConta] = useState(NOVA_CONTA_VAZIA);

  const [contaPagamento, setContaPagamento] = useState(null);
  const [valorEspecieInput, setValorEspecieInput] = useState('');
  const [valorPixInput, setValorPixInput] = useState('');
  const [salvandoPagamento, setSalvandoPagamento] = useState(false);

  async function carregar() {
    setCarregando(true);
    try {
      const [c, cli] = await Promise.all([
        contasReceberTerceirosApi.listar(undefined, somenteEmAberto),
        clientesTerceirosApi.listar(),
      ]);
      setContas(c);
      setClientes(cli);
    } catch (e) {
      setErro(e.message);
    }
    setCarregando(false);
  }

  useEffect(() => { carregar(); }, [somenteEmAberto]);

  const totalEmAberto = contas.filter((c) => c.status !== 'PAGA').reduce((s, c) => s + (c.valorOriginal - c.valorPago), 0);

  function abrirNovaConta() {
    setNovaConta(NOVA_CONTA_VAZIA);
    setModalNovaConta(true);
  }

  async function salvarNovaConta() {
    setSalvando(true);
    setErro(null);
    try {
      await contasReceberTerceirosApi.criar({
        clienteId: Number(novaConta.clienteId),
        descricao: novaConta.descricao,
        valorOriginal: Number(novaConta.valorOriginal),
        dataVencimento: novaConta.dataVencimento || null,
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
    setValorEspecieInput('');
    setValorPixInput('');
  }

  const saldoDevedorPagamento = contaPagamento ? contaPagamento.valorOriginal - contaPagamento.valorPago : 0;
  const totalPagamentoInput = Number(valorEspecieInput || 0) + Number(valorPixInput || 0);

  async function confirmarPagamento() {
    setSalvandoPagamento(true);
    setErro(null);
    try {
      await contasReceberTerceirosApi.registrarPagamento(contaPagamento.id, {
        valorEspecie: Number(valorEspecieInput || 0),
        valorPix: Number(valorPixInput || 0),
      });
      setContaPagamento(null);
      await carregar();
    } catch (e) {
      setErro(e.message);
    } finally {
      setSalvandoPagamento(false);
    }
  }

  function preencherRestante() {
    setValorEspecieInput(String(saldoDevedorPagamento));
    setValorPixInput('');
  }

  if (carregando) return <Loading />;

  return (
    <div>
      <SectionHeaderWithAction title="Contas a Receber" subtitle="Vendas fiado e dívidas de empresas terceiras" actionLabel="+ Nova conta" onAction={abrirNovaConta} />

      <Card style={{ marginBottom: 16 }}>
        <div className="flex items-center justify-between">
          <div>
            <div style={{ fontSize: 12, color: C.textMuted }}>Total em aberto (contas listadas)</div>
            <div style={{ fontSize: 22, fontWeight: 600, color: C.textDark, fontFamily: DISPLAY_FONT }}>{moeda(totalEmAberto)}</div>
          </div>
          <Segmented options={[['true', 'Em aberto'], ['false', 'Todas']]} value={String(somenteEmAberto)} onChange={(v) => setSomenteEmAberto(v === 'true')} />
        </div>
      </Card>

      <Modal open={modalNovaConta} title="Nova conta a receber" onClose={() => setModalNovaConta(false)} maxWidth={480}>
        <ErrorBanner message={erro} />
        <Field label="Cliente">
          <SearchableSelect
            options={clientes.map((c) => ({ value: c.id, label: c.nome }))}
            value={novaConta.clienteId}
            onChange={(v) => setNovaConta({ ...novaConta, clienteId: v })}
            emptyLabel="Selecione"
            placeholder="Buscar cliente por nome..."
          />
        </Field>
        <div className="mt-3">
          <Field label="Descrição"><TextInput placeholder="Ex: Dívida anterior" value={novaConta.descricao} onChange={(e) => setNovaConta({ ...novaConta, descricao: e.target.value })} /></Field>
        </div>
        <div className="grid grid-cols-2 gap-4 mt-3">
          <Field label="Valor"><MoneyInput value={novaConta.valorOriginal} onChange={(v) => setNovaConta({ ...novaConta, valorOriginal: v })} /></Field>
          <Field label="Vencimento (opcional)"><TextInput type="date" value={novaConta.dataVencimento} onChange={(e) => setNovaConta({ ...novaConta, dataVencimento: e.target.value })} /></Field>
        </div>
        <div className="flex gap-2 mt-6">
          <button type="button" disabled={salvando || !novaConta.clienteId || !novaConta.descricao || !novaConta.valorOriginal} onClick={salvarNovaConta} className="px-4 py-2 rounded text-sm font-medium disabled:opacity-60" style={{ background: C.red, color: '#fff' }}>
            {salvando ? 'Salvando...' : 'Salvar'}
          </button>
          <button type="button" onClick={() => setModalNovaConta(false)} className="px-4 py-2 rounded text-sm" style={{ background: C.bg, color: C.textDark }}>Cancelar</button>
        </div>
      </Modal>

      <Modal open={!!contaPagamento} title={`Receber pagamento — ${contaPagamento?.cliente?.nome || ''}`} onClose={() => setContaPagamento(null)} maxWidth={480}>
        <ErrorBanner message={erro} />
        {contaPagamento && (
          <>
            <div className="rounded-lg p-3 mb-4" style={{ background: C.bg }}>
              <div style={{ fontSize: 12, color: C.textMuted }}>{contaPagamento.descricao}</div>
              <div className="flex justify-between mt-1" style={{ fontSize: 13 }}><span style={{ color: C.textMuted }}>Valor original</span><span style={{ color: C.textDark }}>{moeda(contaPagamento.valorOriginal)}</span></div>
              <div className="flex justify-between" style={{ fontSize: 13 }}><span style={{ color: C.textMuted }}>Já pago</span><span style={{ color: C.textDark }}>{moeda(contaPagamento.valorPago)}</span></div>
              <div className="flex justify-between pt-1" style={{ borderTop: `1px solid ${C.border}`, fontWeight: 600, fontSize: 13 }}><span>Saldo devedor</span><span>{moeda(saldoDevedorPagamento)}</span></div>
            </div>
            <div className="grid grid-cols-2 gap-4">
              <Field label="Recebido em espécie"><MoneyInput value={valorEspecieInput} onChange={(v) => setValorEspecieInput(v)} /></Field>
              <Field label="Recebido em PIX"><MoneyInput value={valorPixInput} onChange={(v) => setValorPixInput(v)} /></Field>
            </div>
            <button type="button" onClick={preencherRestante} className="text-xs mt-2" style={{ color: C.blue }}>Preencher com o saldo devedor (espécie)</button>

            {totalPagamentoInput > saldoDevedorPagamento + 0.01 && (
              <div className="mt-2 px-3 py-2 rounded" style={{ background: C.amberLight, color: '#7A4A1F', fontSize: 12 }}>O valor informado é maior que o saldo devedor.</div>
            )}

            <div className="flex gap-2 mt-5">
              <button type="button" disabled={salvandoPagamento || totalPagamentoInput <= 0 || totalPagamentoInput > saldoDevedorPagamento + 0.01} onClick={confirmarPagamento} className="px-4 py-2 rounded text-sm font-medium disabled:opacity-60" style={{ background: C.red, color: '#fff' }}>
                {salvandoPagamento ? 'Registrando...' : 'Registrar pagamento'}
              </button>
              <button type="button" onClick={() => setContaPagamento(null)} className="px-4 py-2 rounded text-sm" style={{ background: C.bg, color: C.textDark }}>Cancelar</button>
            </div>
          </>
        )}
      </Modal>

      {!modalNovaConta && !contaPagamento && <ErrorBanner message={erro} />}

      {contas.length === 0 ? (
        <EmptyState message={somenteEmAberto ? 'Nenhuma conta em aberto no momento.' : 'Nenhuma conta a receber registrada ainda.'} />
      ) : (
        <div className="flex flex-col gap-3">
          {contas.map((conta) => {
            const saldoDevedor = conta.valorOriginal - conta.valorPago;
            return (
              <Card key={conta.id}>
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-3 min-w-0">
                    <Avatar name={conta.cliente?.nome || '?'} color={C.blue} />
                    <div className="min-w-0">
                      <div style={{ fontSize: 14, fontWeight: 600, color: C.textDark }} className="truncate">{conta.cliente?.nome}</div>
                      <div style={{ fontSize: 12, color: C.textMuted }} className="truncate">
                        {conta.descricao} · {dataCurta(conta.dataCriacao)}
                        {conta.dataVencimento ? ` · vence em ${dataCurta(conta.dataVencimento)}` : ''}
                      </div>
                    </div>
                  </div>
                  <div className="flex items-center gap-4 flex-shrink-0">
                    <Badge tone={STATUS_TONE[conta.status]}>{STATUS_LABEL[conta.status]}</Badge>
                    <div style={{ textAlign: 'right' }}>
                      <div style={{ fontSize: 11, color: C.textMuted }}>Saldo devedor</div>
                      <div style={{ fontSize: 16, fontWeight: 600, color: C.textDark, fontFamily: DISPLAY_FONT }}>{moeda(saldoDevedor)}</div>
                    </div>
                    {conta.status !== 'PAGA' && (
                      <button onClick={() => abrirPagamento(conta)} className="px-3 py-1.5 rounded text-xs font-medium" style={{ background: C.red, color: '#fff' }}>Receber</button>
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
