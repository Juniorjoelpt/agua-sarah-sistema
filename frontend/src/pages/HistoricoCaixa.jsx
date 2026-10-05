import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowLeft } from 'lucide-react';
import { Card, Field, TextInput, ErrorBanner, Loading, Badge, EmptyState } from '../components/ui';
import Modal from '../components/Modal';
import { C, DISPLAY_FONT } from '../theme';
import { caixaApi } from '../api/caixa';

const moeda = (v) => (v ?? 0).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
const dataCurta = (iso) => new Date(iso).toLocaleDateString('pt-BR', { day: '2-digit', month: '2-digit', year: 'numeric' });
const horaCurta = (iso) => new Date(iso).toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });

function isoLocal(d) {
  const p = (n) => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}`;
}

function Linha({ label, value, color, bold }) {
  return (
    <div className="flex justify-between" style={{ fontSize: 13, fontWeight: bold ? 500 : 400 }}>
      <span style={{ color: bold ? C.textDark : C.textMuted }}>{label}</span>
      <span style={{ color: color || C.textDark }}>{value}</span>
    </div>
  );
}

function Secao({ titulo, children }) {
  return (
    <div className="mb-5">
      <div style={{ fontSize: 12, fontWeight: 600, color: C.textMuted, textTransform: 'uppercase', letterSpacing: 0.5, marginBottom: 8 }}>{titulo}</div>
      {children}
    </div>
  );
}

export default function HistoricoCaixa() {
  const navigate = useNavigate();
  const hoje = new Date();
  const trintaAtras = new Date();
  trintaAtras.setDate(hoje.getDate() - 30);

  const [inicio, setInicio] = useState(isoLocal(trintaAtras));
  const [fim, setFim] = useState(isoLocal(hoje));
  const [lista, setLista] = useState([]);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState(null);

  const [detalhe, setDetalhe] = useState(null);
  const [carregandoDetalhe, setCarregandoDetalhe] = useState(false);
  const [modalAberto, setModalAberto] = useState(false);

  async function buscar() {
    setCarregando(true);
    setErro(null);
    try {
      setLista(await caixaApi.resumos(inicio, fim));
    } catch (e) {
      setErro(e.message);
    } finally {
      setCarregando(false);
    }
  }

  useEffect(() => { buscar(); /* eslint-disable-next-line */ }, []);

  async function abrirDetalhe(id) {
    setModalAberto(true);
    setDetalhe(null);
    setCarregandoDetalhe(true);
    setErro(null);
    try {
      setDetalhe(await caixaApi.detalhe(id));
    } catch (e) {
      setErro(e.message);
    } finally {
      setCarregandoDetalhe(false);
    }
  }

  const r = detalhe?.resumo;

  return (
    <div>
      <div className="flex items-start justify-between mb-6">
        <div>
          <h2 style={{ fontFamily: DISPLAY_FONT, fontSize: 22, fontWeight: 500, color: C.onDark }}>Histórico de caixa</h2>
          <p style={{ fontSize: 13, color: C.onDarkMuted, marginTop: 2 }}>Consulte em detalhe os caixas de dias anteriores</p>
        </div>
        <button
          onClick={() => navigate('/caixa')}
          className="flex items-center gap-1.5 px-4 py-2 rounded text-sm"
          style={{ background: C.bg, color: C.textDark, border: `1px solid ${C.border}`, flexShrink: 0 }}
        >
          <ArrowLeft size={14} /> Voltar ao caixa
        </button>
      </div>

      <Card style={{ marginBottom: 16 }}>
        <div className="flex items-end gap-3 flex-wrap">
          <Field label="De"><TextInput type="date" value={inicio} onChange={(e) => setInicio(e.target.value)} /></Field>
          <Field label="Até"><TextInput type="date" value={fim} onChange={(e) => setFim(e.target.value)} /></Field>
          <button onClick={buscar} className="px-4 py-2 rounded text-sm font-medium" style={{ background: C.ink, color: '#fff', height: 38 }}>
            Filtrar
          </button>
        </div>
      </Card>

      <ErrorBanner message={!modalAberto ? erro : null} />

      {carregando ? (
        <Loading />
      ) : lista.length === 0 ? (
        <EmptyState message="Nenhum caixa encontrado no período." />
      ) : (
        <div className="flex flex-col gap-2">
          {lista.map((c) => (
            <button key={c.caixaId} onClick={() => abrirDetalhe(c.caixaId)} className="text-left">
              <Card hover>
                <div className="flex items-center justify-between gap-4 flex-wrap">
                  <div>
                    <div style={{ fontSize: 14, fontWeight: 500, color: C.textDark }}>
                      {dataCurta(c.dataAbertura)} <span style={{ color: C.textMuted, fontWeight: 400 }}>· Caixa #{c.caixaId}</span>
                    </div>
                    <div style={{ fontSize: 12, color: C.textMuted }}>
                      {horaCurta(c.dataAbertura)}{c.dataFechamento ? ` — ${horaCurta(c.dataFechamento)}` : ''}
                    </div>
                  </div>
                  <div className="flex items-center gap-6" style={{ fontSize: 12 }}>
                    <div><div style={{ color: C.textMuted }}>Vendas</div><div style={{ color: C.textDark, fontWeight: 500 }}>{moeda(c.totalVendasEspecie + c.totalVendasPix)}</div></div>
                    <div><div style={{ color: C.textMuted }}>Despesas</div><div style={{ color: C.amber, fontWeight: 500 }}>{moeda(c.totalDespesas)}</div></div>
                    <div><div style={{ color: C.textMuted }}>Saldo espécie</div><div style={{ color: C.textDark, fontWeight: 500 }}>{moeda(c.saldoFinalEspecie)}</div></div>
                    <div><div style={{ color: C.textMuted }}>Saldo PIX</div><div style={{ color: C.textDark, fontWeight: 500 }}>{moeda(c.saldoFinalPix)}</div></div>
                    <Badge tone={c.status === 'ABERTO' ? 'blue' : 'neutral'}>{c.status === 'ABERTO' ? 'Aberto' : 'Fechado'}</Badge>
                  </div>
                </div>
              </Card>
            </button>
          ))}
        </div>
      )}

      <Modal open={modalAberto} title={r ? `Caixa #${r.caixaId} — ${dataCurta(r.dataAbertura)}` : 'Caixa'} onClose={() => setModalAberto(false)} maxWidth={760}>
        <ErrorBanner message={erro} />
        {carregandoDetalhe || !detalhe ? (
          <Loading />
        ) : (
          <div style={{ maxHeight: '70vh', overflowY: 'auto', paddingRight: 4 }}>
            <Secao titulo="Resumo">
              <div className="flex flex-col gap-1.5">
                <Linha label="Aberto em" value={`${new Date(r.dataAbertura).toLocaleString('pt-BR')}${detalhe.usuarioAbertura ? ` por ${detalhe.usuarioAbertura}` : ''}`} />
                <Linha label="Fechado em" value={r.dataFechamento ? `${new Date(r.dataFechamento).toLocaleString('pt-BR')}${detalhe.usuarioFechamento ? ` por ${detalhe.usuarioFechamento}` : ''}` : 'Ainda aberto'} />
                <Linha label="Saldo inicial (espécie)" value={moeda(r.saldoInicialEspecie)} />
                <Linha label="Saldo inicial (PIX)" value={moeda(r.saldoInicialPix)} />
                <Linha label="Vendas em espécie" value={moeda(r.totalVendasEspecie)} />
                <Linha label="Vendas em PIX" value={moeda(r.totalVendasPix)} />
                {(r.totalRecebimentosContasReceberEspecie > 0 || r.totalRecebimentosContasReceberPix > 0) && (
                  <Linha label="Recebimentos de fiado" value={moeda(r.totalRecebimentosContasReceberEspecie + r.totalRecebimentosContasReceberPix)} color={C.blue} />
                )}
                <Linha label="Despesas" value={moeda(r.totalDespesas)} color={C.amber} />
                <Linha label="Galões bonificados" value={r.totalGaloesBonificados} color={C.amber} />
                <div className="pt-1.5" style={{ borderTop: `1px solid ${C.border}` }}>
                  <Linha bold label="Saldo final (espécie)" value={moeda(r.saldoFinalEspecie)} />
                  <Linha bold label="Saldo final (PIX)" value={moeda(r.saldoFinalPix)} />
                </div>
              </div>
            </Secao>

            <Secao titulo={`Vendas (${detalhe.vendas.length}) — ${moeda(detalhe.totalVendas)}`}>
              {detalhe.vendas.length === 0 ? (
                <div style={{ fontSize: 13, color: C.textMuted }}>Nenhuma venda neste caixa.</div>
              ) : (
                <div className="flex flex-col gap-2">
                  {detalhe.vendas.map((v) => (
                    <div key={v.id} className="rounded-lg px-3 py-2.5" style={{ border: `1px solid ${C.border}` }}>
                      <div className="flex justify-between gap-3">
                        <div>
                          <div style={{ fontSize: 13, fontWeight: 500, color: C.textDark }}>
                            #{v.id} · {horaCurta(v.dataHora)} · {v.cliente || 'Consumidor'}
                          </div>
                          <div style={{ fontSize: 12, color: C.textMuted }}>
                            {v.itens.map((i) => `${i.quantidade}x ${i.produto}`).join(', ')}
                          </div>
                        </div>
                        <div style={{ fontSize: 14, fontWeight: 500, color: C.textDark, whiteSpace: 'nowrap' }}>{moeda(v.valorTotal)}</div>
                      </div>
                      <div className="flex flex-wrap gap-1.5 mt-1.5">
                        {v.valorRecebidoEspecie > 0 && <Badge tone="neutral">Espécie {moeda(v.valorRecebidoEspecie)}</Badge>}
                        {v.valorRecebidoPix > 0 && <Badge tone="blue">PIX {moeda(v.valorRecebidoPix)}</Badge>}
                        {v.valorFiado > 0 && <Badge tone="amber">Fiado {moeda(v.valorFiado)}</Badge>}
                        {v.quantidadeAvariaCliente > 0 && <Badge tone="amber">Avaria cliente: {v.quantidadeAvariaCliente}</Badge>}
                        {v.quantidadeAvariaProducao > 0 && <Badge tone="amber">Avaria produção: {v.quantidadeAvariaProducao}</Badge>}
                        {v.quantidadeBonificados > 0 && <Badge tone="amber">Bonificados: {v.quantidadeBonificados}</Badge>}
                        {v.valorDesconto > 0 && <Badge tone="neutral">Desconto {Number(v.percentualDesconto).toLocaleString('pt-BR')}% (-{moeda(v.valorDesconto)})</Badge>}
                      </div>
                      {v.observacao && <div style={{ fontSize: 12, color: C.textMuted, marginTop: 4 }}>Obs: {v.observacao}</div>}
                    </div>
                  ))}
                </div>
              )}
            </Secao>

            <Secao titulo={`Despesas (${detalhe.despesas.length})`}>
              {detalhe.despesas.length === 0 ? (
                <div style={{ fontSize: 13, color: C.textMuted }}>Nenhuma despesa neste caixa.</div>
              ) : (
                <div className="flex flex-col gap-1.5">
                  {detalhe.despesas.map((d) => (
                    <div key={d.id} className="flex justify-between" style={{ fontSize: 13 }}>
                      <span style={{ color: C.textDark }}>{d.descricao} <span style={{ color: C.textMuted }}>· {d.categoria}</span></span>
                      <span style={{ color: C.amber }}>{moeda(d.valor)}</span>
                    </div>
                  ))}
                </div>
              )}
            </Secao>

            <Secao titulo={`Recebimentos de fiado (${detalhe.recebimentos.length})`}>
              {detalhe.recebimentos.length === 0 ? (
                <div style={{ fontSize: 13, color: C.textMuted }}>Nenhum recebimento neste caixa.</div>
              ) : (
                <div className="flex flex-col gap-1.5">
                  {detalhe.recebimentos.map((p) => (
                    <div key={p.id} className="flex justify-between" style={{ fontSize: 13 }}>
                      <span style={{ color: C.textDark }}>{p.cliente || 'Cliente'} <span style={{ color: C.textMuted }}>· {p.data ? horaCurta(p.data) : ''}</span></span>
                      <span style={{ color: C.blue }}>
                        {[p.valorEspecie > 0 && `Espécie ${moeda(p.valorEspecie)}`, p.valorPix > 0 && `PIX ${moeda(p.valorPix)}`].filter(Boolean).join(' + ')}
                      </span>
                    </div>
                  ))}
                </div>
              )}
            </Secao>
          </div>
        )}
      </Modal>
    </div>
  );
}
