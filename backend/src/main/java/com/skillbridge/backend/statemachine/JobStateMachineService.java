package com.skillbridge.backend.statemachine;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server-Enforced Job State Machine
 * Local on-demand: REQUESTED -> MATCHED -> EN_ROUTE -> ARRIVED -> IN_PROGRESS -> COMPLETED (-> DISPUTED) | CANCELLED
 * Rejects all illegal transitions with INVALID_STATE_TRANSITION.
 */
@Service
public class JobStateMachineService {

    public enum JobStatus {
        POSTED,
        PROPOSALS,
        HIRED,
        REQUESTED,
        MATCHED,
        EN_ROUTE,
        ARRIVED,
        IN_PROGRESS,
        DELIVERED,
        COMPLETED,
        DISPUTED,
        CANCELLED
    }

    public enum WorkType {
        LOCAL_ON_DEMAND,
        PROJECT_SKILLED
    }

    // In-memory job state cache for high-speed transition checks
    private final Map<String, JobStatus> jobStatuses = new ConcurrentHashMap<>();

    public boolean isValidTransition(WorkType workType, JobStatus current, JobStatus target) {
        if (current == target) return true;

        return switch (workType) {
            case LOCAL_ON_DEMAND -> switch (target) {
                case MATCHED -> current == JobStatus.REQUESTED;
                case EN_ROUTE -> current == JobStatus.MATCHED;
                case ARRIVED -> current == JobStatus.EN_ROUTE;
                case IN_PROGRESS -> current == JobStatus.ARRIVED;
                case COMPLETED -> current == JobStatus.IN_PROGRESS;
                case DISPUTED -> current != JobStatus.COMPLETED && current != JobStatus.CANCELLED;
                case CANCELLED -> current == JobStatus.REQUESTED || current == JobStatus.MATCHED;
                default -> false;
            };
            case PROJECT_SKILLED -> switch (target) {
                case PROPOSALS -> current == JobStatus.POSTED;
                case HIRED -> current == JobStatus.POSTED || current == JobStatus.PROPOSALS;
                case IN_PROGRESS -> current == JobStatus.HIRED;
                case DELIVERED -> current == JobStatus.IN_PROGRESS;
                case COMPLETED -> current == JobStatus.DELIVERED;
                case DISPUTED -> current != JobStatus.COMPLETED && current != JobStatus.CANCELLED;
                case CANCELLED -> current != JobStatus.COMPLETED;
                default -> false;
            };
        };
    }

    public synchronized JobStatus transition(String jobId, WorkType workType, JobStatus targetStatus) {
        JobStatus current = jobStatuses.getOrDefault(jobId, workType == WorkType.LOCAL_ON_DEMAND ? JobStatus.REQUESTED : JobStatus.POSTED);

        if (!isValidTransition(workType, current, targetStatus)) {
            throw new IllegalStateException("Illegal state transition from " + current + " to " + targetStatus + " for " + workType);
        }

        jobStatuses.put(jobId, targetStatus);
        return targetStatus;
    }

    public JobStatus getStatus(String jobId, WorkType defaultType) {
        return jobStatuses.getOrDefault(jobId, defaultType == WorkType.LOCAL_ON_DEMAND ? JobStatus.REQUESTED : JobStatus.POSTED);
    }
}
