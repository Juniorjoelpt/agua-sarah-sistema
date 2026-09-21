package com.aguasarah.terceiros.frota;

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
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

// Mesmo timbrado dos outros relatorios - PDF das prestacoes de contas da
// frota de terceiros, filtradas por periodo e/ou caminhao.
@Service
public class RelatorioFrotaTerceiroPdfService {

    private static final Color NAVY = new Color(21, 49, 107);
    private static final Color LIGHT_BG = new Color(247, 247, 245);
    private static final Color MUTED = new Color(102, 112, 128);
    private static final Color BORDER = new Color(225, 231, 240);
    private static final Color TEXT_DARK = new Color(27, 36, 48);

    private static final Font FONTE_TITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 17, NAVY);
    private static final Font FONTE_SUBTITULO = FontFactory.getFont(FontFactory.HELVETICA, 10, MUTED);
    private static final Font FONTE_CABECALHO_TABELA = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
    private static final Font FONTE_CELULA = FontFactory.getFont(FontFactory.HELVETICA, 9, TEXT_DARK);
    private static final Font FONTE_CELULA_MUTED = FontFactory.getFont(FontFactory.HELVETICA, 9, MUTED);
    private static final Font FONTE_CELULA_NEGRITO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, TEXT_DARK);
    private static final Font FONTE_MARCA_SUB = FontFactory.getFont(FontFactory.HELVETICA, 8, MUTED);
    private static final Font FONTE_RODAPE = FontFactory.getFont(FontFactory.HELVETICA, 7, MUTED);

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
                // se a logo falhar por qualquer motivo, o documento ainda deve ser gerado sem ela
            }

            ColumnText.showTextAligned(cb, Element.ALIGN_LEFT, new Phrase("Compra de Terceiros", FONTE_MARCA_SUB), logoX + logoWidth + 10, top - logoHeight / 2 - 3, 0);

            cb.setColorStroke(NAVY);
            cb.setLineWidth(0.75f);
            cb.moveTo(document.leftMargin(), top - logoHeight - 6);
            cb.lineTo(pageWidth - document.rightMargin(), top - logoHeight - 6);
            cb.stroke();

            String rodape = "Gerado em " + java.time.LocalDateTime.now().format(FMT_DATA_HORA) + "   ·   Página " + writer.getPageNumber();
            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, new Phrase(rodape, FONTE_RODAPE), pageWidth / 2, document.bottomMargin() - 16, 0);
        }
    }

    public byte[] gerar(List<PrestacaoContasTerceiro> prestacoes, LocalDate inicio, LocalDate fim, String caminhaoLabel) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4, 40, 40, 78, 50);
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            writer.setPageEvent(new Timbrado());
            document.open();

            Paragraph titulo = new Paragraph("Prestações de Contas — Frota de Terceiros", FONTE_TITULO);
            titulo.setSpacingAfter(2);
            document.add(titulo);

            String subtitulo = periodoTexto(inicio, fim) + (caminhaoLabel != null ? "  ·  Caminhão: " + caminhaoLabel : "  ·  Todos os caminhões");
            Paragraph sub = new Paragraph(subtitulo, FONTE_SUBTITULO);
            sub.setSpacingAfter(16);
            document.add(sub);

            PdfPTable table = new PdfPTable(new float[]{11, 12, 18, 17, 11, 11, 11, 13});
            table.setWidthPercentage(100);
            for (String h : new String[]{"Data", "Caminhão", "Produto", "Rota", "Vendida", "Avaria", "Despesas", "Lucro"}) {
                PdfPCell cell = new PdfPCell(new Phrase(h, FONTE_CABECALHO_TABELA));
                cell.setBackgroundColor(NAVY);
                cell.setPadding(6);
                cell.setBorderColor(NAVY);
                table.addCell(cell);
            }

            BigDecimal totalDespesas = BigDecimal.ZERO, totalLucro = BigDecimal.ZERO;
            int linha = 0;
            for (PrestacaoContasTerceiro p : prestacoes) {
                boolean par = linha++ % 2 == 1;
                String data = p.getDataPrestacao() != null ? p.getDataPrestacao().toLocalDate().format(FMT_DATA) : "-";
                String placa = p.getCarregamento() != null && p.getCarregamento().getCaminhao() != null ? p.getCarregamento().getCaminhao().getPlaca() : "-";
                String produtoNome = p.getCarregamento() != null && p.getCarregamento().getProduto() != null ? p.getCarregamento().getProduto().getNome() : "-";
                String rota = p.getCarregamento() != null && p.getCarregamento().getRota() != null ? p.getCarregamento().getRota() : "-";

                table.addCell(celula(data, FONTE_CELULA, par));
                table.addCell(celula(placa, FONTE_CELULA, par));
                table.addCell(celula(produtoNome, FONTE_CELULA, par));
                table.addCell(celula(rota, FONTE_CELULA, par));
                table.addCell(celula(p.getQuantidadeVendida() + "", FONTE_CELULA, par));
                table.addCell(celula(p.getQuantidadeAvaria() + "", FONTE_CELULA, par));
                table.addCell(celula(FMT_MOEDA.format(p.getTotalDespesas()), FONTE_CELULA, par));
                table.addCell(celula(FMT_MOEDA.format(p.getLucro()), FONTE_CELULA_NEGRITO, par));

                if (p.getTotalDespesas() != null) totalDespesas = totalDespesas.add(p.getTotalDespesas());
                if (p.getLucro() != null) totalLucro = totalLucro.add(p.getLucro());
            }
            document.add(table);

            document.add(new Paragraph(" ", FONTE_CELULA));
            PdfPTable totais = new PdfPTable(new float[]{50, 50});
            totais.setWidthPercentage(60);
            totais.setHorizontalAlignment(Element.ALIGN_RIGHT);
            adicionarLinhaTotal(totais, "Total de despesas", FMT_MOEDA.format(totalDespesas));
            adicionarLinhaTotal(totais, "Total de lucro", FMT_MOEDA.format(totalLucro));
            document.add(totais);

            document.close();
            return baos.toByteArray();
        } catch (DocumentException e) {
            throw new IllegalStateException("Erro ao gerar PDF de prestações da frota de terceiros", e);
        }
    }

    private PdfPCell celula(String texto, Font fonte, boolean linhaPar) {
        PdfPCell cell = new PdfPCell(new Phrase(texto == null ? "" : texto, fonte));
        cell.setPadding(5);
        cell.setBorderColor(BORDER);
        if (linhaPar) cell.setBackgroundColor(LIGHT_BG);
        return cell;
    }

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
}
