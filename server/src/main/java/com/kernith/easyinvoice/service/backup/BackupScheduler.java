package com.kernith.easyinvoice.service.backup;

import com.kernith.easyinvoice.data.repository.CompanyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

@Configuration
@EnableScheduling
public class BackupScheduler {
    private static final Logger log = LoggerFactory.getLogger(BackupScheduler.class);

    private final CompanyRepository companyRepository;
    private final BackupQueue backupQueue;
    private final BackupService backupService;
    private final AtomicBoolean running = new AtomicBoolean(false);

    public BackupScheduler(
            CompanyRepository companyRepository,
            BackupQueue backupQueue,
            BackupService backupService
    ) {
        this.companyRepository = companyRepository;
        this.backupQueue = backupQueue;
        this.backupService = backupService;
    }

    @Scheduled(cron = "${backup.cron:0 0 2 ? * SUN}", zone = "${backup.zone:Europe/Rome}")
    public void scheduleWeeklyBackups() {
        if (!running.compareAndSet(false, true)) {
            log.info("Backup scheduler skipped: previous run still in progress.");
            backupService.logEvent("Backup scheduler skipped: previous run still in progress.");
            return;
        }

        ExecutorService pool = null;
        try {
            var companies = companyRepository.findAllByOrderByNameAsc();
            backupService.logEvent("Backup scheduler started. Companies: " + companies.size());
            log.info("Backup scheduler started. Companies: {}", companies.size());

            int n = Math.min(Math.max(1, companies.size() / 10), 10);
            pool = Executors.newFixedThreadPool(n);

            for (int i = 0; i < n; i++) {
                pool.submit(new BackupWorker(backupQueue, backupService));
            }

            for (var company : companies) {
                backupQueue.enqueue(company.getId());
            }
        } finally {
            if (pool != null) {
                pool.shutdown();
                try {
                    if (!pool.awaitTermination(2, java.util.concurrent.TimeUnit.HOURS)) {
                        pool.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    pool.shutdownNow();
                    Thread.currentThread().interrupt();
                }
            }
            running.set(false);
        }
    }
}