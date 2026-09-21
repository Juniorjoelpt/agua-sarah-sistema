package com.aguasarah.relatorio;

import com.aguasarah.caixa.ResumoCaixaDTO;
import com.aguasarah.despesa.CategoriaDespesa;
import com.aguasarah.despesa.Despesa;
import com.aguasarah.estoque.Insumo;
import com.aguasarah.fluxocaixa.LancamentoFluxoCaixa;
import com.aguasarah.fluxocaixa.TipoLancamento;
import com.aguasarah.frota.PrestacaoContas;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

// Gera os relatorios em PDF com timbrado (logo + identidade visual da marca)
// repetido no topo de cada pagina, e rodape com data de geracao + numero de pagina.
@Service
public class RelatorioPdfService {

    private static final Color NAVY = new Color(21, 49, 107);
    private static final Color RED = new Color(217, 41, 31);
    private static final Color AMBER = new Color(185, 106, 46);
    private static final Color LIGHT_BG = new Color(247, 247, 245);
    private static final Color BLUE_LIGHT = new Color(228, 237, 252);
    private static final Color AMBER_LIGHT = new Color(247, 233, 218);
    private static final Color MUTED = new Color(102, 112, 128);
    private static final Color BORDER = new Color(225, 231, 240);
    private static final Color TEXT_DARK = new Color(27, 36, 48);

    private static final Font FONTE_TITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 17, NAVY);
    private static final Font FONTE_SUBTITULO = FontFactory.getFont(FontFactory.HELVETICA, 10, MUTED);
    private static final Font FONTE_SECAO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, NAVY);
    private static final Font FONTE_CABECALHO_TABELA = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
    private static final Font FONTE_CELULA = FontFactory.getFont(FontFactory.HELVETICA, 9, TEXT_DARK);
    private static final Font FONTE_CELULA_MUTED = FontFactory.getFont(FontFactory.HELVETICA, 9, MUTED);
    private static final Font FONTE_CELULA_NEGRITO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, TEXT_DARK);
    private static final Font FONTE_MARCA_SUB = FontFactory.getFont(FontFactory.HELVETICA, 8, MUTED);
    private static final Font FONTE_RODAPE = FontFactory.getFont(FontFactory.HELVETICA, 7, MUTED);
    private static final Font FONTE_NUMERO_GRANDE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, NAVY);
    private static final Font FONTE_NUMERO_GRANDE_AMBER = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, AMBER);

    private static final DateTimeFormatter FMT_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FMT_DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");
    private static final NumberFormat FMT_MOEDA = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    private byte[] logoBytes;

    private byte[] logo() {
        if (logoBytes == null) {
            try (InputStream is = new ClassPathResource("assets/logo-agua-sarah-horizontal.png").getInputStream()) {
                logoBytes = is.readAllBytes();
            } catch (IOException e) {
                throw new IllegalStateException("Nao foi possivel carregar a logo para o PDF", e);
            }
        }
        return logoBytes;
    }

    private String periodoTexto(LocalDate inicio, LocalDate fim) {
        if (inicio == null || fim == null) return "Todos os registros";
        if (inicio.equals(fim)) return "Dia " + inicio.format(FMT_DATA);
        return "Período de " + inicio.format(FMT_DATA) + " até " + fim.format(FMT_DATA);
    }

    /* ---------- timbrado (cabecalho + rodape repetidos em toda pagina) ---------- */

    private class Timbrado extends PdfPageEventHelper {
        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();
            float pageWidth = document.getPageSize().getWidth();
            float top = document.getPageSize().getHeight() - 28;
            float logoHeight = 32f;
            float logoWidth = logoHeight * (500f / 239f); // proporcao da logo horizontal
            float logoX = document.leftMargin();
            float logoY = top - logoHeight;

            try {
                Image logo = Image.getInstance(logo());
                logo.scaleAbsolute(logoWidth, logoHeight);
                logo.setAbsolutePosition(logoX, logoY);
                cb.addImage(logo);
            } catch (Exception e) {
                // se a logo falhar por qualquer motivo, o relatorio ainda deve ser gerado sem ela
            }

            ColumnText.showTextAligned(cb, Element.ALIGN_LEFT, new Phrase("Envasadora de água mineral · Adicionada de sais", FONTE_MARCA_SUB), logoX + logoWidth + 10, top - logoHeight / 2 - 3, 0);

            cb.setColorStroke(NAVY);
            cb.setLineWidth(0.75f);
            cb.moveTo(document.leftMargin(), top - logoHeight - 6);
            cb.lineTo(pageWidth - document.rightMargin(), top - logoHeight - 6);
            cb.stroke();

            String rodape = "Gerado em " + LocalDateTime.now().format(FMT_DATA_HORA) + "   ·   Página " + writer.getPageNumber();
            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, new Phrase(rodape, FONTE_RODAPE), pageWidth / 2, document.bottomMargin() - 16, 0);
        }
    }

    private Document criarDocumento(ByteArrayOutputStream baos) throws DocumentException {
        Document document = new Document(PageSize.A4, 40, 40, 78, 50);
        PdfWriter writer = PdfWriter.getInstance(document, baos);
        writer.setPageEvent(new Timbrado());
        document.open();
        return document;
    }

    private void adicionarTitulo(Document document, String titulo, String subtitulo) throws DocumentException {
        Paragraph p = new Paragraph(titulo, FONTE_TITULO);
        p.setSpacingAfter(2);
        document.add(p);
        Paragraph s = new Paragraph(subtitulo, FONTE_SUBTITULO);
        s.setSpacingAfter(16);
        document.add(s);
    }

    private PdfPCell celulaCabecalho(String texto) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, FONTE_CABECALHO_TABELA));
        cell.setBackgroundColor(NAVY);
        cell.setPadding(6);
        cell.setBorderColor(NAVY);
        return cell;
    }

    private PdfPCell celula(String texto, Font fonte, boolean linhaPar) {
        PdfPCell cell = new PdfPCell(new Phrase(texto == null ? "" : texto, fonte));
        cell.setPadding(5);
        cell.setBorderColor(BORDER);
        if (linhaPar) cell.setBackgroundColor(LIGHT_BG);
        return cell;
    }

    /* ---------- relatorio: historico de caixa ---------- */

    public byte[] gerarCaixa(List<ResumoCaixaDTO> resumos, LocalDate inicio, LocalDate fim) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = criarDocumento(baos);
            adicionarTitulo(document, "Histórico de Caixa", periodoTexto(inicio, fim));

            PdfPTable table = new PdfPTable(new float[]{11, 9, 12, 11, 11, 11, 11, 12}) ;
            table.setWidthPercentage(100);
            for (String h : new String[]{"Data", "Status", "Entradas espécie", "Entradas PIX", "Recebim. fiado", "Despesas", "Galões bonif.", "Saldo final"}) {
                table.addCell(celulaCabecalho(h));
            }

            BigDecimal totalEspecie = BigDecimal.ZERO, totalPix = BigDecimal.ZERO, totalDespesas = BigDecimal.ZERO, totalSaldoFinal = BigDecimal.ZERO, totalRecebimentos = BigDecimal.ZERO;
            int linha = 0;
            for (ResumoCaixaDTO r : resumos) {
                boolean par = linha++ % 2 == 1;
                BigDecimal recebimentos = r.totalRecebimentosContasReceberEspecie().add(r.totalRecebimentosContasReceberPix());
                table.addCell(celula(r.dataAbertura() != null ? r.dataAbertura().toLocalDate().format(FMT_DATA) : "-", FONTE_CELULA, par));
                table.addCell(celula(r.status() != null ? r.status().name() : "-", FONTE_CELULA, par));
                table.addCell(celula(FMT_MOEDA.format(r.totalVendasEspecie()), FONTE_CELULA, par));
                table.addCell(celula(FMT_MOEDA.format(r.totalVendasPix()), FONTE_CELULA, par));
                table.addCell(celula(FMT_MOEDA.format(recebimentos), FONTE_CELULA, par));
                table.addCell(celula(FMT_MOEDA.format(r.totalDespesas()), FONTE_CELULA, par));
                table.addCell(celula(String.valueOf(r.totalGaloesBonificados()), FONTE_CELULA, par));
                BigDecimal saldoFinal = r.saldoFinalEspecie().add(r.saldoFinalPix());
                table.addCell(celula(FMT_MOEDA.format(saldoFinal), FONTE_CELULA_NEGRITO, par));

                totalEspecie = totalEspecie.add(r.totalVendasEspecie());
                totalPix = totalPix.add(r.totalVendasPix());
                totalDespesas = totalDespesas.add(r.totalDespesas());
                totalRecebimentos = totalRecebimentos.add(recebimentos);
                totalSaldoFinal = totalSaldoFinal.add(saldoFinal);
            }
            document.add(table);

            document.add(new Paragraph(" ", FONTE_CELULA));
            PdfPTable totais = new PdfPTable(new float[]{50, 50});
            totais.setWidthPercentage(60);
            totais.setHorizontalAlignment(Element.ALIGN_RIGHT);
            adicionarLinhaTotal(totais, "Total entradas espécie", FMT_MOEDA.format(totalEspecie));
            adicionarLinhaTotal(totais, "Total entradas PIX", FMT_MOEDA.format(totalPix));
            adicionarLinhaTotal(totais, "Total recebimentos de fiado", FMT_MOEDA.format(totalRecebimentos));
            adicionarLinhaTotal(totais, "Total despesas", FMT_MOEDA.format(totalDespesas));
            adicionarLinhaTotal(totais, "Total saldo final", FMT_MOEDA.format(totalSaldoFinal));
            document.add(totais);

            document.close();
            return baos.toByteArray();
        } catch (DocumentException e) {
            throw new IllegalStateException("Erro ao gerar PDF do histórico de caixa", e);
        }
    }

    /* ---------- relatorio: despesas ---------- */

    private static final Map<CategoriaDespesa, String> CATEGORIA_LABEL = Map.of(
            CategoriaDespesa.FROTA, "Frota",
            CategoriaDespesa.PRODUCAO, "Produção",
            CategoriaDespesa.INSUMOS, "Insumos",
            CategoriaDespesa.OUTROS, "Outros"
    );

    public byte[] gerarDespesas(List<Despesa> despesas, LocalDate inicio, LocalDate fim) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = criarDocumento(baos);
            adicionarTitulo(document, "Relatório de Despesas", periodoTexto(inicio, fim));

            PdfPTable table = new PdfPTable(new float[]{14, 40, 20, 26});
            table.setWidthPercentage(100);
            for (String h : new String[]{"Data", "Descrição", "Categoria", "Valor"}) {
                table.addCell(celulaCabecalho(h));
            }

            BigDecimal total = BigDecimal.ZERO;
            int linha = 0;
            for (Despesa d : despesas) {
                boolean par = linha++ % 2 == 1;
                table.addCell(celula(d.getData().format(FMT_DATA), FONTE_CELULA, par));
                table.addCell(celula(d.getDescricao(), FONTE_CELULA, par));
                table.addCell(celula(CATEGORIA_LABEL.getOrDefault(d.getCategoria(), "-"), FONTE_CELULA, par));
                table.addCell(celula(FMT_MOEDA.format(d.getValor()), FONTE_CELULA, par));
                total = total.add(d.getValor());
            }
            document.add(table);

            document.add(new Paragraph(" ", FONTE_CELULA));
            PdfPTable totais = new PdfPTable(new float[]{50, 50});
            totais.setWidthPercentage(50);
            totais.setHorizontalAlignment(Element.ALIGN_RIGHT);
            adicionarLinhaTotal(totais, "Total de despesas", FMT_MOEDA.format(total));
            document.add(totais);

            document.close();
            return baos.toByteArray();
        } catch (DocumentException e) {
            throw new IllegalStateException("Erro ao gerar PDF de despesas", e);
        }
    }

    /* ---------- relatorio: fluxo de caixa ---------- */

    public byte[] gerarFluxoCaixa(RelatorioFluxoCaixaDTO relatorio, LocalDate inicio, LocalDate fim) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = criarDocumento(baos);
            adicionarTitulo(document, "Relatório de Fluxo de Caixa", periodoTexto(inicio, fim));

            PdfPTable table = new PdfPTable(new float[]{12, 24, 20, 20, 12, 12});
            table.setWidthPercentage(100);
            for (String h : new String[]{"Data", "Descrição", "Conta", "Categoria", "Tipo", "Valor"}) {
                table.addCell(celulaCabecalho(h));
            }

            int linha = 0;
            for (LancamentoFluxoCaixa l : relatorio.lancamentos()) {
                boolean par = linha++ % 2 == 1;
                table.addCell(celula(l.getData().format(FMT_DATA), FONTE_CELULA, par));
                table.addCell(celula(l.getDescricao(), FONTE_CELULA, par));
                table.addCell(celula(l.getContaBancaria() != null ? l.getContaBancaria().getApelido() : "-", FONTE_CELULA, par));
                table.addCell(celula(l.getCategoria() != null ? l.getCategoria() : "-", FONTE_CELULA, par));
                table.addCell(celula(l.getTipo() == TipoLancamento.ENTRADA ? "Entrada" : "Saída", FONTE_CELULA, par));
                table.addCell(celula(FMT_MOEDA.format(l.getValor()), FONTE_CELULA_NEGRITO, par));
            }
            document.add(table);

            document.add(new Paragraph(" ", FONTE_CELULA));
            PdfPTable totais = new PdfPTable(new float[]{50, 50});
            totais.setWidthPercentage(50);
            totais.setHorizontalAlignment(Element.ALIGN_RIGHT);
            adicionarLinhaTotal(totais, "Total de entradas", FMT_MOEDA.format(relatorio.totalEntradas()));
            adicionarLinhaTotal(totais, "Total de saídas", FMT_MOEDA.format(relatorio.totalSaidas()));
            adicionarLinhaTotal(totais, "Saldo do período", FMT_MOEDA.format(relatorio.saldoPeriodo()));
            document.add(totais);

            document.close();
            return baos.toByteArray();
        } catch (DocumentException e) {
            throw new IllegalStateException("Erro ao gerar PDF de fluxo de caixa", e);
        }
    }

    /* ---------- relatorio: estoque ---------- */

    public byte[] gerarEstoque(List<Insumo> insumos) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = criarDocumento(baos);
            adicionarTitulo(document, "Posição de Estoque", "Gerado em " + LocalDate.now().format(FMT_DATA));

            PdfPTable table = new PdfPTable(new float[]{40, 20, 20, 20});
            table.setWidthPercentage(100);
            for (String h : new String[]{"Insumo", "Quantidade", "Mínimo", "Situação"}) {
                table.addCell(celulaCabecalho(h));
            }

            int linha = 0;
            for (Insumo i : insumos) {
                boolean par = linha++ % 2 == 1;
                boolean baixo = i.getQuantidadeAtual() != null && i.getQuantidadeMinima() != null
                        && i.getQuantidadeAtual() <= i.getQuantidadeMinima();
                table.addCell(celula(i.getNome(), FONTE_CELULA, par));
                table.addCell(celula(i.getQuantidadeAtual() + " " + (i.getUnidadeMedida() != null ? i.getUnidadeMedida() : ""), FONTE_CELULA, par));
                table.addCell(celula(String.valueOf(i.getQuantidadeMinima()), FONTE_CELULA, par));
                PdfPCell situacao = celula(baixo ? "Baixo" : "Normal", baixo ? FONTE_CELULA_NEGRITO : FONTE_CELULA_MUTED, par);
                table.addCell(situacao);
            }
            document.add(table);

            document.close();
            return baos.toByteArray();
        } catch (DocumentException e) {
            throw new IllegalStateException("Erro ao gerar PDF de estoque", e);
        }
    }

    /* ---------- relatorio: envase de agua ---------- */

    public byte[] gerarEnvase(RelatorioEnvaseDTO dto, LocalDate inicio, LocalDate fim) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = criarDocumento(baos);
            adicionarTitulo(document, "Relatório de Envase de Água", periodoTexto(inicio, fim));

            PdfPTable destaque = new PdfPTable(new float[]{50, 50});
            destaque.setWidthPercentage(100);
            destaque.setSpacingAfter(16);

            PdfPCell envasado = new PdfPCell();
            envasado.setBackgroundColor(BLUE_LIGHT);
            envasado.setPadding(10);
            envasado.setBorderColor(BLUE_LIGHT);
            Paragraph pEnv1 = new Paragraph("Total envasado", FONTE_SUBTITULO);
            Paragraph pEnv2 = new Paragraph(dto.totalGaloesEnvasados() + " galões", FONTE_NUMERO_GRANDE);
            envasado.addElement(pEnv1);
            envasado.addElement(pEnv2);
            destaque.addCell(envasado);

            PdfPCell avarias = new PdfPCell();
            avarias.setBackgroundColor(AMBER_LIGHT);
            avarias.setPadding(10);
            avarias.setBorderColor(AMBER_LIGHT);
            Paragraph pAv1 = new Paragraph("Total de avarias", FONTE_SUBTITULO);
            Paragraph pAv2 = new Paragraph(dto.totalAvarias() + " galões", FONTE_NUMERO_GRANDE_AMBER);
            avarias.addElement(pAv1);
            avarias.addElement(pAv2);
            destaque.addCell(avarias);

            document.add(destaque);

            document.add(new Paragraph("De onde vem o envase", FONTE_SECAO));
            PdfPTable tOrigem = new PdfPTable(new float[]{70, 30});
            tOrigem.setWidthPercentage(100);
            tOrigem.setSpacingBefore(6);
            tOrigem.setSpacingAfter(16);
            adicionarLinhaSimples(tOrigem, "Vendidos no PDV", String.valueOf(dto.totalGaloesVendidosPdv()), false);
            adicionarLinhaSimples(tOrigem, "Carregados nos caminhões", String.valueOf(dto.totalGaloesCarregadosCaminhoes()), true);
            document.add(tOrigem);

            document.add(new Paragraph("De onde vêm as avarias", FONTE_SECAO));
            PdfPTable tAvarias = new PdfPTable(new float[]{70, 30});
            tAvarias.setWidthPercentage(100);
            tAvarias.setSpacingBefore(6);
            adicionarLinhaSimples(tAvarias, "Produção (bonificadas na venda)", String.valueOf(dto.totalGaloesBonificados()), false);
            adicionarLinhaSimples(tAvarias, "Cliente (venda no PDV)", String.valueOf(dto.totalGaloesAvariaClientePdv()), true);
            adicionarLinhaSimples(tAvarias, "Caminhões (prestação de contas)", String.valueOf(dto.totalGaloesAvariaCaminhoes()), false);
            document.add(tAvarias);

            document.close();
            return baos.toByteArray();
        } catch (DocumentException e) {
            throw new IllegalStateException("Erro ao gerar PDF de envase", e);
        }
    }

    /* ---------- relatorio: prestacoes de contas da frota (por caminhao/periodo) ---------- */

    public byte[] gerarPrestacoesFrota(List<PrestacaoContas> prestacoes, LocalDate inicio, LocalDate fim, String caminhaoLabel) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = criarDocumento(baos);
            String subtitulo = periodoTexto(inicio, fim) + (caminhaoLabel != null ? "  ·  Caminhão: " + caminhaoLabel : "  ·  Todos os caminhões");
            adicionarTitulo(document, "Prestações de Contas da Frota", subtitulo);

            PdfPTable table = new PdfPTable(new float[]{11, 12, 18, 11, 11, 11, 12, 14});
            table.setWidthPercentage(100);
            for (String h : new String[]{"Data", "Caminhão", "Rota", "Vendida", "Avaria", "Despesas", "Lucro", "Diferença"}) {
                table.addCell(celulaCabecalho(h));
            }

            BigDecimal totalDespesas = BigDecimal.ZERO, totalLucro = BigDecimal.ZERO, totalDiferenca = BigDecimal.ZERO;
            int linha = 0;
            for (PrestacaoContas p : prestacoes) {
                boolean par = linha++ % 2 == 1;
                String data = p.getDataPrestacao() != null ? p.getDataPrestacao().toLocalDate().format(FMT_DATA) : "-";
                String placa = p.getCarregamento() != null && p.getCarregamento().getCaminhao() != null ? p.getCarregamento().getCaminhao().getPlaca() : "-";
                String rota = p.getCarregamento() != null && p.getCarregamento().getRota() != null ? p.getCarregamento().getRota() : "-";

                table.addCell(celula(data, FONTE_CELULA, par));
                table.addCell(celula(placa, FONTE_CELULA, par));
                table.addCell(celula(rota, FONTE_CELULA, par));
                table.addCell(celula(p.getQuantidadeVendida() + " gl", FONTE_CELULA, par));
                table.addCell(celula(p.getQuantidadeAvaria() + " gl", FONTE_CELULA, par));
                table.addCell(celula(FMT_MOEDA.format(p.getTotalDespesas()), FONTE_CELULA, par));
                table.addCell(celula(FMT_MOEDA.format(p.getLucro()), FONTE_CELULA_NEGRITO, par));
                boolean confere = p.getDiferenca() != null && p.getDiferenca().abs().compareTo(new BigDecimal("0.01")) < 0;
                table.addCell(celula(FMT_MOEDA.format(p.getDiferenca()), confere ? FONTE_CELULA_MUTED : FONTE_CELULA_NEGRITO, par));

                if (p.getTotalDespesas() != null) totalDespesas = totalDespesas.add(p.getTotalDespesas());
                if (p.getLucro() != null) totalLucro = totalLucro.add(p.getLucro());
                if (p.getDiferenca() != null) totalDiferenca = totalDiferenca.add(p.getDiferenca());
            }
            document.add(table);

            document.add(new Paragraph(" ", FONTE_CELULA));
            PdfPTable totais = new PdfPTable(new float[]{50, 50});
            totais.setWidthPercentage(60);
            totais.setHorizontalAlignment(Element.ALIGN_RIGHT);
            adicionarLinhaTotal(totais, "Total de despesas", FMT_MOEDA.format(totalDespesas));
            adicionarLinhaTotal(totais, "Total de lucro", FMT_MOEDA.format(totalLucro));
            adicionarLinhaTotal(totais, "Total de diferença (conferência)", FMT_MOEDA.format(totalDiferenca));
            document.add(totais);

            document.close();
            return baos.toByteArray();
        } catch (DocumentException e) {
            throw new IllegalStateException("Erro ao gerar PDF de prestações da frota", e);
        }
    }

    /* ---------- helpers de layout ---------- */

    private void adicionarLinhaTotal(PdfPTable tabela, String label, String valor) {
        PdfPCell c1 = new PdfPCell(new Phrase(label, FONTE_CELULA_MUTED));
        c1.setBorder(Rectangle.NO_BORDER);
        c1.setPadding(3);
        PdfPCell c2 = new PdfPCell(new Phrase(valor, FONTE_CELULA_NEGRITO));
        c2.setBorder(Rectangle.NO_BORDER);
        c2.setPadding(3);
        c2.setHorizontalAlignment(Element.ALIGN_RIGHT);
        tabela.addCell(c1);
        tabela.addCell(c2);
    }

    private void adicionarLinhaSimples(PdfPTable tabela, String label, String valor, boolean par) {
        tabela.addCell(celula(label, FONTE_CELULA, par));
        PdfPCell c = celula(valor, FONTE_CELULA_NEGRITO, par);
        c.setHorizontalAlignment(Element.ALIGN_RIGHT);
        tabela.addCell(c);
    }
}
