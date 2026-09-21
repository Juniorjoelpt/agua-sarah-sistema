import React, { useEffect, useState } from 'react';
import { Pencil, Trash2 } from 'lucide-react';
import { SectionHeaderWithAction, Card, Field, TextInput, Segmented, ErrorBanner, Loading, Avatar, Badge, EmptyState } from '../components/ui';
import Modal from '../components/Modal';
import { C } from '../theme';
import { usuariosApi } from '../api/usuarios';
import { useAuth } from '../context/AuthContext';

const VAZIO = { nome: '', login: '', senha: '', perfil: 'OPERADOR', ativo: true };

export default function Usuarios() {
  const { usuario: usuarioLogado } = useAuth();
  const [usuarios, setUsuarios] = useState([]);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState(null);
  const [salvando, setSalvando] = useState(false);

  const [modalAberto, setModalAberto] = useState(false);
  const [editando, setEditando] = useState(null); // usuario sendo editado, ou null = criando
  const [form, setForm] = useState(VAZIO);

  const [paraExcluir, setParaExcluir] = useState(null);
  const [excluindo, setExcluindo] = useState(false);

  async function carregar() {
    setCarregando(true);
    try {
      setUsuarios(await usuariosApi.listar());
    } catch (e) {
      setErro(e.message);
    }
    setCarregando(false);
  }

  useEffect(() => { carregar(); }, []);

  function abrirNovo() {
    setEditando(null);
    setForm(VAZIO);
    setModalAberto(true);
  }

  function abrirEdicao(u) {
    setEditando(u);
    setForm({ nome: u.nome, login: u.login, senha: '', perfil: u.perfil, ativo: u.ativo });
    setModalAberto(true);
  }

  async function salvar() {
    setSalvando(true);
    setErro(null);
    try {
      if (editando) {
        await usuariosApi.atualizar(editando.id, form);
      } else {
        await usuariosApi.criar(form);
      }
      setModalAberto(false);
      await carregar();
    } catch (e) {
      setErro(e.message);
    } finally {
      setSalvando(false);
    }
  }

  async function confirmarExclusao() {
    setExcluindo(true);
    setErro(null);
    try {
      await usuariosApi.inativar(paraExcluir.id);
      setParaExcluir(null);
      await carregar();
    } catch (e) {
      setErro(e.message);
    } finally {
      setExcluindo(false);
    }
  }

  if (carregando) return <Loading />;

  return (
    <div>
      <SectionHeaderWithAction title="Usuários" subtitle="Login e nível de acesso da equipe" actionLabel="+ Novo usuário" onAction={abrirNovo} />

      <Modal open={modalAberto} title={editando ? 'Editar usuário' : 'Novo usuário'} onClose={() => setModalAberto(false)}>
        <ErrorBanner message={erro} />
        <div className="grid grid-cols-2 gap-4">
          <Field label="Nome"><TextInput value={form.nome} onChange={(e) => setForm({ ...form, nome: e.target.value })} /></Field>
          <Field label="Login"><TextInput value={form.login} onChange={(e) => setForm({ ...form, login: e.target.value })} /></Field>
          <Field label={editando ? 'Nova senha (deixe em branco para manter)' : 'Senha'}>
            <TextInput type="password" value={form.senha} onChange={(e) => setForm({ ...form, senha: e.target.value })} />
          </Field>
          <Field label="Perfil">
            <Segmented options={[['ADMIN', 'Admin'], ['OPERADOR', 'Operador']]} value={form.perfil} onChange={(v) => setForm({ ...form, perfil: v })} />
          </Field>
          {editando && (
            <Field label="Ativo">
              <Segmented options={[['true', 'Sim'], ['false', 'Não']]} value={String(form.ativo)} onChange={(v) => setForm({ ...form, ativo: v === 'true' })} />
            </Field>
          )}
        </div>
        <div className="flex items-center justify-between mt-6">
          <div className="flex gap-2">
            <button type="button" disabled={salvando} onClick={salvar} className="px-4 py-2 rounded text-sm font-medium disabled:opacity-60" style={{ background: C.red, color: '#fff' }}>
              {salvando ? 'Salvando...' : 'Salvar'}
            </button>
            <button type="button" onClick={() => setModalAberto(false)} className="px-4 py-2 rounded text-sm" style={{ background: C.bg, color: C.textDark }}>Cancelar</button>
          </div>
          {editando && editando.id !== usuarioLogado?.id && (
            <button
              type="button"
              onClick={() => { setModalAberto(false); setParaExcluir(editando); }}
              className="flex items-center gap-1 px-3 py-2 rounded text-sm"
              style={{ color: '#7A1E17' }}
            >
              <Trash2 size={14} /> Excluir
            </button>
          )}
        </div>
      </Modal>

      <Modal open={!!paraExcluir} title="Excluir usuário" onClose={() => setParaExcluir(null)} maxWidth={400}>
        <ErrorBanner message={erro} />
        <p style={{ fontSize: 14, color: C.textDark }}>
          Tem certeza que deseja excluir <strong>{paraExcluir?.nome}</strong>? O login dele deixará de funcionar.
        </p>
        <div className="flex gap-2 mt-6">
          <button type="button" disabled={excluindo} onClick={confirmarExclusao} className="px-4 py-2 rounded text-sm font-medium disabled:opacity-60" style={{ background: '#B3261E', color: '#fff' }}>
            {excluindo ? 'Excluindo...' : 'Excluir'}
          </button>
          <button type="button" onClick={() => setParaExcluir(null)} className="px-4 py-2 rounded text-sm" style={{ background: C.bg, color: C.textDark }}>Cancelar</button>
        </div>
      </Modal>

      {!modalAberto && !paraExcluir && <ErrorBanner message={erro} />}

      {usuarios.length === 0 ? (
        <EmptyState message="Nenhum usuário cadastrado ainda." />
      ) : (
        <div className="grid grid-cols-3 gap-4">
          {usuarios.map((u) => (
            <Card key={u.id} hover style={{ position: 'relative' }}>
              <button
                onClick={() => abrirEdicao(u)}
                aria-label="Editar usuário"
                className="flex items-center justify-center"
                style={{ position: 'absolute', top: 12, right: 12, width: 28, height: 28, borderRadius: '50%', background: C.bg, color: C.textMuted }}
              >
                <Pencil size={13} />
              </button>
              <div className="flex items-center gap-3" style={{ paddingRight: 28 }}>
                <Avatar name={u.nome} color={u.perfil === 'ADMIN' ? C.red : C.blue} />
                <div className="flex-1 min-w-0">
                  <div style={{ fontSize: 14, fontWeight: 600, color: C.textDark }} className="truncate">{u.nome}</div>
                  <div style={{ fontSize: 12, color: C.textMuted }} className="truncate">@{u.login}</div>
                </div>
              </div>
              <div className="flex gap-2 mt-3">
                <Badge tone={u.perfil === 'ADMIN' ? 'red' : 'blue'}>{u.perfil === 'ADMIN' ? 'Admin' : 'Operador'}</Badge>
                <Badge tone={u.ativo ? 'blue' : 'neutral'}>{u.ativo ? 'Ativo' : 'Inativo'}</Badge>
              </div>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}
