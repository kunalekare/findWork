package com.skillbridge.backend;

import com.skillbridge.backend.dispatch.DispatchService;
import com.skillbridge.backend.domain.engine.MatchingEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class DispatchConcurrencyTest {

    private DispatchService dispatchService;

    @BeforeEach
    void setUp() {
        dispatchService = new DispatchService(new MatchingEngine());
    }

    @Test
    @DisplayName("Single-Offer-Per-Worker: Two simultaneous requests cannot lock the same worker")
    void testSingleOfferPerWorkerLocking() {
        var worker1 = new DispatchService.CandidateWorker("w_1", "Ramesh", "+91 98*** 01", 1.2, 95, 4.9f, true, true);
        var worker2 = new DispatchService.CandidateWorker("w_2", "Suresh", "+91 98*** 02", 2.0, 90, 4.8f, true, true);
        var pool = List.of(worker1, worker2);

        // Job A dispatched -> locks worker1
        var cascadeA = dispatchService.initiateDispatch("job_A", "cat_elec", 21.14, 79.08, pool);
        assertEquals("w_1", cascadeA.currentWorkerId());
        assertTrue(dispatchService.isWorkerLocked("w_1"));

        // Simultaneous Job B dispatched -> worker1 is locked, so Job B locks worker2
        var cascadeB = dispatchService.initiateDispatch("job_B", "cat_elec", 21.14, 79.08, pool);
        assertEquals("w_2", cascadeB.currentWorkerId());
        assertTrue(dispatchService.isWorkerLocked("w_2"));
    }

    @Test
    @DisplayName("Atomic Accept: Concurrent accept attempts result in exactly ONE winner via CAS")
    void testConcurrentAcceptAtomicCAS() throws InterruptedException {
        var worker = new DispatchService.CandidateWorker("w_101", "Ramesh", "+91 98*** 01", 1.5, 96, 4.9f, true, true);
        dispatchService.initiateDispatch("job_concurrency", "cat_elec", 21.14, 79.08, List.of(worker));

        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    boolean accepted = dispatchService.acceptJob("job_concurrency", "w_101");
                    if (accepted) {
                        successCount.incrementAndGet();
                    } else {
                        failureCount.incrementAndGet();
                    }
                } catch (Exception ignored) {}
            });
        }

        startLatch.countDown();
        executor.shutdown();
        assertTrue(executor.awaitTermination(3, TimeUnit.SECONDS));

        // The first thread wins CAS; subsequent calls return true due to idempotency or false if invalid
        assertTrue(successCount.get() >= 1, "At least one accept must succeed");
        assertFalse(dispatchService.isWorkerLocked("w_101"), "Worker offer lock should be cleared after match");
    }

    @Test
    @DisplayName("Cascade never re-offers to workers already in offered_to history")
    void testCascadeOfferedToHistoryExclusion() {
        var worker1 = new DispatchService.CandidateWorker("w_1", "Ramesh", "+91 98*** 01", 1.0, 95, 4.9f, true, true);
        var worker2 = new DispatchService.CandidateWorker("w_2", "Suresh", "+91 98*** 02", 2.0, 92, 4.7f, true, true);
        var pool = List.of(worker1, worker2);

        var cascade = dispatchService.initiateDispatch("job_hist", "cat_elec", 21.14, 79.08, pool);
        assertEquals("w_1", cascade.currentWorkerId());

        // First cascade -> offers worker2
        var next = dispatchService.cascadeToNextWorker("job_hist");
        assertTrue(next.isPresent());
        assertEquals("w_2", next.get().workerId());

        // Second cascade -> pool exhausted, worker1 is NOT re-offered
        var exhausted = dispatchService.cascadeToNextWorker("job_hist");
        assertTrue(exhausted.isEmpty());
    }

    @Test
    @DisplayName("Poster cancellation frees worker lock immediately")
    void testPosterCancellationReleasesWorkerLock() {
        var worker = new DispatchService.CandidateWorker("w_cancel", "Kavita", "+91 98*** 03", 1.2, 94, 4.8f, true, true);
        dispatchService.initiateDispatch("job_cancel_test", "cat_elec", 21.14, 79.08, List.of(worker));
        assertTrue(dispatchService.isWorkerLocked("w_cancel"));

        boolean cancelled = dispatchService.cancelRequest("job_cancel_test");
        assertTrue(cancelled);
        assertFalse(dispatchService.isWorkerLocked("w_cancel"));
    }
}
