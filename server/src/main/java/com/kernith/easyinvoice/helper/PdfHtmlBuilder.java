package com.kernith.easyinvoice.helper;

import com.kernith.easyinvoice.helper.adapter.PdfDocumentView;
import com.kernith.easyinvoice.helper.adapter.PdfLineView;

import java.util.Comparator;
import java.util.List;

import static com.kernith.easyinvoice.helper.Utils.esc;
import static com.kernith.easyinvoice.helper.Utils.nvl;

public final class PdfHtmlBuilder {

    private PdfDocumentView doc;

    private String companyAddress = "";
    private String customerAddress = "";

    public PdfHtmlBuilder document(PdfDocumentView doc) {
        this.doc = doc;
        return this;
    }

    public PdfHtmlBuilder companyAddress(String companyAddress) {
        this.companyAddress = nvl(companyAddress);
        return this;
    }

    public PdfHtmlBuilder customerAddress(String customerAddress) {
        this.customerAddress = nvl(customerAddress);
        return this;
    }

    public String build() {
        if (doc == null) throw new IllegalStateException("document is required");

        return """
            <html xmlns="http://www.w3.org/1999/xhtml">
            <head>
              <meta charset="utf-8"/>
              %s
            </head>
            <body>
              %s
              %s
              %s
              %s
              %s
            </body>
            </html>
            """.formatted(
                css(),
                headerSection(),
                customerSection(),
                itemsSection(),
                totalsSection(),
                notesSection()
        );
    }

    /* ---------------- Sections ---------------- */

    private String headerSection() {
        String due = nvl(doc.dueDateLabel()).isBlank()
                ? ""
                : "<br/>Scadenza: " + esc(doc.dueDateLabel());

        String address = companyAddress.isBlank()
                ? ""
                : "<div class=\"muted small\">" + esc(companyAddress) + "</div>";

        return """
            <table class="header">
              <tr>
                <td style="width:60%%;">
                  <div class="brand">%s</div>
                  %s<br/>
                  %s
                </td>

                <td style="width:40%%; text-align:right;">
                  <div class="doc-title">%s</div>
                  <div class="muted">#%s</div>
                  <div class="pill">Stato: %s</div>
                  <div class="muted small" style="margin-top:8px;">
                    Data: %s%s
                  </div>
                </td>
              </tr>
            </table>
            <div class="hr"></div>
            """.formatted(
                esc(doc.companyName()),
                address,
                esc(doc.companyVAT()),
                esc(doc.title()),
                esc(doc.numberLabel()),
                esc(doc.statusLabel()),
                esc(doc.issueDateLabel()),
                due
        );
    }

    private String customerSection() {
        String cAddr = customerAddress.isBlank()
                ? ""
                : "<div class=\"muted\">" + esc(customerAddress) + "</div>";

        return """
            <table class="info">
              <tr>
                <td style="width:60%%; vertical-align:top;">
                  <div class="muted small" style="font-weight:700; margin-bottom:6px;">Cliente</div>
                  <div style="font-weight:700;">%s</div>
                  %s
                  <div class="muted">P. IVA: %s</div>
                  <div class="muted">%s</div>
                </td>

                <td style="width:40%%; vertical-align:top; text-align:right;">
                  <div class="muted small" style="font-weight:700; margin-bottom:6px;">Valuta</div>
                  <div>%s</div>
                </td>
              </tr>
            </table>
            """.formatted(
                esc(doc.customerName()),
                cAddr,
                esc(doc.customerVAT()),
                esc(doc.customerEmail()),
                esc(doc.currency())
        );
    }

    private String itemsSection() {
        String rows = buildRows(doc.lines());

        return """
            <table class="items">
              <thead>
                <tr>
                  <th style="width:52%%;">Descrizione</th>
                  <th class="num" style="width:10%%;">Q.tà</th>
                  <th style="width:10%%;">Unità</th>
                  <th class="num" style="width:14%%;">Prezzo</th>
                  <th class="num" style="width:14%%;">Totale</th>
                </tr>
              </thead>

              <tbody>
                %s
              </tbody>
            </table>
            """.formatted(rows);
    }

    private String buildRows(List<? extends PdfLineView> lines) {
        if (lines == null || lines.isEmpty()) {
            return """
                <tr>
                  <td colspan="5" class="muted">Nessuna riga presente</td>
                </tr>
                """;
        }

        return lines.stream()
                .sorted(Comparator.comparingInt(PdfLineView::position))
                .map(it -> {
                    String note = nvl(it.notes()).isBlank()
                            ? ""
                            : "<div class=\"muted small\" style=\"margin-top:4px;\">" + esc(it.notes()) + "</div>";

                    return """
                    <tr>
                      <td>
                        <div style="font-weight:700;">%s</div>
                        %s
                      </td>
                      <td class="num">%s</td>
                      <td>%s</td>
                      <td class="num">%s</td>
                      <td class="num">%s</td>
                    </tr>
                    """.formatted(
                            esc(it.description()),
                            note,
                            esc(it.qtyLabel()),
                            esc(it.unitLabel()),
                            esc(it.unitPriceLabel()),
                            esc(it.lineTotalLabel())
                    );
                })
                .reduce("", String::concat);
    }

    private String totalsSection() {
        return """
            <table class="totals">
              <tr>
                <td class="label">Subtotale</td>
                <td class="num">%s</td>
              </tr>
              <tr>
                <td class="label">IVA</td>
                <td class="num">%s</td>
              </tr>
              <tr class="line">
                <td class="label grand">Totale</td>
                <td class="num grand">%s</td>
              </tr>
            </table>
            """.formatted(
                esc(doc.subtotalLabel()),
                esc(doc.taxLabel()),
                esc(doc.totalLabel())
        );
    }

    private String notesSection() {
        String notes = nvl(doc.notes());
        if (notes.isBlank()) return "";

        return """
            <div class="notes">
              <div class="muted small" style="font-weight:700; margin-bottom:6px;">Note</div>
              <div>%s</div>
            </div>
            """.formatted(esc(notes));
    }

    /* ---------------- CSS ---------------- */

    private String css() {
        return """
        <style>
          @page {
            size: A4;
            margin: 18mm 14mm 20mm 14mm;
            @bottom-right {
              content: "Pag. " counter(page) " / " counter(pages);
              font-size: 9.5px;
              color: #666;
            }
          }

          body {
            font-family: Arial, sans-serif;
            font-size: 12px;
            color: #111;
            line-height: 1.35;
          }
          .muted { color: #666; }
          .small { font-size: 10.5px; }
          .hr { height: 1px; background: #e7e7e7; margin: 14px 0; }

          .header { width: 100%; }
          .header td { vertical-align: top; }

          .brand { font-size: 18px; font-weight: 700; letter-spacing: 0.2px; }
          .doc-title { font-size: 16px; font-weight: 700; margin: 0; padding: 0; }
          .pill {
            display: inline-block;
            padding: 3px 8px;
            border: 1px solid #ddd;
            border-radius: 999px;
            font-size: 10px;
            color: #444;
            margin-top: 6px;
          }

          table.info { width: 100%; border-collapse: collapse; }
          table.info td { padding: 3px 0; }

          table.items {
            width: 100%;
            border-collapse: collapse;
            margin-top: 10px;
          }
          table.items thead th {
            text-align: left;
            font-size: 10.5px;
            color: #555;
            padding: 8px 10px;
            background: #f6f6f6;
            border-bottom: 1px solid #e5e5e5;
          }
          table.items tbody td {
            padding: 10px;
            border-bottom: 1px solid #efefef;
            vertical-align: top;
          }
          .num { text-align: right; white-space: nowrap; }

          table.totals {
            width: 45%;
            margin-left: auto;
            border-collapse: collapse;
            margin-top: 12px;
          }
          table.totals td { padding: 6px 8px; }
          table.totals tr.line td { border-top: 1px solid #e5e5e5; }
          table.totals .label { color: #555; }
          table.totals .grand { font-weight: 800; font-size: 13px; }

          .notes {
            margin-top: 14px;
            padding: 10px 12px;
            border: 1px solid #eee;
            border-radius: 8px;
            background: #fafafa;
          }
        </style>
        """;
    }
}