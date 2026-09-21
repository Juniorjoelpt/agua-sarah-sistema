import React, { useEffect, useState } from 'react';
import { Archive } from 'lucide-react';
import { SectionHeaderWithAction, Card, Field, TextInput, Segmented, ErrorBanner, Loading, Badge, EmptyState } from '../components/ui';
import Modal from '../components/Modal';
import { C, DISPLAY_FONT } from '../theme';
import { estoqueApi } from '../api/estoque';
import { fornecedoresApi } from '../api/fornecedores';

const INSUMO_VAZIO = { nome: '', unidadeMedida: 'un', quantidadeMinima: '0' };
const MOV_VAZIA = { insumoId: '', tipo: 'ENTRADA', quantidade: '', fornecedorId: '', observacao: '' };

export default function Estoque() {
  const [insumos, setInsumos] = useState([]);
  const [fornecedores, setFornecedores] = useState([]);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState(null);
  const [salvando, setSalvando] = useState(false);

  const [modalInsumo, setModalInsumo] = useState(false);
  const [modalMov, setModalMov] = useState(false);
  const [novoInsumo, setNovoInsumo] = useState(INSUMO_VAZIO);
  const [novaMov, setNovaMov] = useState(MOV_VAZIA);

  async function carregar() {
    setCarregando(true);
    try {
      const [i, f] = await Promise.all([estoqueApi.listarInsumos(), fornecedoresApi.listar()]);
      setInsumos(i);
      setFornecedores(f);
    } catch (e) {
      setErro(e.message);
    }
    setCarregando(false);
  }

  useEffect(() => { carregar(); }, []);

  function abrirNovoInsumo() {
    setNovoInsumo(INSUMO_VAZIO);
    setModalInsumo(true);
  }

  function abrirNovaMovimentacao() {
    setNovaMov(MOV_VAZIA);
    setModalMov(true);
  }

  async function salvarInsumo() {
    setSalvando(true);
    setErro(null);
    try {
      await estoqueApi.criarInsumo({ ...novoInsumo, quantidadeMinima: Number(novoInsumo.quantidadeMinima), quantidadeAtual: 0 });
      setModalInsumo(false);
      await carregar();
    } catch (e) {
      setErro(e.message);
    } finally {
      setSalvando(false);
    }
  }

  async function salvarMovimentacao() {
    setSalvando(true);
    setErro(null);
    try {
      await estoqueApi.registrarMovimentacao({
        ...novaMov,
        insumoId: Number(novaMov.insumoId),
        quantidade: Number(novaMov.quantidade),
        fornecedorId: novaMov.tipo === 'ENTRADA' && novaMov.fornecedorId ? Number(novaMov.fornecedorId) : null,
      });
      setModalMov(false);
      await carregar();
    } catch (e) {
      setErro(e.message);
    } finally {
      setSalvando(false);
    }
  }

  if (carregando) return <Loading />;

  const algumModalAberto = modalInsumo || modalMov;

  return (
    <div>
      <div className="flex items-start justify-between mb-6">
        <div>
          <h2 style={{ fontFamily: DISPLAY_FONT, fontSize: 22, fontWeight: 500, color: C.onDark }}>Estoque de insumos</h2>
          <p style={{ fontSize: 13, color: C.onDarkMuted, marginTop: 2 }}>Controle manual, sem baixa automática por venda</p>
        </div>
        <div className="flex gap-2" style={{ flexShrink: 0 }}>
          <button onClick={abrirNovoInsumo} className="px-4 py-2 rounded text-sm" style={{ background: C.bg, color: C.textDark, border: `1px solid ${C.border}` }}>+ Novo insumo</button>
          <button onClick={abrirNovaMovimentacao} className="px-4 py-2 rounded text-sm font-medium" style={{ background: C.red, color: '#fff' }}>+ Nova movimentação</button>
        </div>
      </div>

      <Modal open={modalInsumo} title="Novo insumo" onClose={() => setModalInsumo(false)}>
        <ErrorBanner message={erro} />
        <div className="grid grid-cols-2 gap-4">
          <Field label="Nome"><TextInput value={novoInsumo.nome} onChange={(e) => setNovoInsumo({ ...novoInsumo, nome: e.target.value })} /></Field>
          <Field label="Unidade de medida"><TextInput value={novoInsumo.unidadeMedida} onChange={(e) => setNovoInsumo({ ...novoInsumo, unidadeMedida: e.target.value })} /></Field>
          <Field label="Quantidade mínima (alerta)"><TextInput type="number" value={novoInsumo.quantidadeMinima} onChange={(e) => setNovoInsumo({ ...novoInsumo, quantidadeMinima: e.target.value })} /></Field>
        </div>
        <div className="flex gap-2 mt-6">
          <button type="button" disabled={salvando} onClick={salvarInsumo} className="px-4 py-2 rounded text-sm font-medium disabled:opacity-60" style={{ background: C.red, color: '#fff' }}>
            {salvando ? 'Salvando...' : 'Salvar'}
          </button>
          <button type="button" onClick={() => setModalInsumo(false)} className="px-4 py-2 rounded text-sm" style={{ background: C.bg, color: C.textDark }}>Cancelar</button>
        </div>
      </Modal>

      <Modal open={modalMov} title="Nova movimentação" onClose={() => setModalMov(false)}>
        <ErrorBanner message={erro} />
        <div className="grid grid-cols-2 gap-4">
          <Field label="Insumo">
            <select className="w-full px-3 py-2 rounded border" style={{ borderColor: C.border, fontSize: 14 }} value={novaMov.insumoId} onChange={(e) => setNovaMov({ ...novaMov, insumoId: e.target.value })}>
              <option value="">Selecione</option>
              {insumos.map((i) => <option key={i.id} value={i.id}>{i.nome}</option>)}
            </select>
          </Field>
          <Field label="Tipo">
            <Segmented options={[['ENTRADA', 'Entrada'], ['SAIDA', 'Saída']]} value={novaMov.tipo} onChange={(v) => setNovaMov({ ...novaMov, tipo: v })} />
          </Field>
          <Field label="Quantidade"><TextInput type="number" value={novaMov.quantidade} onChange={(e) => setNovaMov({ ...novaMov, quantidade: e.target.value })} /></Field>
          {novaMov.tipo === 'ENTRADA' && (
            <Field label="Fornecedor (opcional)">
              <select className="w-full px-3 py-2 rounded border" style={{ borderColor: C.border, fontSize: 14 }} value={novaMov.fornecedorId} onChange={(e) => setNovaMov({ ...novaMov, fornecedorId: e.target.value })}>
                <option value="">Não informado</option>
                {fornecedores.map((f) => <option key={f.id} value={f.id}>{f.nome}</option>)}
              </select>
            </Field>
          )}
          <Field label="Observação"><TextInput value={novaMov.observacao} onChange={(e) => setNovaMov({ ...novaMov, observacao: e.target.value })} /></Field>
        </div>
        <div className="flex gap-2 mt-6">
          <button type="button" disabled={salvando} onClick={salvarMovimentacao} className="px-4 py-2 rounded text-sm font-medium disabled:opacity-60" style={{ background: C.red, color: '#fff' }}>
            {salvando ? 'Salvando...' : 'Salvar'}
          </button>
          <button type="button" onClick={() => setModalMov(false)} className="px-4 py-2 rounded text-sm" style={{ background: C.bg, color: C.textDark }}>Cancelar</button>
        </div>
      </Modal>

      {!algumModalAberto && <ErrorBanner message={erro} />}

      {insumos.length === 0 ? (
        <EmptyState message="Nenhum insumo cadastrado ainda." />
      ) : (
        <div className="grid grid-cols-3 gap-4">
          {insumos.map((i) => {
            const baixo = i.quantidadeAtual <= i.quantidadeMinima;
            return (
              <Card key={i.id} hover>
                <div className="flex items-start justify-between">
                  <div className="flex items-center justify-center rounded-full" style={{ width: 40, height: 40, background: baixo ? C.amberLight : C.blueLight, flexShrink: 0 }}>
                    <Archive size={18} color={baixo ? C.amber : C.blue} />
                  </div>
                  <Badge tone={baixo ? 'amber' : 'blue'}>{baixo ? 'Baixo' : 'Normal'}</Badge>
                </div>
                <div style={{ fontSize: 14, fontWeight: 600, color: C.textDark, marginTop: 10 }}>{i.nome}</div>
                <div style={{ fontSize: 20, fontWeight: 600, color: C.textDark, fontFamily: DISPLAY_FONT, marginTop: 4 }}>
                  {i.quantidadeAtual} <span style={{ fontSize: 13, fontWeight: 400, color: C.textMuted }}>{i.unidadeMedida}</span>
                </div>
              </Card>
            );
          })}
        </div>
      )}
    </div>
  );
}
