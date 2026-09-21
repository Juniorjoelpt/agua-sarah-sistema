import React, { useEffect, useState } from 'react';
import { Droplet, Package, Pencil } from 'lucide-react';
import { SectionHeaderWithAction, Card, Field, TextInput, Segmented, ErrorBanner, Loading, Badge, EmptyState } from '../components/ui';
import Modal from '../components/Modal';
import { C, DISPLAY_FONT } from '../theme';
import { produtosApi } from '../api/produtos';

const VAZIO = { nome: '', preco: '', contaComoEnvase: false, ativo: true };

export default function Produtos() {
  const [produtos, setProdutos] = useState([]);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState(null);
  const [salvando, setSalvando] = useState(false);
  const [modalAberto, setModalAberto] = useState(false);
  const [editandoId, setEditandoId] = useState(null);
  const [form, setForm] = useState(VAZIO);

  async function carregar() {
    setCarregando(true);
    try {
      setProdutos(await produtosApi.listar(false));
    } catch (e) {
      setErro(e.message);
    }
    setCarregando(false);
  }

  useEffect(() => { carregar(); }, []);

  function abrirNovo() {
    setEditandoId(null);
    setForm(VAZIO);
    setModalAberto(true);
  }

  function abrirEdicao(produto) {
    setEditandoId(produto.id);
    setForm({
      nome: produto.nome,
      preco: String(produto.preco),
      contaComoEnvase: produto.contaComoEnvase,
      ativo: produto.ativo,
    });
    setModalAberto(true);
  }

  async function salvar() {
    setSalvando(true);
    setErro(null);
    try {
      const dto = { ...form, preco: Number(form.preco) };
      if (editandoId) {
        await produtosApi.atualizar(editandoId, dto);
      } else {
        await produtosApi.criar(dto);
      }
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
      <SectionHeaderWithAction title="Produtos" actionLabel="+ Novo produto" onAction={abrirNovo} />

      <Modal open={modalAberto} title={editandoId ? 'Editar produto' : 'Novo produto'} onClose={() => setModalAberto(false)}>
        <ErrorBanner message={erro} />
        <div className="grid grid-cols-2 gap-4">
          <Field label="Nome"><TextInput value={form.nome} onChange={(e) => setForm({ ...form, nome: e.target.value })} /></Field>
          <Field label="Preço"><TextInput type="number" step="0.01" value={form.preco} onChange={(e) => setForm({ ...form, preco: e.target.value })} /></Field>
          <Field label="Conta como envase de água?">
            <Segmented options={[['true', 'Sim'], ['false', 'Não']]} value={String(form.contaComoEnvase)} onChange={(v) => setForm({ ...form, contaComoEnvase: v === 'true' })} />
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

      {produtos.length === 0 ? (
        <EmptyState message="Nenhum produto cadastrado ainda." />
      ) : (
        <div className="grid grid-cols-3 gap-4">
          {produtos.map((p) => (
            <Card key={p.id} hover style={{ position: 'relative' }}>
              <button
                onClick={() => abrirEdicao(p)}
                aria-label="Editar produto"
                className="flex items-center justify-center"
                style={{ position: 'absolute', top: 12, right: 12, width: 28, height: 28, borderRadius: '50%', background: C.bg, color: C.textMuted }}
              >
                <Pencil size={13} />
              </button>
              <div className="flex items-start justify-between" style={{ paddingRight: 28 }}>
                <div className="flex items-center justify-center rounded-full" style={{ width: 40, height: 40, background: C.blueLight, flexShrink: 0 }}>
                  {p.contaComoEnvase ? <Droplet size={18} color={C.blue} /> : <Package size={18} color={C.blue} />}
                </div>
                {p.contaComoEnvase && <Badge tone="blue">Envase</Badge>}
              </div>
              <div style={{ fontSize: 14, fontWeight: 600, color: C.textDark, marginTop: 10 }}>{p.nome}</div>
              <div style={{ fontSize: 20, fontWeight: 600, color: C.textDark, fontFamily: DISPLAY_FONT, marginTop: 4 }}>
                {p.preco.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })}
              </div>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}
