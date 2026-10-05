import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { History } from 'lucide-react';
import { Card, Field, ErrorBanner, Loading, MoneyInput } from '../components/ui';
import { C, DISPLAY_FONT } from '../theme';
import { caixaApi } from '../api/caixa';

const moeda = (v) => (v ?? 0).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });

export default function Caixa() {
  const navigate = useNavigate();
  const [caixa, setCaixa] = useState(null);
  const [resumo, setResumo] = useState(null);
  const [carregando, setCarregando] = useState(true);
  const [salvando, setSalvando] = useState(false);
  const [erro, setErro] = useState(null);

  const [especie, setEspecie] = useState('0');
  const [pix, setPix] = useState('0');

  async function carregar() {
    setCarregando(true);
    try {
      const c = await caixaApi.atual();
      setCaixa(c);
      if (c?.id) setResumo(await caixaApi.resumo(c.id));
    } catch {
      setCaixa(null);
      setResumo(null);
    }
    setCarregando(false);
  }

  useEffect(() => { carregar(); }, []);

  async function abrir() {
    setSalvando(true);
    setErro(null);
    try {
      await caixaApi.abrir({ saldoInicialEspecie: Number(especie), saldoInicialPix: Number(pix) });
      await carregar();
    } catch (e) {
      setErro(e.message);
    } finally {
      setSalvando(false);
    }
  }

  async function fechar() {
    setSalvando(true);
    setErro(null);
    try {
      await caixaApi.fechar(caixa.id);
      await carregar();
    } catch (e) {
      setErro(e.message);
    } finally {
      setSalvando(false);
    }
  }

  if (carregando) return <Loading />;

  return (
    <div>
      <div className="flex items-start justify-between mb-6">
        <div>
          <h2 style={{ fontFamily: DISPLAY_FONT, fontSize: 22, fontWeight: 500, color: C.onDark }}>Caixa</h2>
          <p style={{ fontSize: 13, color: C.onDarkMuted, marginTop: 2 }}>
            {caixa ? `Caixa #${caixa.id} aberto desde ${new Date(caixa.dataAbertura).toLocaleString('pt-BR')}` : 'Nenhum caixa aberto no momento'}
          </p>
        </div>
        <button
          onClick={() => navigate('/caixa/historico')}
          className="flex items-center gap-1.5 px-4 py-2 rounded text-sm"
          style={{ background: C.bg, color: C.textDark, border: `1px solid ${C.border}`, flexShrink: 0 }}
        >
          <History size={14} /> Histórico
        </button>
      </div>

      <ErrorBanner message={erro} />

      <div className="grid grid-cols-2 gap-4">
        <Card>
          <div style={{ fontSize: 13, fontWeight: 500, color: C.textDark, marginBottom: 12 }}>Abertura de caixa</div>
          {caixa ? (
            <div style={{ fontSize: 13, color: C.textMuted }}>Já existe um caixa aberto. Feche-o antes de abrir outro.</div>
          ) : (
            <>
              <Field label="Saldo inicial - Espécie">
                <MoneyInput value={especie} onChange={setEspecie} />
              </Field>
              <div className="mb-3" />
              <Field label="Saldo inicial - PIX">
                <MoneyInput value={pix} onChange={setPix} />
              </Field>
              <button onClick={abrir} disabled={salvando} className="w-full mt-4 py-2 rounded text-sm font-medium disabled:opacity-60" style={{ background: C.red, color: '#fff' }}>
                {salvando ? 'Abrindo...' : 'Abrir caixa'}
              </button>
            </>
          )}
        </Card>
        <Card>
          <div style={{ fontSize: 13, fontWeight: 500, color: C.textDark, marginBottom: 12 }}>Fechamento de caixa</div>
          {!caixa || !resumo ? (
            <div style={{ fontSize: 13, color: C.textMuted }}>Abra um caixa para ver o resumo do dia.</div>
          ) : (
            <>
              <div className="flex flex-col gap-2 mb-4" style={{ fontSize: 13 }}>
                <div className="flex justify-between"><span style={{ color: C.textMuted }}>Entradas em espécie</span><span style={{ color: C.textDark }}>{moeda(resumo.totalVendasEspecie)}</span></div>
                <div className="flex justify-between"><span style={{ color: C.textMuted }}>Entradas em PIX</span><span style={{ color: C.textDark }}>{moeda(resumo.totalVendasPix)}</span></div>
                {(resumo.totalRecebimentosContasReceberEspecie > 0 || resumo.totalRecebimentosContasReceberPix > 0) && (
                  <div className="flex justify-between"><span style={{ color: C.textMuted }}>Recebimentos de fiado</span><span style={{ color: C.blue }}>{moeda(resumo.totalRecebimentosContasReceberEspecie + resumo.totalRecebimentosContasReceberPix)}</span></div>
                )}
                <div className="flex justify-between"><span style={{ color: C.textMuted }}>Despesas do caixa</span><span style={{ color: C.amber }}>{moeda(resumo.totalDespesas)}</span></div>
                <div className="flex justify-between"><span style={{ color: C.textMuted }}>Galões bonificados</span><span style={{ color: C.amber }}>{resumo.totalGaloesBonificados}</span></div>
                <div className="flex justify-between pt-2" style={{ borderTop: `1px solid ${C.border}`, fontWeight: 500 }}>
                  <span style={{ color: C.textDark }}>Saldo final (espécie)</span><span style={{ color: C.textDark }}>{moeda(resumo.saldoFinalEspecie)}</span>
                </div>
                <div className="flex justify-between">
                  <span style={{ color: C.textDark }}>Saldo final (PIX)</span><span style={{ color: C.textDark }}>{moeda(resumo.saldoFinalPix)}</span>
                </div>
              </div>
              {caixa.status === 'ABERTO' && (
                <button onClick={fechar} disabled={salvando} className="w-full py-2 rounded text-sm font-medium disabled:opacity-60" style={{ background: C.ink, color: '#fff' }}>
                  {salvando ? 'Fechando...' : 'Fechar caixa'}
                </button>
              )}
            </>
          )}
        </Card>
      </div>
    </div>
  );
}
