package com.kernith.easyinvoice.service.backup;

import com.kernith.easyinvoice.data.model.Company;
import com.kernith.easyinvoice.data.repository.CompanyRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BackupSchedulerTests {

    @Test
    void scheduleWeeklyBackupsSkipsWhenAlreadyRunning() {
        CompanyRepository companyRepository = mock(CompanyRepository.class);
        BackupQueue queue = mock(BackupQueue.class);
        BackupService service = mock(BackupService.class);
        BackupScheduler scheduler = new BackupScheduler(companyRepository, queue, service);

        ReflectionTestUtils.setField(scheduler, "running", new java.util.concurrent.atomic.AtomicBoolean(true));

        scheduler.scheduleWeeklyBackups();

        verify(companyRepository, never()).findAllByOrderByNameAsc();
        verify(service).logEvent("Backup scheduler skipped: previous run still in progress.");
    }

    @Test
    void scheduleWeeklyBackupsEnqueuesCompaniesAndRunsWorkers() {
        CompanyRepository companyRepository = mock(CompanyRepository.class);
        BackupQueue queue = mock(BackupQueue.class);
        BackupService service = mock(BackupService.class);
        BackupScheduler scheduler = new BackupScheduler(companyRepository, queue, service);

        Company c1 = new Company();
        Company c2 = new Company();
        ReflectionTestUtils.setField(c1, "id", 1L);
        ReflectionTestUtils.setField(c2, "id", 2L);
        when(companyRepository.findAllByOrderByNameAsc()).thenReturn(List.of(c1, c2));
        when(queue.dequeue(60_000)).thenReturn(null);

        scheduler.scheduleWeeklyBackups();

        verify(queue).enqueue(1L);
        verify(queue).enqueue(2L);
        verify(service, atLeastOnce()).logEvent(contains("Backup scheduler started"));
    }
}
