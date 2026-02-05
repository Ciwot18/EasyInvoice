package com.kernith.easyinvoice.helper;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HtmlToPdfRendererTests {

    @Test
    void renderReturnsPdfBytes() {
        byte[] pdf = HtmlToPdfRenderer.render("<html><body><p>Test</p></body></html>");

        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
    }

    @Test
    void renderWrapsExceptions() {
        assertThrows(RuntimeException.class, () -> HtmlToPdfRenderer.render(null));
    }
}
