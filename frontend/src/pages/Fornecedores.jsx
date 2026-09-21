import React, { useEffect, useState } from 'react';
import { Pencil, Phone, MapPin, History } from 'lucide-react';
import { SectionHeaderWithAction, Card, Field, TextInput, Segmented, ErrorBanner, Loading, Avatar, Badge, EmptyState } from '../components/ui';
import Modal from '../components/Modal';
import { C, DISPLAY_FONT } from '../theme';
import { fornecedoresApi } from '../api/fornecedores';
import { produtosApi } from '../api/produtos';
import { estoqueApi } from '../api/estoque';

const FORM_VAZIO = { nome: '', nomeContato: '', telefone: '', endereco: '', ativo: true, produtoIds: [], insumoIds: [] };
const COMPRA_VAZIA = { descricao: '', categoria: 'INSUMOS', valor: '', data: new Date().toISOString().slice(0, 10), insumoId: '', quantidadeInsumo: '' };
const CATEGORIA_LABEL = { FROTA: 'Frota', PRODUCAO: 'Produção', INSUMOS: 'Insumos', OUTROS: 'Outros' };
const moeda = (v) => (v ?? 0).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
const dataCurta = (iso) => new Date(iso).toLocaleDateString('pt-BR', { day: '2-digit', month: '2-digit', year: 'numeric' });

export default function Fornecedores() {
  const [fornecedores, setFornecedores] = useState([]);
  const [produtos, setProdutos] = useState([]);
  const [insumos, setInsumos] = useState([]);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState(null);
  const [salvando, setSalvando] = useState(false);

  const [modalAberto, setModalAberto] = useState(false);
  const [editandoId, setEditandoId] = useState(null);
  const [form, setForm] = useState(FORM_VAZIO);

  const [fornecedorHistorico, setFornecedorHistorico] = useState(null);
  const [compras, setCompras] = useState([]);
  const [fornecimentos, setFornecimentos] = useState([]);
  const [carregandoCompras, setCarregandoCompras] = useState(false);
  const [novaCompra, setNovaCompra] = useState(COMPRA_VAZIA);
  const [salvandoCompra, setSalvandoCompra] = useState(false);

  async function carregar() {
    setCarregando(true);
    try {
      const [f, p, i] = await Promise.all([fornecedoresApi.listar(false), produtosApi.listar(), estoqueApi.listarInsumos()]);
      setFornecedores(f);
      setProdutos(p);
      setInsumos(i);
    } catch (e) {
      setErro(e.message);
    }
    setCarregando(false);
  }

  useEffect(() => { carregar(); }, []);

  function abrirNovo() {
    setEditandoId(null);
    setForm(FORM_VAZIO);
    setModalAberto(true);
  }

  function abrirEdicao(f) {
    setEditandoId(f.id);
    setForm({
      nome: f.nome,
      nomeContato: f.nomeContato || '',
      telefone: f.telefone || '',
      endereco: f.endereco || '',
      ativo: f.ativo,
      produtoIds: (f.produtosFornecidos || []).map((p) => p.id),
      insumoIds: (f.insumosFornecidos || []).map((i) => i.id),
    });
    setModalAberto(true);
  }

  function toggleProduto(id) {
    setForm((f) => ({
      ...f,
      produtoIds: f.produtoIds.includes(id) ? f.produtoIds.filter((x) => x !== id) : [...f.produtoIds, id],
    }));
  }

  function toggleInsumo(id) {
    setForm((f) => ({
      ...f,
      insumoIds: f.insumoIds.includes(id) ? f.insumoIds.filter((x) => x !== id) : [...f.insumoIds, id],
    }));
  }

  async function salvar() {
    setSalvando(true);
    setErro(null);
    try {
      if (editandoId) {
        await fornecedoresApi.atualizar(editandoId, form);
      } else {
        await fornecedoresApi.criar(form);
      }
      setModalAberto(false);
      await carregar();
    } catch (e) {
      setErro(e.message);
    } finally {
      setSalvando(false);
    }
  }

  async function abrirHistorico(fornecedor) {
    setErro(null);
    setFornecedorHistorico(fornecedor);
    setNovaCompra(COMPRA_VAZIA);
    setCarregandoCompras(true);
    try {
      const [c, f] = await Promise.all([fornecedoresApi.listarCompras(fornecedor.id), fornecedoresApi.listarFornecimentos(fornecedor.id)]);
      setCompras(c);
      setFornecimentos(f);
    } catch (e) {
      setErro(e.message);
    } finally {
      setCarregandoCompras(false);
    }
  }

  async function registrarCompra() {
    setSalvandoCompra(true);
    setErro(null);
    try {
      await fornecedoresApi.registrarCompra(fornecedorHistorico.id, {
        ...novaCompra,
        valor: Number(novaCompra.valor),
        insumoId: novaCompra.insumoId ? Number(novaCompra.insumoId) : null,
        quantidadeInsumo: novaCompra.insumoId && novaCompra.quantidadeInsumo ? Number(novaCompra.quantidadeInsumo) : null,
      });
      setNovaCompra(COMPRA_VAZIA);
      const [c, f] = await Promise.all([fornecedoresApi.listarCompras(fornecedorHistorico.id), fornecedoresApi.listarFornecimentos(fornecedorHistorico.id)]);
      setCompras(c);
      setFornecimentos(f);
      if (novaCompra.insumoId) await carregar(); // insumo mudou de quantidade - atualiza tambem a listagem geral
    } catch (e) {
      setErro(e.message);
    } finally {
      setSalvandoCompra(false);
    }
  }

  const totalCompras = compras.reduce((s, c) => s + c.valor, 0);

  if (carregando) return <Loading />;

  function tagButton(id, nome, ativo, onClick) {
    return (
      <button
        type="button"
        key={id}
        onClick={onClick}
        style={{
          fontSize: 12,
          background: ativo ? C.blueLight : C.bg,
          color: ativo ? C.ink : C.textMuted,
          border: `1px solid ${ativo ? C.blue : C.border}`,
          padding: '4px 10px',
          borderRadius: 999,
        }}
      >
        {nome}
      </button>
    );
  }

  return (
    <div>
      <SectionHeaderWithAction title="Fornecedores" subtitle="Quem fornece cada produto e insumo" actionLabel="+ Novo fornecedor" onAction={abrirNovo} />

      <Modal open={modalAberto} title={editandoId ? 'Editar fornecedor' : 'Novo fornecedor'} onClose={() => setModalAberto(false)} maxWidth={560}>
        <ErrorBanner message={erro} />
        <div className="grid grid-cols-2 gap-4">
          <Field label="Nome do fornecedor"><TextInput value={form.nome} onChange={(e) => setForm({ ...form, nome: e.target.value })} /></Field>
          <Field label="Pessoa de contato"><TextInput value={form.nomeContato} onChange={(e) => setForm({ ...form, nomeContato: e.target.value })} /></Field>
          <Field label="Telefone"><TextInput value={form.telefone} onChange={(e) => setForm({ ...form, telefone: e.target.value })} /></Field>
          <Field label="Endereço"><TextInput value={form.endereco} onChange={(e) => setForm({ ...form, endereco: e.target.value })} /></Field>
        </div>

        <div className="mt-4">
          <Field label="Produtos que este fornecedor fornece">
            <div className="flex gap-2 flex-wrap">
              {produtos.length === 0 && <span style={{ fontSize: 12, color: C.textMuted }}>Nenhum produto cadastrado ainda.</span>}
              {produtos.map((p) => tagButton(p.id, p.nome, form.produtoIds.includes(p.id), () => toggleProduto(p.id)))}
            </div>
          </Field>
        </div>

        <div className="mt-4">
          <Field label="Insumos que este fornecedor fornece">
            <div className="flex gap-2 flex-wrap">
              {insumos.length === 0 && <span style={{ fontSize: 12, color: C.textMuted }}>Nenhum insumo cadastrado ainda.</span>}
              {insumos.map((i) => tagButton(i.id, i.nome, form.insumoIds.includes(i.id), () => toggleInsumo(i.id)))}
            </div>
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

      {/* ---- Modal: historico de compras ---- */}
      <Modal open={!!fornecedorHistorico} title={`Compras — ${fornecedorHistorico?.nome || ''}`} onClose={() => setFornecedorHistorico(null)} maxWidth={620}>
        <ErrorBanner message={erro} />
        <div className="grid grid-cols-2 gap-4">
          <Field label="Descrição"><TextInput placeholder="Ex: Lacres e rótulos" value={novaCompra.descricao} onChange={(e) => setNovaCompra({ ...novaCompra, descricao: e.target.value })} /></Field>
          <Field label="Valor"><TextInput type="number" step="0.01" value={novaCompra.valor} onChange={(e) => setNovaCompra({ ...novaCompra, valor: e.target.value })} /></Field>
        </div>
        <div className="mt-3">
          <Field label="Categoria">
            <Segmented
              options={[['FROTA', 'Frota'], ['PRODUCAO', 'Produção'], ['INSUMOS', 'Insumos'], ['OUTROS', 'Outros']]}
              value={novaCompra.categoria}
              onChange={(v) => setNovaCompra({ ...novaCompra, categoria: v })}
            />
          </Field>
        </div>
        <div className="mt-3" style={{ maxWidth: 180 }}>
          <Field label="Data"><TextInput type="date" value={novaCompra.data} onChange={(e) => setNovaCompra({ ...novaCompra, data: e.target.value })} /></Field>
        </div>

        <div className="mt-4 pt-3" style={{ borderTop: `1px solid ${C.border}` }}>
          <div style={{ fontSize: 12, fontWeight: 500, color: C.textDark, marginBottom: 2 }}>Isso também é uma entrada no estoque?</div>
          <div style={{ fontSize: 11, color: C.textMuted, marginBottom: 8 }}>Opcional — se escolher um insumo, a quantidade recebida já entra automaticamente no estoque.</div>
          <div className="grid grid-cols-2 gap-4">
            <Field label="Insumo recebido">
              <select className="w-full px-3 py-2 rounded border" style={{ borderColor: C.border, fontSize: 14 }} value={novaCompra.insumoId} onChange={(e) => setNovaCompra({ ...novaCompra, insumoId: e.target.value })}>
                <option value="">Nenhum (só financeiro)</option>
                {insumos.map((i) => <option key={i.id} value={i.id}>{i.nome}</option>)}
              </select>
            </Field>
            <Field label="Quantidade recebida">
              <TextInput type="number" disabled={!novaCompra.insumoId} value={novaCompra.quantidadeInsumo} onChange={(e) => setNovaCompra({ ...novaCompra, quantidadeInsumo: e.target.value })} />
            </Field>
          </div>
        </div>

        <button
          type="button"
          disabled={salvandoCompra || !novaCompra.descricao || !novaCompra.valor || (novaCompra.insumoId && !novaCompra.quantidadeInsumo)}
          onClick={registrarCompra}
          className="mt-4 px-4 py-2 rounded text-sm font-medium disabled:opacity-60"
          style={{ background: C.red, color: '#fff' }}
        >
          {salvandoCompra ? 'Salvando...' : '+ Lançar compra'}
        </button>

        <div className="mt-5 pt-4" style={{ borderTop: `1px solid ${C.border}` }}>
          <div className="flex items-center justify-between mb-3">
            <div style={{ fontSize: 13, fontWeight: 500, color: C.textDark }}>Histórico de compras</div>
            <div style={{ fontSize: 14, fontWeight: 600, color: C.textDark, fontFamily: DISPLAY_FONT }}>{moeda(totalCompras)}</div>
          </div>
          {carregandoCompras ? (
            <div style={{ fontSize: 13, color: C.textMuted }}>Carregando...</div>
          ) : compras.length === 0 ? (
            <div style={{ fontSize: 13, color: C.textMuted }}>Nenhuma compra registrada com este fornecedor ainda.</div>
          ) : (
            <div className="flex flex-col gap-2" style={{ maxHeight: 300, overflowY: 'auto', paddingRight: 4 }}>
              {compras.map((c) => (
                <div key={c.id} className="flex justify-between items-center py-2" style={{ borderBottom: `1px solid ${C.border}`, fontSize: 13 }}>
                  <div>
                    <div style={{ color: C.textDark }}>{c.descricao}</div>
                    <div style={{ fontSize: 11, color: C.textMuted }}>{CATEGORIA_LABEL[c.categoria]} · {dataCurta(c.data)}</div>
                  </div>
                  <div style={{ fontWeight: 600, color: C.textDark }}>{moeda(c.valor)}</div>
                </div>
              ))}
            </div>
          )}
        </div>

        <div className="mt-5 pt-4" style={{ borderTop: `1px solid ${C.border}` }}>
          <div style={{ fontSize: 13, fontWeight: 500, color: C.textDark, marginBottom: 3 }}>Histórico de fornecimento</div>
          <div style={{ fontSize: 11, color: C.textMuted, marginBottom: 10 }}>Entradas de insumo no estoque vindas deste fornecedor</div>
          {carregandoCompras ? (
            <div style={{ fontSize: 13, color: C.textMuted }}>Carregando...</div>
          ) : fornecimentos.length === 0 ? (
            <div style={{ fontSize: 13, color: C.textMuted }}>Nenhum fornecimento registrado ainda. Vincule este fornecedor ao lançar uma entrada em Estoque.</div>
          ) : (
            <div className="flex flex-col gap-2" style={{ maxHeight: 260, overflowY: 'auto', paddingRight: 4 }}>
              {fornecimentos.map((mv) => (
                <div key={mv.id} className="flex justify-between items-center py-2" style={{ borderBottom: `1px solid ${C.border}`, fontSize: 13 }}>
                  <div>
                    <div style={{ color: C.textDark }}>{mv.insumo?.nome}</div>
                    <div style={{ fontSize: 11, color: C.textMuted }}>{dataCurta(mv.data)}{mv.observacao ? ` · ${mv.observacao}` : ''}</div>
                  </div>
                  <div style={{ fontWeight: 600, color: C.blue }}>+{mv.quantidade} {mv.insumo?.unidadeMedida}</div>
                </div>
              ))}
            </div>
          )}
        </div>
      </Modal>

      {fornecedores.length === 0 ? (
        <EmptyState message="Nenhum fornecedor cadastrado ainda." />
      ) : (
        <div className="grid grid-cols-3 gap-4">
          {fornecedores.map((f) => (
            <Card key={f.id} hover style={{ position: 'relative' }}>
              <div className="flex gap-1.5" style={{ position: 'absolute', top: 12, right: 12 }}>
                <button
                  onClick={() => abrirHistorico(f)}
                  aria-label="Ver histórico de compras"
                  className="flex items-center justify-center"
                  style={{ width: 28, height: 28, borderRadius: '50%', background: C.bg, color: C.textMuted }}
                >
                  <History size={13} />
                </button>
                <button
                  onClick={() => abrirEdicao(f)}
                  aria-label="Editar fornecedor"
                  className="flex items-center justify-center"
                  style={{ width: 28, height: 28, borderRadius: '50%', background: C.bg, color: C.textMuted }}
                >
                  <Pencil size={13} />
                </button>
              </div>
              <div className="flex items-start gap-3" style={{ paddingRight: 64 }}>
                <Avatar name={f.nome} color={C.blue} />
                <div className="flex-1 min-w-0">
                  <div style={{ fontSize: 14, fontWeight: 600, color: C.textDark }} className="truncate">{f.nome}</div>
                  {f.nomeContato && <div style={{ fontSize: 12, color: C.textMuted }} className="truncate">{f.nomeContato}</div>}
                  {!f.ativo && <Badge tone="neutral">Inativo</Badge>}
                </div>
              </div>
              <div className="flex flex-col gap-1 mt-3" style={{ fontSize: 12, color: C.textMuted }}>
                <div className="flex items-center gap-1.5"><Phone size={12} /> {f.telefone || 'Não informado'}</div>
                <div className="flex items-center gap-1.5"><MapPin size={12} /> {f.endereco || 'Não informado'}</div>
              </div>
              {(f.produtosFornecidos?.length > 0 || f.insumosFornecidos?.length > 0) && (
                <div className="flex gap-1 flex-wrap mt-3" style={{ borderTop: `1px solid ${C.border}`, paddingTop: 10 }}>
                  {(f.produtosFornecidos || []).map((p) => <Badge key={`p${p.id}`} tone="blue">{p.nome}</Badge>)}
                  {(f.insumosFornecidos || []).map((i) => <Badge key={`i${i.id}`} tone="amber">{i.nome}</Badge>)}
                </div>
              )}
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}
