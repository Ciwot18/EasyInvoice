package com.kernith.easyinvoice.service.backup;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BackupServiceTests {

    @TempDir
    Path tempDir;

    @Test
    void backupCompanyThrowsWhenCompanyIdMissing() {
        BackupService service = new BackupService(tempDir.toString());
        assertThrows(IllegalArgumentException.class, () -> service.backupCompany(null));
    }

    @Test
    void backupCompanyReturnsNullWhenCompanyFolderMissing() {
        BackupService service = new BackupService(tempDir.toString());
        assertNull(service.backupCompany(10L));
    }

    @Test
    void backupCompanyCreatesZipWithPdfFiles() throws Exception {
        BackupService service = new BackupService(tempDir.toString());
        Path companyDir = tempDir.resolve("companies/10/invoices");
        Files.createDirectories(companyDir);
        Files.write(companyDir.resolve("a.pdf"), "pdf".getBytes());
        Files.write(companyDir.resolve("b.txt"), "skip".getBytes());

        Path output = service.backupCompany(10L);

        assertNotNull(output);
        assertTrue(Files.exists(output));
    }

    @Test
    void logEventWritesLogFile() throws Exception {
        BackupService service = new BackupService(tempDir.toString());
        service.logEvent("Hello");

        Path logFile = tempDir.resolve("backup/backup-debug.log");
        assertTrue(Files.exists(logFile));
        String content = Files.readString(logFile);
        assertTrue(content.contains("Hello"));
    }
}
