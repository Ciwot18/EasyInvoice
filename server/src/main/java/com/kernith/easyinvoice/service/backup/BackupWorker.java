package com.kernith.easyinvoice.service.backup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Background worker that consumes the backup queue and processes companies.
 */
public class BackupWorker implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(BackupWorker.class);

    private final BackupQueue backupQueue;
    private final BackupService backupService;

    /**
     * Creates a worker with required queue and service.
     *
     * @param backupQueue queue of company ids
     * @param backupService backup service
     */
    public BackupWorker(BackupQueue backupQueue, BackupService backupService) {
        this.backupQueue = backupQueue;
        this.backupService = backupService;
    }

    /**
     * Runs the worker loop until the queue is empty for the timeout window.
     */
    @Override
    public void run() {
        while (true) {
            Long companyId = backupQueue.dequeue(60_000);   //Wait 60sec
            if (companyId == null) {
                backupService.logEvent("Backup queue empty. Worker exiting.");
                log.info("Backup queue empty. Worker exiting.");
                return;
            }

            try {
                backupService.logEvent("Worker processing company " + companyId);
                backupService.backupCompany(companyId);
            } catch (Exception e) {
                backupService.logEvent("Worker failed company " + companyId + ": " + e.getMessage());
                log.error("Backup failed for company {}", companyId, e);
            }
        }
    }
}
