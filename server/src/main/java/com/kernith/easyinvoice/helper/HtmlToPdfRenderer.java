package com.kernith.easyinvoice.helper;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import java.io.ByteArrayOutputStream;

/**
 * Renders HTML content into a PDF document using OpenHTMLToPDF.
 */
public final class HtmlToPdfRenderer {

    private HtmlToPdfRenderer() {}

    /**
     * Generates a PDF from the given HTML string.
     *
     * @param html HTML markup to render
     * @return PDF bytes
     */
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

    /**
     * Removes a UTF-8 BOM if present at the start of the string.
     *
     * @param s input string
     * @return string without BOM
     */
    private static String stripBom(String s) {
        if (s != null && !s.isEmpty() && s.charAt(0) == '\uFEFF') {
            return s.substring(1);
        }
        return s;
    }
}
