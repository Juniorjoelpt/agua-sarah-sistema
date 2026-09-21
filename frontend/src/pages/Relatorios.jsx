import React, { useState } from 'react';
import { PageHeader, Card, Field, TextInput, ErrorBanner, Badge } from '../components/ui';
import { C } from '../theme';
import { FileBarChart, Download } from 'lucide-react';
import { relatoriosApi } from '../api/relatorios';

function primeiroDiaDoMes() {
  const hoje = new Date();
  return new Date(hoje.getFullYear(), hoje.getMonth(), 1).toISOString().slice(0, 10);
}
function hojeISO() {
  return new Date().toISOString().slice(0, 10);
}
function moeda(v) {
  return (v ?? 0).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
}

export default function Relatorios() {
  const [inicio, setInicio] = useState(primeiroDiaDoMes());
  const [fim, setFim] = useState(hojeISO());
  const [somenteUmDia, setSomenteUmDia] = useState(false);
  const [erro, setErro] = useState(null);
  const [resultado, setResultado] = useState(null);
  const [tipoAberto, setTipoAberto] = useState(null);
  const [carregando, setCarregando] = useState(false);
  const [baixando, setBaixando] = useState(false);

  function alterarInicio(v) {
    setInicio(v);
    if (somenteUmDia) setFim(v);
  }

  function alternarSomenteUmDia(ativo) {
    setSomenteUmDia(ativo);
    if (ativo) setFim(inicio);
  }

  async function gerar(tipo) {
    setTipoAberto(tipo);
    setErro(null);
    setResultado(null);
    setCarregando(true);
    try {
      if (tipo === 'despesas') setResultado(await relatoriosApi.despesas(inicio, fim));
      if (tipo === 'estoque') setResultado(await relatoriosApi.estoque());
      if (tipo === 'envase') setResultado(await relatoriosApi.envase(inicio, fim));
      if (tipo === 'caixa') setResultado(await relatoriosApi.caixa(inicio, fim));
      if (tipo === 'fluxo-caixa') setResultado(await relatoriosApi.fluxoCaixa(inicio, fim));
    } catch (e) {
      setErro(e.message);
    } finally {
      setCarregando(false);
    }
  }

  async function baixarPdf() {
    setErro(null);
    setBaixando(true);
    try {
      if (tipoAberto === 'despesas') await relatoriosApi.baixarPdfDespesas(inicio, fim);
      if (tipoAberto === 'estoque') await relatoriosApi.baixarPdfEstoque();
      if (tipoAberto === 'envase') await relatoriosApi.baixarPdfEnvase(inicio, fim);
      if (tipoAberto === 'caixa') await relatoriosApi.baixarPdfCaixa(inicio, fim);
      if (tipoAberto === 'fluxo-caixa') await relatoriosApi.baixarPdfFluxoCaixa(inicio, fim);
    } catch (e) {
      setErro(e.message);
    } finally {
      setBaixando(false);
    }
  }

  function BotaoPdf() {
    return (
      <button
        onClick={baixarPdf}
        disabled={baixando}
        className="flex items-center gap-1.5 px-3 py-1.5 rounded text-xs font-medium disabled:opacity-60"
        style={{ background: C.red, color: '#fff' }}
      >
        <Download size={13} /> {baixando ? 'Gerando...' : 'Baixar PDF'}
      </button>
    );
  }

  const reports = [
    { id: 'caixa', title: 'Histórico de caixa', desc: 'Fechamentos por período ou por dia' },
    { id: 'fluxo-caixa', title: 'Fluxo de Caixa', desc: 'Entradas e saídas das contas bancárias, por período ou por dia' },
    { id: 'despesas', title: 'Despesas', desc: 'Por categoria e período' },
    { id: 'estoque', title: 'Estoque', desc: 'Posição atual dos insumos' },
    { id: 'envase', title: 'Envase de água', desc: 'PDV + caminhões, com avarias do período' },
  ];

  return (
    <div>
      <PageHeader title="Relatórios" />
      <Card style={{ marginBottom: 16 }}>
        <div className="flex gap-4 items-end flex-wrap">
          <Field label="Período - de">
            <TextInput type="date" value={inicio} onChange={(e) => alterarInicio(e.target.value)} style={{ width: 160 }} />
          </Field>
          <Field label="até">
            <TextInput type="date" value={fim} disabled={somenteUmDia} onChange={(e) => setFim(e.target.value)} style={{ width: 160, opacity: somenteUmDia ? 0.6 : 1 }} />
          </Field>
          <label className="flex items-center gap-2 pb-2" style={{ fontSize: 12, color: C.textMuted }}>
            <input type="checkbox" checked={somenteUmDia} onChange={(e) => alternarSomenteUmDia(e.target.checked)} />
            Gerar por um único dia
          </label>
        </div>
      </Card>
      <div className="grid grid-cols-2 gap-4 mb-6">
        {reports.map((r) => (
          <Card key={r.id} hover style={{ cursor: 'pointer' }}>
            <div onClick={() => gerar(r.id)} className="flex justify-between items-center">
              <div>
                <div style={{ fontSize: 14, fontWeight: 500, color: C.textDark }}>{r.title}</div>
                <div style={{ fontSize: 12, color: C.textMuted, marginTop: 2 }}>{r.desc}</div>
              </div>
              <FileBarChart size={18} color={C.blue} />
            </div>
          </Card>
        ))}
      </div>

      <ErrorBanner message={erro} />
      {carregando && <div style={{ fontSize: 13, color: C.onDarkMuted }}>Gerando relatório...</div>}

      {tipoAberto === 'caixa' && resultado && (
        <Card>
          <div className="flex items-center justify-between mb-3">
            <div style={{ fontSize: 13, fontWeight: 500, color: C.textDark }}>Histórico de caixa no período</div>
            <BotaoPdf />
          </div>
          {resultado.length === 0 ? (
            <div style={{ fontSize: 13, color: C.textMuted }}>Nenhum caixa nesse período.</div>
          ) : (
            <div className="flex flex-col gap-3">
              {resultado.map((r) => (
                <div key={r.caixaId} className="rounded-lg p-3" style={{ border: `1px solid ${C.border}` }}>
                  <div className="flex justify-between items-center mb-2">
                    <span style={{ fontSize: 13, fontWeight: 600, color: C.textDark }}>
                      {new Date(r.dataAbertura).toLocaleDateString('pt-BR', { day: '2-digit', month: '2-digit', year: 'numeric' })}
                    </span>
                    <Badge tone={r.status === 'ABERTO' ? 'blue' : 'neutral'}>{r.status === 'ABERTO' ? 'Aberto' : 'Fechado'}</Badge>
                  </div>
                  <div className="flex flex-col gap-1" style={{ fontSize: 12 }}>
                    <div className="flex justify-between"><span style={{ color: C.textMuted }}>Entradas espécie</span><span style={{ color: C.textDark }}>{moeda(r.totalVendasEspecie)}</span></div>
                    <div className="flex justify-between"><span style={{ color: C.textMuted }}>Entradas PIX</span><span style={{ color: C.textDark }}>{moeda(r.totalVendasPix)}</span></div>
                    {(r.totalRecebimentosContasReceberEspecie > 0 || r.totalRecebimentosContasReceberPix > 0) && (
                      <div className="flex justify-between"><span style={{ color: C.textMuted }}>Recebimentos de fiado</span><span style={{ color: C.blue }}>{moeda(r.totalRecebimentosContasReceberEspecie + r.totalRecebimentosContasReceberPix)}</span></div>
                    )}
                    <div className="flex justify-between"><span style={{ color: C.textMuted }}>Despesas</span><span style={{ color: C.amber }}>{moeda(r.totalDespesas)}</span></div>
                    <div className="flex justify-between"><span style={{ color: C.textMuted }}>Galões bonificados</span><span style={{ color: C.amber }}>{r.totalGaloesBonificados}</span></div>
                    <div className="flex justify-between pt-1" style={{ borderTop: `1px solid ${C.border}`, fontWeight: 600 }}>
                      <span style={{ color: C.textDark }}>Saldo final</span>
                      <span style={{ color: C.textDark }}>{moeda(r.saldoFinalEspecie + r.saldoFinalPix)}</span>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </Card>
      )}

      {tipoAberto === 'fluxo-caixa' && resultado && (
        <Card>
          <div className="flex items-center justify-between mb-3">
            <div style={{ fontSize: 13, fontWeight: 500, color: C.textDark }}>Fluxo de caixa no período</div>
            <BotaoPdf />
          </div>

          <div className="grid grid-cols-3 gap-3 mb-4">
            <div className="rounded-lg p-3" style={{ background: C.blueLight }}>
              <div style={{ fontSize: 11, color: C.ink }}>Total de entradas</div>
              <div style={{ fontSize: 18, fontWeight: 600, color: C.ink }}>{moeda(resultado.totalEntradas)}</div>
            </div>
            <div className="rounded-lg p-3" style={{ background: C.amberLight }}>
              <div style={{ fontSize: 11, color: '#7A4A1F' }}>Total de saídas</div>
              <div style={{ fontSize: 18, fontWeight: 600, color: '#7A4A1F' }}>{moeda(resultado.totalSaidas)}</div>
            </div>
            <div className="rounded-lg p-3" style={{ background: C.bg }}>
              <div style={{ fontSize: 11, color: C.textMuted }}>Saldo do período</div>
              <div style={{ fontSize: 18, fontWeight: 600, color: C.textDark }}>{moeda(resultado.saldoPeriodo)}</div>
            </div>
          </div>

          {resultado.lancamentos.length === 0 ? (
            <div style={{ fontSize: 13, color: C.textMuted }}>Nenhum lançamento nesse período.</div>
          ) : (
            <div className="flex flex-col" style={{ maxHeight: 360, overflowY: 'auto' }}>
              {resultado.lancamentos.map((l) => (
                <div key={l.id} className="flex justify-between items-center py-2" style={{ borderBottom: `1px solid ${C.border}`, fontSize: 13 }}>
                  <div>
                    <div style={{ color: C.textDark }}>{l.descricao}{l.categoria ? ` · ${l.categoria}` : ''}</div>
                    <div style={{ fontSize: 11, color: C.textMuted }}>
                      {l.contaBancaria?.apelido} · {new Date(l.data + 'T00:00:00').toLocaleDateString('pt-BR')}
                    </div>
                  </div>
                  <span style={{ fontWeight: 600, color: l.tipo === 'ENTRADA' ? '#1E7A4C' : C.red }}>
                    {l.tipo === 'ENTRADA' ? '+ ' : '- '}{moeda(l.valor)}
                  </span>
                </div>
              ))}
            </div>
          )}
        </Card>
      )}

      {tipoAberto === 'despesas' && resultado && (
        <Card>
          <div className="flex items-center justify-between mb-3">
            <div style={{ fontSize: 13, fontWeight: 500, color: C.textDark }}>Despesas no período</div>
            <BotaoPdf />
          </div>
          {resultado.length === 0 ? (
            <div style={{ fontSize: 13, color: C.textMuted }}>Nenhuma despesa no período.</div>
          ) : resultado.map((d) => (
            <div key={d.id} className="flex justify-between py-2" style={{ borderBottom: `1px solid ${C.border}`, fontSize: 13 }}>
              <span>{d.descricao} ({d.categoria})</span>
              <span>{d.valor.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })}</span>
            </div>
          ))}
        </Card>
      )}

      {tipoAberto === 'estoque' && resultado && (
        <Card>
          <div className="flex items-center justify-between mb-3">
            <div style={{ fontSize: 13, fontWeight: 500, color: C.textDark }}>Posição de estoque</div>
            <BotaoPdf />
          </div>
          {resultado.map((i) => (
            <div key={i.id} className="flex justify-between py-2" style={{ borderBottom: `1px solid ${C.border}`, fontSize: 13 }}>
              <span>{i.nome}</span>
              <span style={{ color: i.quantidadeAtual <= i.quantidadeMinima ? C.amber : C.textDark }}>{i.quantidadeAtual} {i.unidadeMedida}</span>
            </div>
          ))}
        </Card>
      )}

      {tipoAberto === 'envase' && resultado && (
        <Card>
          <div className="flex items-center justify-between mb-3">
            <div style={{ fontSize: 13, fontWeight: 500, color: C.textDark }}>Envase de água no período</div>
            <BotaoPdf />
          </div>

          <div className="grid grid-cols-2 gap-3 mb-4">
            <div className="rounded-lg p-3" style={{ background: C.blueLight }}>
              <div style={{ fontSize: 11, color: C.ink }}>Total envasado</div>
              <div style={{ fontSize: 22, fontWeight: 600, color: C.ink }}>{resultado.totalGaloesEnvasados}</div>
              <div style={{ fontSize: 11, color: C.ink }}>galões</div>
            </div>
            <div className="rounded-lg p-3" style={{ background: C.amberLight }}>
              <div style={{ fontSize: 11, color: '#7A4A1F' }}>Total de avarias</div>
              <div style={{ fontSize: 22, fontWeight: 600, color: '#7A4A1F' }}>{resultado.totalAvarias}</div>
              <div style={{ fontSize: 11, color: '#7A4A1F' }}>galões</div>
            </div>
          </div>

          <div style={{ fontSize: 12, fontWeight: 500, color: C.textMuted, marginBottom: 6, marginTop: 8 }}>De onde vem o envase</div>
          <div className="flex justify-between py-1.5" style={{ fontSize: 13 }}>
            <span>Vendidos no PDV</span><span>{resultado.totalGaloesVendidosPdv}</span>
          </div>
          <div className="flex justify-between py-1.5" style={{ fontSize: 13, borderBottom: `1px solid ${C.border}` }}>
            <span>Carregados nos caminhões</span><span>{resultado.totalGaloesCarregadosCaminhoes}</span>
          </div>

          <div style={{ fontSize: 12, fontWeight: 500, color: C.textMuted, marginBottom: 6, marginTop: 12 }}>De onde vêm as avarias</div>
          <div className="flex justify-between py-1.5" style={{ fontSize: 13 }}>
            <span>Produção (bonificadas na venda)</span><span style={{ color: C.amber }}>{resultado.totalGaloesBonificados}</span>
          </div>
          <div className="flex justify-between py-1.5" style={{ fontSize: 13 }}>
            <span>Cliente (venda no PDV)</span><span style={{ color: C.amber }}>{resultado.totalGaloesAvariaClientePdv}</span>
          </div>
          <div className="flex justify-between py-1.5" style={{ fontSize: 13 }}>
            <span>Caminhões (prestação de contas)</span><span style={{ color: C.amber }}>{resultado.totalGaloesAvariaCaminhoes}</span>
          </div>
        </Card>
      )}
    </div>
  );
}
