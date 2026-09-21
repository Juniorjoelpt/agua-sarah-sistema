import React, { useEffect, useState } from 'react';
import { Package, Pencil } from 'lucide-react';
import { SectionHeaderWithAction, Card, Field, TextInput, ErrorBanner, Loading, Badge, EmptyState } from '../../components/ui';
import Modal from '../../components/Modal';
import { C } from '../../theme';
import { produtosTerceirosApi } from '../../api/terceiros/produtos';

const VAZIO = { nome: '', preco: '', ativo: true };
const moeda = (v) => (v ?? 0).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });

export default function ProdutosTerceiros() {
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
      setProdutos(await produtosTerceirosApi.listar(false));
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

  function abrirEdicao(p) {
    setEditandoId(p.id);
    setForm({ nome: p.nome, preco: String(p.preco), ativo: p.ativo });
    setModalAberto(true);
  }

  async function salvar() {
    setSalvando(true);
    setErro(null);
    try {
      const dto = { nome: form.nome, preco: Number(form.preco), ativo: form.ativo };
      if (editandoId) {
        await produtosTerceirosApi.atualizar(editandoId, dto);
      } else {
        await produtosTerceirosApi.criar(dto);
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
      <SectionHeaderWithAction title="Produtos" subtitle="Itens vendidos às empresas terceiras" actionLabel="+ Novo produto" onAction={abrirNovo} />

      <Modal open={modalAberto} title={editandoId ? 'Editar produto' : 'Novo produto'} onClose={() => setModalAberto(false)} maxWidth={420}>
        <ErrorBanner message={erro} />
        <div className="grid grid-cols-2 gap-4">
          <Field label="Nome"><TextInput value={form.nome} onChange={(e) => setForm({ ...form, nome: e.target.value })} /></Field>
          <Field label="Preço"><TextInput type="number" step="0.01" value={form.preco} onChange={(e) => setForm({ ...form, preco: e.target.value })} /></Field>
        </div>
        <div className="flex gap-2 mt-6">
          <button type="button" disabled={salvando || !form.nome || !form.preco} onClick={salvar} className="px-4 py-2 rounded text-sm font-medium disabled:opacity-60" style={{ background: C.red, color: '#fff' }}>
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
              <div className="flex items-center gap-3" style={{ paddingRight: 32 }}>
                <div className="flex items-center justify-center rounded-full" style={{ width: 36, height: 36, background: C.blueLight, flexShrink: 0 }}>
                  <Package size={16} color={C.blue} />
                </div>
                <div className="min-w-0">
                  <div style={{ fontSize: 14, fontWeight: 600, color: C.textDark }} className="truncate">{p.nome}</div>
                  <div style={{ fontSize: 13, color: C.textMuted }}>{moeda(p.preco)}</div>
                </div>
              </div>
              {!p.ativo && <Badge tone="neutral">Inativo</Badge>}
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}
