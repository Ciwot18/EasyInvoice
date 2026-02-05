package com.kernith.easyinvoice.helper;

import com.kernith.easyinvoice.helper.adapter.PdfDocumentView;
import com.kernith.easyinvoice.helper.adapter.PdfLineView;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PdfHtmlBuilderTests {

    @Test
    void buildThrowsWhenDocumentMissing() {
        PdfHtmlBuilder builder = new PdfHtmlBuilder();
        assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    void buildRendersEmptyLinesAndOmitsNotes() {
        PdfDocumentView doc = new TestDoc(
                List.of(),
                "",
                "",
                "",
                ""
        );

        String html = new PdfHtmlBuilder()
                .document(doc)
                .build();

        assertTrue(html.contains("Nessuna riga presente"));
        assertTrue(!html.contains("Note</div>"));
    }

    @Test
    void buildRendersLinesSortedAndEscapesAddresses() {
        List<PdfLineView> lines = new ArrayList<>();
        lines.add(new TestLine(2, "Line B", "", "2", "h", "€ 10.00", "€ 20.00"));
        lines.add(new TestLine(1, "Line A", "note", "1", "", "€ 5.00", "€ 5.00"));

        PdfDocumentView doc = new TestDoc(
                lines,
                "2025-02-01",
                "Notes & <b>safe</b>",
                "Via <Roma>",
                "Client & Co"
        );

        String html = new PdfHtmlBuilder()
                .document(doc)
                .companyAddress("Via <Roma>")
                .customerAddress("Client & Co")
                .build();

        assertTrue(html.contains("Notes &amp; &lt;b&gt;safe&lt;/b&gt;"));
        assertTrue(html.contains("Via &lt;Roma&gt;"));
        assertTrue(html.contains("Client &amp; Co"));

        int firstIndex = html.indexOf("Line A");
        int secondIndex = html.indexOf("Line B");
        assertTrue(firstIndex >= 0 && secondIndex > firstIndex);
    }

    private record TestLine(
            int position,
            String description,
            String notes,
            String qtyLabel,
            String unitLabel,
            String unitPriceLabel,
            String lineTotalLabel
    ) implements PdfLineView {
        @Override public String taxRateLabel() { return ""; }
    }

    private record TestDoc(
            List<? extends PdfLineView> lines,
            String dueDateLabel,
            String notes,
            String companyAddress,
            String customerAddress
    ) implements PdfDocumentView {
        @Override public String title() { return "Doc"; }
        @Override public String numberLabel() { return "1"; }
        @Override public String statusLabel() { return "DRAFT"; }
        @Override public String issueDateLabel() { return "2025-01-01"; }
        @Override public String companyName() { return "Acme"; }
        @Override public String customerName() { return "Customer"; }
        @Override public String companyVAT() { return "IT123"; }
        @Override public String customerVAT() { return "IT456"; }
        @Override public String customerEmail() { return "info@acme.test"; }
        @Override public String currency() { return "EUR"; }
        @Override public String subtotalLabel() { return "€ 0.00"; }
        @Override public String taxLabel() { return "€ 0.00"; }
        @Override public String totalLabel() { return "€ 0.00"; }
    }
}
