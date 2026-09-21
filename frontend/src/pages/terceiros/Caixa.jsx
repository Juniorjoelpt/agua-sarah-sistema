import React, { useEffect, useState } from 'react';
import { PageHeader, Card, Field, TextInput, ErrorBanner, Loading } from '../../components/ui';
import { C, DISPLAY_FONT } from '../../theme';
import { caixaTerceirosApi } from '../../api/terceiros/caixa';

const moeda = (v) => (v ?? 0).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });

export default function CaixaTerceiros() {
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
      const c = await caixaTerceirosApi.atual();
      setCaixa(c);
      if (c?.id) setResumo(await caixaTerceirosApi.resumo(c.id));
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
      await caixaTerceirosApi.abrir({ saldoInicialEspecie: Number(especie), saldoInicialPix: Number(pix) });
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
      await caixaTerceirosApi.fechar(caixa.id);
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
      <PageHeader title="Caixa de Terceiros" subtitle="Abertura, fechamento e resumo do caixa isolado deste módulo" />
      <ErrorBanner message={erro} />

      {!caixa ? (
        <Card>
          <div style={{ fontSize: 13, fontWeight: 500, color: C.textDark, marginBottom: 12 }}>Abrir caixa</div>
          <div className="grid grid-cols-2 gap-4">
            <Field label="Saldo inicial — Espécie"><TextInput type="number" value={especie} onChange={(e) => setEspecie(e.target.value)} /></Field>
            <Field label="Saldo inicial — PIX"><TextInput type="number" value={pix} onChange={(e) => setPix(e.target.value)} /></Field>
          </div>
          <button disabled={salvando} onClick={abrir} className="mt-4 px-4 py-2 rounded text-sm font-medium disabled:opacity-60" style={{ background: C.red, color: '#fff' }}>
            {salvando ? 'Abrindo...' : 'Abrir caixa'}
          </button>
        </Card>
      ) : (
        <Card>
          <div className="flex items-center justify-between mb-4">
            <div style={{ fontSize: 13, fontWeight: 500, color: C.textDark }}>
              Caixa #{caixa.id} aberto desde {new Date(caixa.dataAbertura).toLocaleString('pt-BR')}
            </div>
            <button disabled={salvando} onClick={fechar} className="px-4 py-2 rounded text-sm font-medium disabled:opacity-60" style={{ background: C.red, color: '#fff' }}>
              {salvando ? 'Fechando...' : 'Fechar caixa'}
            </button>
          </div>
          {resumo && (
            <div className="flex flex-col gap-2" style={{ fontSize: 13 }}>
              <div className="flex justify-between"><span style={{ color: C.textMuted }}>Entradas em espécie</span><span style={{ color: C.textDark }}>{moeda(resumo.totalVendasEspecie)}</span></div>
              <div className="flex justify-between"><span style={{ color: C.textMuted }}>Entradas em PIX</span><span style={{ color: C.textDark }}>{moeda(resumo.totalVendasPix)}</span></div>
              {(resumo.totalRecebimentosContasReceberEspecie > 0 || resumo.totalRecebimentosContasReceberPix > 0) && (
                <div className="flex justify-between"><span style={{ color: C.textMuted }}>Recebimentos de fiado</span><span style={{ color: C.blue }}>{moeda(resumo.totalRecebimentosContasReceberEspecie + resumo.totalRecebimentosContasReceberPix)}</span></div>
              )}
              {resumo.totalLucroFrota !== 0 && (
                <div className="flex justify-between"><span style={{ color: C.textMuted }}>Lucro de frota (prestações de contas)</span><span style={{ color: C.blue }}>{moeda(resumo.totalLucroFrota)}</span></div>
              )}
              <div className="flex justify-between"><span style={{ color: C.textMuted }}>Despesas</span><span style={{ color: C.amber }}>{moeda(resumo.totalDespesas)}</span></div>
              <div className="flex justify-between pt-2" style={{ borderTop: `1px solid ${C.border}`, fontWeight: 600 }}>
                <span style={{ color: C.textDark }}>Saldo final espécie</span>
                <span style={{ color: C.textDark, fontFamily: DISPLAY_FONT }}>{moeda(resumo.saldoFinalEspecie)}</span>
              </div>
              <div className="flex justify-between">
                <span style={{ color: C.textDark, fontWeight: 600 }}>Saldo final PIX</span>
                <span style={{ color: C.textDark, fontFamily: DISPLAY_FONT, fontWeight: 600 }}>{moeda(resumo.saldoFinalPix)}</span>
              </div>
            </div>
          )}
        </Card>
      )}
    </div>
  );
}
