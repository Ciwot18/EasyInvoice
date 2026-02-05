package com.kernith.easyinvoice.service.backup;

import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BackupWorkerTests {

    @Test
    void workerProcessesCompanyAndStopsWhenQueueEmpty() {
        BackupQueue queue = mock(BackupQueue.class);
        BackupService service = mock(BackupService.class);
        when(queue.dequeue(60_000)).thenReturn(10L, null);

        new BackupWorker(queue, service).run();

        verify(service).backupCompany(10L);
        verify(service, atLeastOnce()).logEvent(contains("Worker processing company 10"));
        verify(service, atLeastOnce()).logEvent(contains("Backup queue empty"));
    }

    @Test
    void workerLogsWhenBackupFails() {
        BackupQueue queue = mock(BackupQueue.class);
        BackupService service = mock(BackupService.class);
        when(queue.dequeue(60_000)).thenReturn(10L, null);
        doThrow(new IllegalStateException("boom")).when(service).backupCompany(10L);

        new BackupWorker(queue, service).run();

        verify(service, times(1)).backupCompany(10L);
        verify(service, atLeastOnce()).logEvent(contains("Worker failed company 10"));
    }
}
