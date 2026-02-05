package com.kernith.easyinvoice.service.backup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.Queue;

@Component
public class BackupQueue {
    private static final Logger log = LoggerFactory.getLogger(BackupQueue.class);
    private final Queue<Long> companyIds = new ArrayDeque<>();

    synchronized void enqueue(Long id) {
        if (id == null) { return; }
        companyIds.add(id);
        notifyAll();
    }

    synchronized Long dequeue(long timeoutMs) {
        long end = System.currentTimeMillis() + timeoutMs;
        while (companyIds.isEmpty()) {
            long left = end - System.currentTimeMillis();
            if (left <= 0) return null;
            try {
                wait(left);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }
        return companyIds.poll();
    }
}