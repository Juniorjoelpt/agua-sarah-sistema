package com.aguasarah.orcamento;

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
import java.time.format.DateTimeFormatter;
import java.util.Locale;

// Gera o PDF do orcamento - documento formal para entregar ao cliente,
// com o mesmo timbrado (logo + identidade visual) dos relatorios internos.
@Service
public class OrcamentoPdfService {

    private static final Color NAVY = new Color(21, 49, 107);
    private static final Color RED = new Color(217, 41, 31);
    private static final Color LIGHT_BG = new Color(247, 247, 245);
    private static final Color BLUE_LIGHT = new Color(228, 237, 252);
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
    private static final Font FONTE_TOTAL = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 15, NAVY);

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

            ColumnText.showTextAligned(cb, Element.ALIGN_LEFT, new Phrase("Envasadora de água mineral · Adicionada de sais", FONTE_MARCA_SUB), logoX + logoWidth + 10, top - logoHeight / 2 - 3, 0);

            cb.setColorStroke(NAVY);
            cb.setLineWidth(0.75f);
            cb.moveTo(document.leftMargin(), top - logoHeight - 6);
            cb.lineTo(pageWidth - document.rightMargin(), top - logoHeight - 6);
            cb.stroke();

            String rodape = "Gerado em " + java.time.LocalDateTime.now().format(FMT_DATA_HORA) + "   ·   Página " + writer.getPageNumber();
            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, new Phrase(rodape, FONTE_RODAPE), pageWidth / 2, document.bottomMargin() - 16, 0);
        }
    }

    public byte[] gerar(Orcamento orcamento) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4, 40, 40, 78, 50);
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            writer.setPageEvent(new Timbrado());
            document.open();

            Paragraph titulo = new Paragraph("Orçamento Nº " + orcamento.getId(), FONTE_TITULO);
            titulo.setSpacingAfter(2);
            document.add(titulo);

            String subtitulo = "Emitido em " + orcamento.getDataCriacao().format(FMT_DATA)
                    + (orcamento.getValidoAte() != null ? "  ·  Válido até " + orcamento.getValidoAte().format(FMT_DATA) : "");
            Paragraph sub = new Paragraph(subtitulo, FONTE_SUBTITULO);
            sub.setSpacingAfter(16);
            document.add(sub);

            String nomeCliente = orcamento.getCliente() != null ? orcamento.getCliente().getNome() : orcamento.getNomeClienteAvulso();
            String telefoneCliente = orcamento.getCliente() != null ? orcamento.getCliente().getTelefone() : orcamento.getTelefoneClienteAvulso();

            PdfPTable infoCliente = new PdfPTable(new float[]{100});
            infoCliente.setWidthPercentage(100);
            infoCliente.setSpacingAfter(16);
            PdfPCell cellCliente = new PdfPCell();
            cellCliente.setBackgroundColor(LIGHT_BG);
            cellCliente.setBorderColor(LIGHT_BG);
            cellCliente.setPadding(10);
            cellCliente.addElement(new Paragraph("Orçamento para", FONTE_SUBTITULO));
            cellCliente.addElement(new Paragraph(nomeCliente != null ? nomeCliente : "-", FONTE_CELULA_NEGRITO));
            if (telefoneCliente != null && !telefoneCliente.isBlank()) {
                cellCliente.addElement(new Paragraph(telefoneCliente, FONTE_CELULA_MUTED));
            }
            infoCliente.addCell(cellCliente);
            document.add(infoCliente);

            boolean temDesconto = orcamento.getValorDescontoItens() != null
                    && orcamento.getValorDescontoItens().compareTo(BigDecimal.ZERO) > 0;

            PdfPTable table = temDesconto
                    ? new PdfPTable(new float[]{38, 12, 18, 12, 20})
                    : new PdfPTable(new float[]{46, 14, 20, 20});
            table.setWidthPercentage(100);
            String[] cabecalhos = temDesconto
                    ? new String[]{"Produto", "Qtd.", "Preço unit.", "Desc.", "Subtotal"}
                    : new String[]{"Produto", "Qtd.", "Preço unit.", "Subtotal"};
            for (String h : cabecalhos) {
                PdfPCell cell = new PdfPCell(new Phrase(h, FONTE_CABECALHO_TABELA));
                cell.setBackgroundColor(NAVY);
                cell.setPadding(6);
                cell.setBorderColor(NAVY);
                table.addCell(cell);
            }

            int linha = 0;
            for (ItemOrcamento item : orcamento.getItens()) {
                boolean par = linha++ % 2 == 1;
                table.addCell(celula(item.getProduto().getNome(), FONTE_CELULA, par));
                table.addCell(celula(String.valueOf(item.getQuantidade()), FONTE_CELULA, par));
                table.addCell(celula(FMT_MOEDA.format(item.getPrecoUnitario()), FONTE_CELULA, par));
                if (temDesconto) {
                    BigDecimal percentual = item.getPercentualDesconto();
                    String desc = percentual != null && percentual.compareTo(BigDecimal.ZERO) > 0
                            ? percentual.stripTrailingZeros().toPlainString() + "%"
                            : "-";
                    table.addCell(celula(desc, FONTE_CELULA_MUTED, par));
                }
                table.addCell(celula(FMT_MOEDA.format(item.getSubtotal()), FONTE_CELULA_NEGRITO, par));
            }
            document.add(table);

            Paragraph espaco = new Paragraph(" ");
            espaco.setSpacingAfter(4);
            document.add(espaco);

            PdfPTable totalTable = new PdfPTable(new float[]{60, 40});
            totalTable.setWidthPercentage(100);
            PdfPCell vazio = new PdfPCell(new Phrase(""));
            vazio.setBorder(Rectangle.NO_BORDER);
            totalTable.addCell(vazio);
            PdfPCell totalCell = new PdfPCell();
            totalCell.setBackgroundColor(BLUE_LIGHT);
            totalCell.setBorderColor(BLUE_LIGHT);
            totalCell.setPadding(10);
            if (temDesconto) {
                Paragraph brutoLinha = new Paragraph(
                        "Subtotal: " + FMT_MOEDA.format(orcamento.getValorBruto())
                                + "   ·   Desconto: -" + FMT_MOEDA.format(orcamento.getValorDescontoItens()),
                        FONTE_CELULA_MUTED);
                totalCell.addElement(brutoLinha);
            }
            Paragraph totalLabel = new Paragraph("Valor total", FONTE_SUBTITULO);
            Paragraph totalValor = new Paragraph(FMT_MOEDA.format(orcamento.getValorTotal()), FONTE_TOTAL);
            totalCell.addElement(totalLabel);
            totalCell.addElement(totalValor);
            totalTable.addCell(totalCell);
            totalTable.setSpacingAfter(16);
            document.add(totalTable);

            if (orcamento.getObservacoes() != null && !orcamento.getObservacoes().isBlank()) {
                document.add(new Paragraph("Observações", FONTE_SECAO));
                Paragraph obs = new Paragraph(orcamento.getObservacoes(), FONTE_CELULA);
                obs.setSpacingBefore(4);
                document.add(obs);
            }

            // area de assinatura do cliente, com espaco em branco acima da linha para assinar
            PdfPTable assinaturaTable = new PdfPTable(new float[]{58, 42});
            assinaturaTable.setWidthPercentage(100);
            assinaturaTable.setSpacingBefore(50);

            PdfPCell linhaAssinatura = new PdfPCell();
            linhaAssinatura.setBorder(Rectangle.TOP);
            linhaAssinatura.setBorderColorTop(MUTED);
            linhaAssinatura.setBorderWidthTop(0.75f);
            linhaAssinatura.setPaddingTop(6);
            linhaAssinatura.addElement(new Paragraph("Assinatura do cliente", FONTE_CELULA_MUTED));
            assinaturaTable.addCell(linhaAssinatura);

            PdfPCell linhaData = new PdfPCell();
            linhaData.setBorder(Rectangle.TOP);
            linhaData.setBorderColorTop(MUTED);
            linhaData.setBorderWidthTop(0.75f);
            linhaData.setPaddingTop(6);
            linhaData.addElement(new Paragraph("Data:  ____ / ____ / ________", FONTE_CELULA_MUTED));
            assinaturaTable.addCell(linhaData);

            document.add(assinaturaTable);

            document.close();
            return baos.toByteArray();
        } catch (DocumentException e) {
            throw new IllegalStateException("Erro ao gerar PDF do orçamento", e);
        }
    }

    private PdfPCell celula(String texto, Font fonte, boolean linhaPar) {
        PdfPCell cell = new PdfPCell(new Phrase(texto == null ? "" : texto, fonte));
        cell.setPadding(5);
        cell.setBorderColor(BORDER);
        if (linhaPar) cell.setBackgroundColor(LIGHT_BG);
        return cell;
    }
}
