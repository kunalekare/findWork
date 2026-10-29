package com.skillbridge.backend;

import com.skillbridge.backend.dispatch.DispatchService;
import com.skillbridge.backend.domain.engine.MatchingEngine;
import com.skillbridge.backend.statemachine.JobStateMachineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class JobStateMachineTest {

    private JobStateMachineService stateMachineService;
    private DispatchService dispatchService;

    @BeforeEach
    void setUp() {
        stateMachineService = new JobStateMachineService();
        dispatchService = new DispatchService(new MatchingEngine());
    }

    @Test
    @DisplayName("Local on-demand forward progression is legal")
    void testLocalOnDemandForwardProgression() {
        String jobId = "job_test_1";
        var type = JobStateMachineService.WorkType.LOCAL_ON_DEMAND;

        assertEquals(JobStateMachineService.JobStatus.MATCHED, stateMachineService.transition(jobId, type, JobStateMachineService.JobStatus.MATCHED));
        assertEquals(JobStateMachineService.JobStatus.EN_ROUTE, stateMachineService.transition(jobId, type, JobStateMachineService.JobStatus.EN_ROUTE));
        assertEquals(JobStateMachineService.JobStatus.ARRIVED, stateMachineService.transition(jobId, type, JobStateMachineService.JobStatus.ARRIVED));
        assertEquals(JobStateMachineService.JobStatus.IN_PROGRESS, stateMachineService.transition(jobId, type, JobStateMachineService.JobStatus.IN_PROGRESS));
        assertEquals(JobStateMachineService.JobStatus.COMPLETED, stateMachineService.transition(jobId, type, JobStateMachineService.JobStatus.COMPLETED));
    }

    @Test
    @DisplayName("Illegal transition directly from REQUESTED to COMPLETED must throw IllegalStateException")
    void testIllegalStateTransitionRejected() {
        String jobId = "job_test_2";
        var type = JobStateMachineService.WorkType.LOCAL_ON_DEMAND;

        assertThrows(IllegalStateException.class, () -> {
            stateMachineService.transition(jobId, type, JobStateMachineService.JobStatus.COMPLETED);
        });
    }

    @Test
    @DisplayName("20-Second cascade switches candidate worker on expiration")
    void testDispatchCascadeToNextWorker() {
        String jobId = "job_test_3";
        var worker1 = new DispatchService.CandidateWorker("w_1", "Ramesh", "+91 98*** 01", 1.2, 95, 4.9f, true, true);
        var worker2 = new DispatchService.CandidateWorker("w_2", "Suresh", "+91 98*** 02", 2.4, 90, 4.7f, true, true);

        var cascade = dispatchService.initiateDispatch(jobId, "cat_elec", 21.1458, 79.0882, List.of(worker1, worker2));
        assertEquals("w_1", cascade.currentWorkerId());

        // Cascade to next candidate
        var nextCandidate = dispatchService.cascadeToNextWorker(jobId);
        assertTrue(nextCandidate.isPresent());
        assertEquals("w_2", nextCandidate.get().workerId());

        // Worker 2 accepts within active window
        assertTrue(dispatchService.acceptJob(jobId, "w_2"));
    }
}
