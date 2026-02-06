package com.kernith.easyinvoice.service.backup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Creates PDF backups for a company by zipping its stored documents.
 */
@Service
public class BackupService {
    private static final Logger log = LoggerFactory.getLogger(BackupService.class);
    private static final DateTimeFormatter TS_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private final Path storageRoot;
    private final Path backupRoot;
    private final Path backupLogFile;
    private final Object logLock = new Object();

    /**
     * Creates the service and resolves storage paths.
     *
     * @param storageRoot base storage directory
     */
    public BackupService(@Value("${storage.root:storage}") String storageRoot) {
        this.storageRoot = Paths.get(storageRoot).toAbsolutePath().normalize();
        this.backupRoot = this.storageRoot.resolve("backup").normalize();
        this.backupLogFile = this.backupRoot.resolve("backup-debug.log");
    }

    /**
     * Builds a ZIP archive of all PDFs for a company.
     *
     * <p>Lifecycle: validate input, resolve company path, gather PDFs, zip, and log.</p>
     *
     * @param companyId company identifier
     * @return output ZIP path or null if company folder is missing
     * @throws IllegalArgumentException if companyId is null
     * @throws IllegalStateException if backup creation fails
     */
    public Path backupCompany(Long companyId) {
        if (companyId == null) {
            throw new IllegalArgumentException("CompanyId is required for backup");
        }

        LocalDateTime now = LocalDateTime.now();
        Path companyRoot = resolveCompanyRoot(companyId);
        logEvent("Starting backup for company " + companyId + " from " + companyRoot);

        if (!Files.exists(companyRoot)) {
            logEvent("Company folder not found for company " + companyId + ": " + companyRoot);
            log.warn("Company folder missing: {}", companyRoot);
            return null;
        }

        Path outputDir = resolveBackupDir(companyId);
        String filename = "backup_" + now.format(TS_FORMAT) + ".zip";
        Path outputFile = outputDir.resolve(filename);

        try {
            Files.createDirectories(outputDir);
            int files = createZip(companyRoot, outputFile);
            logEvent("Backup completed for company " + companyId + ". Files: " + files + ". Output: " + outputFile);
            log.info("Backup completed for company {}. Files: {}. Output: {}", companyId, files, outputFile);
            return outputFile;
        } catch (IOException e) {
            logEvent("Backup failed for company " + companyId + ". Error: " + e.getMessage());
            throw new IllegalStateException("Failed to create backup for company " + companyId, e);
        }
    }

    /**
     * Appends a log line to the backup debug log.
     *
     * @param message log message
     */
    public void logEvent(String message) {
        String line = LocalDateTime.now().format(TS_FORMAT) + " - " + message;
        synchronized (logLock) {
            try {
                Files.createDirectories(backupRoot);
                Files.writeString(
                        backupLogFile,
                        line + System.lineSeparator(),
                        StandardOpenOption.CREATE,
                        StandardOpenOption.WRITE,
                        StandardOpenOption.APPEND
                );
            } catch (IOException e) {
                log.warn("Failed to write backup log file: {}", backupLogFile, e);
            }
        }
    }

    /**
     * Creates a ZIP archive containing PDF files under the company root.
     *
     * @param companyRoot company storage directory
     * @param outputFile output zip file
     * @return number of files added to the archive
     * @throws IOException if file operations fail
     */
    private int createZip(Path companyRoot, Path outputFile) throws IOException {
        List<Path> files;
        try (var stream = Files.walk(companyRoot)) {
            files = stream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().toLowerCase(Locale.ROOT).endsWith(".pdf"))
                    .toList();
        }

        try (OutputStream out = Files.newOutputStream(outputFile, StandardOpenOption.CREATE_NEW);
             ZipOutputStream zip = new ZipOutputStream(out)) {
            for (Path file : files) {
                String entryName = companyRoot.relativize(file).toString().replace('\\', '/');
                ZipEntry entry = new ZipEntry(entryName);
                zip.putNextEntry(entry);
                Files.copy(file, zip);
                zip.closeEntry();
            }
        }

        return files.size();
    }

    /**
     * Resolves the storage directory for a company.
     *
     * @param companyId company identifier
     * @return normalized company directory
     * @throws IllegalStateException if the path is invalid
     */
    private Path resolveCompanyRoot(Long companyId) {
        Path dir = storageRoot.resolve("companies").resolve(companyId.toString()).normalize();
        if (!dir.startsWith(storageRoot)) {
            throw new IllegalStateException("Invalid company storage path");
        }
        return dir;
    }

    /**
     * Resolves the output directory for company backups.
     *
     * @param companyId company identifier
     * @return normalized backup directory
     * @throws IllegalStateException if the path is invalid
     */
    private Path resolveBackupDir(Long companyId) {
        Path dir = backupRoot.resolve(companyId.toString()).normalize();
        if (!dir.startsWith(backupRoot)) {
            throw new IllegalStateException("Invalid backup output path");
        }
        return dir;
    }
}
