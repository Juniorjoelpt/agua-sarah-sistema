import React, { useEffect, useState } from 'react';
import { Phone, MapPin, Pencil } from 'lucide-react';
import { SectionHeaderWithAction, Card, Field, TextInput, ErrorBanner, Loading, Avatar, Badge, EmptyState } from '../../components/ui';
import Modal from '../../components/Modal';
import { C } from '../../theme';
import { clientesTerceirosApi } from '../../api/terceiros/clientes';

const VAZIO = { nome: '', telefone: '', bairro: '', endereco: '', ativo: true };

export default function ClientesTerceiros() {
  const [clientes, setClientes] = useState([]);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState(null);
  const [salvando, setSalvando] = useState(false);

  const [modalAberto, setModalAberto] = useState(false);
  const [editandoId, setEditandoId] = useState(null);
  const [form, setForm] = useState(VAZIO);

  async function carregar() {
    setCarregando(true);
    try {
      setClientes(await clientesTerceirosApi.listar(false));
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

  function abrirEdicao(c) {
    setEditandoId(c.id);
    setForm({ nome: c.nome, telefone: c.telefone || '', bairro: c.bairro || '', endereco: c.endereco || '', ativo: c.ativo });
    setModalAberto(true);
  }

  async function salvar() {
    setSalvando(true);
    setErro(null);
    try {
      if (editandoId) {
        await clientesTerceirosApi.atualizar(editandoId, form);
      } else {
        await clientesTerceirosApi.criar(form);
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
      <SectionHeaderWithAction title="Clientes" subtitle="Empresas terceiras cadastradas" actionLabel="+ Novo cliente" onAction={abrirNovo} />

      <Modal open={modalAberto} title={editandoId ? 'Editar cliente' : 'Novo cliente'} onClose={() => setModalAberto(false)} maxWidth={480}>
        <ErrorBanner message={erro} />
        <div className="grid grid-cols-2 gap-4">
          <Field label="Nome da empresa"><TextInput value={form.nome} onChange={(e) => setForm({ ...form, nome: e.target.value })} /></Field>
          <Field label="Telefone"><TextInput value={form.telefone} onChange={(e) => setForm({ ...form, telefone: e.target.value })} /></Field>
          <Field label="Bairro"><TextInput value={form.bairro} onChange={(e) => setForm({ ...form, bairro: e.target.value })} /></Field>
          <Field label="Endereço"><TextInput value={form.endereco} onChange={(e) => setForm({ ...form, endereco: e.target.value })} /></Field>
        </div>
        <div className="flex gap-2 mt-6">
          <button type="button" disabled={salvando} onClick={salvar} className="px-4 py-2 rounded text-sm font-medium disabled:opacity-60" style={{ background: C.red, color: '#fff' }}>
            {salvando ? 'Salvando...' : 'Salvar'}
          </button>
          <button type="button" onClick={() => setModalAberto(false)} className="px-4 py-2 rounded text-sm" style={{ background: C.bg, color: C.textDark }}>Cancelar</button>
        </div>
      </Modal>

      {!modalAberto && <ErrorBanner message={erro} />}

      {clientes.length === 0 ? (
        <EmptyState message="Nenhum cliente cadastrado ainda." />
      ) : (
        <div className="grid grid-cols-3 gap-4">
          {clientes.map((c) => (
            <Card key={c.id} hover style={{ position: 'relative' }}>
              <button
                onClick={() => abrirEdicao(c)}
                aria-label="Editar cliente"
                className="flex items-center justify-center"
                style={{ position: 'absolute', top: 12, right: 12, width: 28, height: 28, borderRadius: '50%', background: C.bg, color: C.textMuted }}
              >
                <Pencil size={13} />
              </button>
              <div className="flex items-start gap-3" style={{ paddingRight: 32 }}>
                <Avatar name={c.nome} color={C.blue} />
                <div className="min-w-0">
                  <div style={{ fontSize: 14, fontWeight: 600, color: C.textDark }} className="truncate">{c.nome}</div>
                  {!c.ativo && <Badge tone="neutral">Inativo</Badge>}
                </div>
              </div>
              <div className="flex flex-col gap-1 mt-3" style={{ fontSize: 12, color: C.textMuted }}>
                <div className="flex items-center gap-1.5"><Phone size={12} /> {c.telefone || 'Não informado'}</div>
                <div className="flex items-center gap-1.5"><MapPin size={12} /> {[c.bairro, c.endereco].filter(Boolean).join(' · ') || 'Não informado'}</div>
              </div>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}
