import React, { useEffect, useState } from 'react';
import { motion } from 'framer-motion';
import { ResponsiveContainer, BarChart, Bar, XAxis, YAxis, Tooltip, CartesianGrid } from 'recharts';
import { TrendingUp, TrendingDown, Trophy, Users } from 'lucide-react';
import { PageHeader, Card, Loading } from '../components/ui';
import { DISPLAY_FONT, C } from '../theme';
import { caixaApi } from '../api/caixa';
import { estoqueApi } from '../api/estoque';
import { contasPagarApi } from '../api/contasPagar';
import { dashboardApi } from '../api/dashboard';

const container = {
  hidden: { opacity: 0 },
  show: { opacity: 1, transition: { staggerChildren: 0.06 } },
};
const item = {
  hidden: { opacity: 0, y: 10 },
  show: { opacity: 1, y: 0, transition: { duration: 0.3, ease: 'easeOut' } },
};

const moeda = (v) => (v ?? 0).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
const moedaCompacta = (v) => (v ?? 0).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL', notation: 'compact' });

export default function Dashboard() {
  const [caixa, setCaixa] = useState(null);
  const [resumoCaixa, setResumoCaixa] = useState(null);
  const [insumos, setInsumos] = useState([]);
  const [contasVencidas, setContasVencidas] = useState([]);
  const [dash, setDash] = useState(null);
  const [carregando, setCarregando] = useState(true);

  useEffect(() => {
    async function carregar() {
      setCarregando(true);
      try {
        const c = await caixaApi.atual();
        setCaixa(c);
        if (c?.id) setResumoCaixa(await caixaApi.resumo(c.id));
      } catch {
        setCaixa(null); // nenhum caixa aberto - nao e erro
      }
      try {
        setInsumos(await estoqueApi.listarInsumos());
      } catch {
        setInsumos([]);
      }
      try {
        setContasVencidas(await contasPagarApi.listarVencidas());
      } catch {
        setContasVencidas([]);
      }
      try {
        setDash(await dashboardApi.resumo());
      } catch {
        setDash(null);
      }
      setCarregando(false);
    }
    carregar();
  }, []);

  if (carregando) return <Loading />;

  const insumosBaixos = insumos.filter((i) => i.quantidadeAtual <= i.quantidadeMinima);
  const dadosGrafico = (dash?.vendasUltimos7Dias || []).map((p) => ({
    ...p,
    label: new Date(p.data + 'T00:00:00').toLocaleDateString('pt-BR', { weekday: 'short' }).replace('.', ''),
  }));
  const variacao = dash?.variacaoPercentualMes;

  return (
    <motion.div variants={container} initial="hidden" animate="show">
      <PageHeader title="Painel" subtitle={new Date().toLocaleDateString('pt-BR', { weekday: 'long', day: 'numeric', month: 'long' })} />

      {/* ---- linha 1: status do dia ---- */}
      <div className="grid grid-cols-4 gap-4 mb-4">
        <motion.div variants={item}>
          <Card hover>
            <div style={{ fontSize: 12, color: C.textMuted }}>Status do caixa</div>
            {caixa ? (
              <>
                <div style={{ fontSize: 22, fontWeight: 500, color: C.textDark, fontFamily: DISPLAY_FONT }}>Aberto</div>
                <div style={{ fontSize: 12, color: C.blue, marginTop: 4 }}>desde {new Date(caixa.dataAbertura).toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })}</div>
              </>
            ) : (
              <>
                <div style={{ fontSize: 22, fontWeight: 500, color: C.textDark, fontFamily: DISPLAY_FONT }}>Fechado</div>
                <div style={{ fontSize: 12, color: C.textMuted, marginTop: 4 }}>Abra o caixa para vender</div>
              </>
            )}
          </Card>
        </motion.div>
        <motion.div variants={item}>
          <Card hover>
            <div style={{ fontSize: 12, color: C.textMuted }}>Entradas em espécie</div>
            <div style={{ fontSize: 22, fontWeight: 500, color: C.textDark, fontFamily: DISPLAY_FONT }}>
              {resumoCaixa ? moeda(resumoCaixa.totalVendasEspecie) : '—'}
            </div>
          </Card>
        </motion.div>
        <motion.div variants={item}>
          <Card hover>
            <div style={{ fontSize: 12, color: C.textMuted }}>Entradas em PIX</div>
            <div style={{ fontSize: 22, fontWeight: 500, color: C.textDark, fontFamily: DISPLAY_FONT }}>
              {resumoCaixa ? moeda(resumoCaixa.totalVendasPix) : '—'}
            </div>
          </Card>
        </motion.div>
        <motion.div variants={item}>
          <Card hover>
            <div style={{ fontSize: 12, color: C.textMuted }}>Vendas hoje</div>
            <div style={{ fontSize: 22, fontWeight: 500, color: C.textDark, fontFamily: DISPLAY_FONT }}>
              {dash ? dash.vendasHojeQuantidade : '—'}
            </div>
            <div style={{ fontSize: 12, color: C.textMuted, marginTop: 4 }}>
              {dash ? `Ticket médio: ${moeda(dash.ticketMedioHoje)}` : ''}
            </div>
          </Card>
        </motion.div>
      </div>

      {/* ---- linha 2: resumo do mes ---- */}
      <div className="grid grid-cols-3 gap-4 mb-4">
        <motion.div variants={item}>
          <Card hover>
            <div style={{ fontSize: 12, color: C.textMuted }}>Vendido no mês</div>
            <div style={{ fontSize: 22, fontWeight: 500, color: C.textDark, fontFamily: DISPLAY_FONT }}>
              {dash ? moeda(dash.totalVendidoMesAtual) : '—'}
            </div>
            {dash && variacao !== null && variacao !== undefined && (
              <div className="flex items-center gap-1 mt-1" style={{ fontSize: 12, color: variacao >= 0 ? '#1E7A4C' : C.red }}>
                {variacao >= 0 ? <TrendingUp size={13} /> : <TrendingDown size={13} />}
                {Math.abs(variacao).toFixed(1)}% vs. mês anterior
              </div>
            )}
          </Card>
        </motion.div>
        <motion.div variants={item}>
          <Card hover>
            <div style={{ fontSize: 12, color: C.textMuted }}>Despesas no mês</div>
            <div style={{ fontSize: 22, fontWeight: 500, color: C.textDark, fontFamily: DISPLAY_FONT }}>
              {dash ? moeda(dash.totalDespesasMesAtual) : '—'}
            </div>
          </Card>
        </motion.div>
        <motion.div variants={item}>
          <Card hover>
            <div style={{ fontSize: 12, color: C.textMuted }}>Lucro bruto no mês</div>
            <div style={{ fontSize: 22, fontWeight: 500, color: C.textDark, fontFamily: DISPLAY_FONT }}>
              {dash ? moeda(dash.lucroBrutoMesAtual) : '—'}
            </div>
          </Card>
        </motion.div>
      </div>

      {/* ---- grafico dos ultimos 7 dias + rankings ---- */}
      <div className="grid grid-cols-3 gap-4 mb-4">
        <motion.div variants={item} style={{ gridColumn: 'span 2' }}>
          <Card>
            <div style={{ fontSize: 13, fontWeight: 500, color: C.textDark, marginBottom: 12 }}>Vendas dos últimos 7 dias</div>
            {dadosGrafico.length === 0 ? (
              <div style={{ fontSize: 13, color: C.textMuted }}>Sem vendas registradas nesse período.</div>
            ) : (
              <div style={{ width: '100%', height: 220 }}>
                <ResponsiveContainer>
                  <BarChart data={dadosGrafico} margin={{ top: 4, right: 8, left: 0, bottom: 0 }}>
                    <CartesianGrid strokeDasharray="3 3" stroke={C.border} vertical={false} />
                    <XAxis dataKey="label" tick={{ fontSize: 12, fill: C.textMuted }} axisLine={{ stroke: C.border }} tickLine={false} />
                    <YAxis tickFormatter={moedaCompacta} tick={{ fontSize: 11, fill: C.textMuted }} axisLine={false} tickLine={false} width={54} />
                    <Tooltip
                      formatter={(v, n) => [n === 'totalVendido' ? moeda(v) : v, n === 'totalVendido' ? 'Vendido' : 'Vendas']}
                      labelFormatter={(_, payload) => payload?.[0]?.payload?.data ? new Date(payload[0].payload.data + 'T00:00:00').toLocaleDateString('pt-BR') : ''}
                      contentStyle={{ fontSize: 12, borderRadius: 8, border: `1px solid ${C.border}` }}
                    />
                    <Bar dataKey="totalVendido" fill={C.blue} radius={[4, 4, 0, 0]} />
                  </BarChart>
                </ResponsiveContainer>
              </div>
            )}
          </Card>
        </motion.div>

        <motion.div variants={item} className="flex flex-col gap-4">
          <Card>
            <div className="flex items-center gap-1.5 mb-2" style={{ fontSize: 12, fontWeight: 500, color: C.textDark }}>
              <Trophy size={13} color={C.amber} /> Mais vendidos no mês
            </div>
            {!dash || dash.topProdutosMes.length === 0 ? (
              <div style={{ fontSize: 12, color: C.textMuted }}>Sem vendas este mês ainda.</div>
            ) : (
              <div className="flex flex-col gap-1.5">
                {dash.topProdutosMes.map((p, idx) => (
                  <div key={p.nome} className="flex justify-between items-center" style={{ fontSize: 12 }}>
                    <span style={{ color: C.textDark }} className="truncate">{idx + 1}. {p.nome}</span>
                    <span style={{ color: C.textMuted, flexShrink: 0, marginLeft: 8 }}>{p.quantidadeVendida}x</span>
                  </div>
                ))}
              </div>
            )}
          </Card>
          <Card>
            <div className="flex items-center gap-1.5 mb-2" style={{ fontSize: 12, fontWeight: 500, color: C.textDark }}>
              <Users size={13} color={C.blue} /> Clientes que mais compraram
            </div>
            {!dash || dash.topClientesMes.length === 0 ? (
              <div style={{ fontSize: 12, color: C.textMuted }}>Sem vendas a clientes cadastrados este mês.</div>
            ) : (
              <div className="flex flex-col gap-1.5">
                {dash.topClientesMes.map((c, idx) => (
                  <div key={c.nome} className="flex justify-between items-center" style={{ fontSize: 12 }}>
                    <span style={{ color: C.textDark }} className="truncate">{idx + 1}. {c.nome}</span>
                    <span style={{ color: C.textMuted, flexShrink: 0, marginLeft: 8 }}>{moeda(c.valorTotal)}</span>
                  </div>
                ))}
              </div>
            )}
          </Card>
        </motion.div>
      </div>

      {/* ---- pendencias financeiras ---- */}
      {dash && (
        <div className="grid grid-cols-3 gap-4 mb-4">
          <motion.div variants={item}>
            <Card>
              <div style={{ fontSize: 12, color: C.textMuted }}>Contas a receber em aberto</div>
              <div style={{ fontSize: 18, fontWeight: 600, color: C.textDark, fontFamily: DISPLAY_FONT }}>{moeda(dash.totalContasReceberAberto)}</div>
            </Card>
          </motion.div>
          <motion.div variants={item}>
            <Card>
              <div style={{ fontSize: 12, color: C.textMuted }}>Contas a pagar em aberto</div>
              <div style={{ fontSize: 18, fontWeight: 600, color: C.textDark, fontFamily: DISPLAY_FONT }}>{moeda(dash.totalContasPagarAberto)}</div>
            </Card>
          </motion.div>
          <motion.div variants={item}>
            <Card>
              <div style={{ fontSize: 12, color: C.textMuted }}>Orçamentos pendentes</div>
              <div style={{ fontSize: 18, fontWeight: 600, color: C.textDark, fontFamily: DISPLAY_FONT }}>{dash.orcamentosPendentesQuantidade}</div>
            </Card>
          </motion.div>
        </div>
      )}

      {/* ---- alertas ---- */}
      {contasVencidas.length > 0 && (
        <motion.div variants={item} className="mb-4">
          <Card style={{ background: C.redLight }}>
            <div style={{ fontSize: 13, fontWeight: 500, color: '#7A1E17', marginBottom: 10 }}>
              Contas a pagar vencidas ({contasVencidas.length})
            </div>
            <div className="flex flex-col gap-2">
              {contasVencidas.map((c) => (
                <div key={c.id} className="flex justify-between items-center py-2" style={{ borderBottom: '1px solid rgba(122,30,23,0.15)', fontSize: 13 }}>
                  <span style={{ color: '#7A1E17' }}>{c.descricao}</span>
                  <span style={{ color: '#7A1E17', fontWeight: 600 }}>{moeda(c.valorOriginal - c.valorPago)}</span>
                </div>
              ))}
            </div>
          </Card>
        </motion.div>
      )}
      <motion.div variants={item}>
        <Card>
          <div style={{ fontSize: 13, fontWeight: 500, color: C.textDark, marginBottom: 10 }}>Estoque de insumos com atenção</div>
          {insumosBaixos.length === 0 ? (
            <div style={{ fontSize: 13, color: C.textMuted }}>Nenhum insumo abaixo do mínimo cadastrado.</div>
          ) : (
            <div className="flex flex-col gap-2">
              {insumosBaixos.map((i) => (
                <div key={i.id} className="flex justify-between items-center py-2" style={{ borderBottom: `1px solid ${C.border}`, fontSize: 13 }}>
                  <span style={{ color: C.textDark }}>{i.nome}</span>
                  <span style={{ color: C.amber, fontWeight: 500 }}>{i.quantidadeAtual} {i.unidadeMedida}</span>
                </div>
              ))}
            </div>
          )}
        </Card>
      </motion.div>
    </motion.div>
  );
}
