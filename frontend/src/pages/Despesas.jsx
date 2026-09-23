import React, { useEffect, useState } from 'react';
import { Receipt } from 'lucide-react';
import { SectionHeaderWithAction, Card, Field, TextInput, Segmented, ErrorBanner, Loading, Badge, EmptyState, MoneyInput } from '../components/ui';
import Modal from '../components/Modal';
import { C, DISPLAY_FONT } from '../theme';
import { despesasApi } from '../api/despesas';
import { caixaApi } from '../api/caixa';

const VAZIO = { descricao: '', categoria: 'FROTA', valor: '', data: new Date().toISOString().slice(0, 10) };
const CATEGORIA_LABEL = { FROTA: 'Frota', PRODUCAO: 'Produção', INSUMOS: 'Insumos', OUTROS: 'Outros' };
const CATEGORIA_TONE = { FROTA: 'blue', PRODUCAO: 'amber', INSUMOS: 'neutral', OUTROS: 'neutral' };

export default function Despesas() {
  const [despesas, setDespesas] = useState([]);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState(null);
  const [salvando, setSalvando] = useState(false);
  const [modalAberto, setModalAberto] = useState(false);
  const [form, setForm] = useState(VAZIO);
  const [vincularCaixa, setVincularCaixa] = useState('SIM');

  async function carregar() {
    setCarregando(true);
    try {
      setDespesas(await despesasApi.listar());
    } catch (e) {
      setErro(e.message);
    }
    setCarregando(false);
  }

  useEffect(() => { carregar(); }, []);

  function abrirNovo() {
    setForm(VAZIO);
    setVincularCaixa('SIM');
    setModalAberto(true);
  }

  async function salvar() {
    setSalvando(true);
    setErro(null);
    try {
      let caixaId = null;
      if (vincularCaixa === 'SIM') {
        try {
          const caixa = await caixaApi.atual();
          caixaId = caixa?.id ?? null;
        } catch {
          caixaId = null;
        }
      }
      await despesasApi.criar({ ...form, valor: Number(form.valor) }, caixaId);
      setModalAberto(false);
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
      <SectionHeaderWithAction title="Despesas" actionLabel="+ Nova despesa" onAction={abrirNovo} />

      <Modal open={modalAberto} title="Nova despesa" onClose={() => setModalAberto(false)}>
        <ErrorBanner message={erro} />
        <div className="grid grid-cols-2 gap-4">
          <Field label="Descrição"><TextInput value={form.descricao} onChange={(e) => setForm({ ...form, descricao: e.target.value })} /></Field>
          <Field label="Valor"><MoneyInput value={form.valor} onChange={(v) => setForm({ ...form, valor: v })} /></Field>
          <Field label="Categoria">
            <Segmented
              options={[['FROTA', 'Frota'], ['PRODUCAO', 'Produção'], ['INSUMOS', 'Insumos'], ['OUTROS', 'Outros']]}
              value={form.categoria}
              onChange={(v) => setForm({ ...form, categoria: v })}
            />
          </Field>
          <Field label="Data"><TextInput type="date" value={form.data} onChange={(e) => setForm({ ...form, data: e.target.value })} /></Field>
          <Field label="Sai do caixa do dia?">
            <Segmented options={[['SIM', 'Sim'], ['NAO', 'Não']]} value={vincularCaixa} onChange={setVincularCaixa} />
          </Field>
        </div>
        <div className="flex gap-2 mt-6">
          <button type="button" disabled={salvando} onClick={salvar} className="px-4 py-2 rounded text-sm font-medium disabled:opacity-60" style={{ background: C.red, color: '#fff' }}>
            {salvando ? 'Salvando...' : 'Salvar'}
          </button>
          <button type="button" onClick={() => setModalAberto(false)} className="px-4 py-2 rounded text-sm" style={{ background: C.bg, color: C.textDark }}>Cancelar</button>
        </div>
      </Modal>

      {!modalAberto && <ErrorBanner message={erro} />}

      {despesas.length === 0 ? (
        <EmptyState message="Nenhuma despesa registrada ainda." />
      ) : (
        <div className="grid grid-cols-3 gap-4">
          {despesas.map((d) => (
            <Card key={d.id} hover>
              <div className="flex items-start justify-between">
                <div className="flex items-center justify-center rounded-full" style={{ width: 40, height: 40, background: C.blueLight, flexShrink: 0 }}>
                  <Receipt size={18} color={C.blue} />
                </div>
                <Badge tone={CATEGORIA_TONE[d.categoria]}>{CATEGORIA_LABEL[d.categoria]}</Badge>
              </div>
              <div style={{ fontSize: 14, fontWeight: 600, color: C.textDark, marginTop: 10 }}>{d.descricao}</div>
              <div style={{ fontSize: 20, fontWeight: 600, color: C.textDark, fontFamily: DISPLAY_FONT, marginTop: 4 }}>
                {d.valor.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })}
              </div>
              <div style={{ fontSize: 12, color: C.textMuted, marginTop: 4 }}>{new Date(d.data).toLocaleDateString('pt-BR')}</div>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}
