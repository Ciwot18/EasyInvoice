package com.kernith.easyinvoice.service.backup;

import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BackupQueueTests {

    @Test
    void enqueueAndDequeueWork() {
        BackupQueue queue = new BackupQueue();
        queue.enqueue(10L);

        Long value = queue.dequeue(10);
        assertEquals(10L, value);
    }

    @Test
    void enqueueIgnoresNull() {
        BackupQueue queue = new BackupQueue();
        queue.enqueue(null);

        assertNull(queue.dequeue(5));
    }

    @Test
    void dequeueReturnsNullOnTimeout() {
        BackupQueue queue = new BackupQueue();
        assertNull(queue.dequeue(1));
    }

    @Test
    void dequeueReturnsNullWhenInterrupted() throws Exception {
        BackupQueue queue = new BackupQueue();
        AtomicReference<Long> result = new AtomicReference<>();
        Thread thread = new Thread(() -> result.set(queue.dequeue(10_000)));
        thread.start();
        Thread.sleep(5);
        thread.interrupt();
        thread.join(1000);

        assertNull(result.get());
        assertTrue(thread.isInterrupted());
    }
}
