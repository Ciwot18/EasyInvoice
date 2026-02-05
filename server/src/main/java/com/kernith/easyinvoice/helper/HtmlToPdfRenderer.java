package com.kernith.easyinvoice.helper;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import java.io.ByteArrayOutputStream;

public final class HtmlToPdfRenderer {

    private HtmlToPdfRenderer() {}

    public static byte[] render(String html) {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {

            String cleaned = stripBom(html).trim();
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withHtmlContent(cleaned, null);
            builder.toStream(os);
            builder.run();

            return os.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("PDF generation failed", e);
        }
    }

    private static String stripBom(String s) {
        if (s != null && !s.isEmpty() && s.charAt(0) == '\uFEFF') {
            return s.substring(1);
        }
        return s;
    }
}