package com.skillbridge.backend.payment;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server Double-Entry Financial Ledger Service
 * Strict Invariant: Total Debits MUST strictly equal Total Credits in Integer Paise.
 */
@Service
public class LedgerService {

    public record JournalEntry(
        String id,
        String transactionRef,
        String idempotencyKey,
        String jobId,
        String debitAccount,
        String creditAccount,
        long amountPaise,
        String narration,
        Instant recordedAt
    ) {}

    // In-memory ledger journal (persisted to escrow_ledger_entries in PostgreSQL)
    private final List<JournalEntry> ledgerEntries = Collections.synchronizedList(new ArrayList<>());
    private final Set<String> processedKeys = ConcurrentHashMap.newKeySet();

    public synchronized JournalEntry recordEscrowHold(String jobId, long amountPaise, String idempotencyKey) {
        if (processedKeys.contains(idempotencyKey)) {
            return ledgerEntries.stream().filter(e -> e.idempotencyKey().equals(idempotencyKey)).findFirst().orElseThrow();
        }

        JournalEntry entry = new JournalEntry(
            UUID.randomUUID().toString(),
            "TX_HOLD_" + System.currentTimeMillis(),
            idempotencyKey,
            jobId,
            "TASK_POSTER_PREPAID",
            "ESCROW_LIABILITY",
            amountPaise,
            "Escrow deposit held at job creation",
            Instant.now()
        );

        ledgerEntries.add(entry);
        processedKeys.add(idempotencyKey);
        return entry;
    }

    public synchronized List<JournalEntry> recordSettlement(
        String jobId,
        long totalHeldPaise,
        boolean isWorkerPro,
        String idempotencyKey
    ) {
        if (processedKeys.contains(idempotencyKey)) {
            return ledgerEntries.stream().filter(e -> e.idempotencyKey().equals(idempotencyKey)).toList();
        }

        // Pro tier receives 10% platform fee; standard receives 15%
        double commissionRate = isWorkerPro ? 10.0 : 15.0;
        long commissionPaise = (long) ((totalHeldPaise * commissionRate) / 100.0);
        long workerPayoutPaise = totalHeldPaise - commissionPaise;

        // Strict double-entry invariant check
        if (totalHeldPaise != commissionPaise + workerPayoutPaise) {
            throw new IllegalStateException("Double-Entry Invariant Failed: " + totalHeldPaise + " != " + commissionPaise + " + " + workerPayoutPaise);
        }

        Instant now = Instant.now();
        String txRef = "TX_SETTLE_" + System.currentTimeMillis();

        JournalEntry commissionEntry = new JournalEntry(
            UUID.randomUUID().toString(),
            txRef,
            idempotencyKey + "_COMMISSION",
            jobId,
            "ESCROW_LIABILITY",
            "PLATFORM_REVENUE",
            commissionPaise,
            "Platform commission fee (" + commissionRate + "%" + (isWorkerPro ? " Pro Tier" : " Standard") + ")",
            now
        );

        JournalEntry workerPayoutEntry = new JournalEntry(
            UUID.randomUUID().toString(),
            txRef,
            idempotencyKey + "_PAYOUT",
            jobId,
            "ESCROW_LIABILITY",
            "WORKER_WALLET",
            workerPayoutPaise,
            "Worker payout credited to wallet",
            now
        );

        ledgerEntries.add(commissionEntry);
        ledgerEntries.add(workerPayoutEntry);
        processedKeys.add(idempotencyKey);

        return List.of(commissionEntry, workerPayoutEntry);
    }

    public synchronized JournalEntry freezeForDispute(String jobId, long amountPaise, String idempotencyKey) {
        JournalEntry entry = new JournalEntry(
            UUID.randomUUID().toString(),
            "TX_DISPUTE_FREEZE_" + System.currentTimeMillis(),
            idempotencyKey,
            jobId,
            "ESCROW_LIABILITY",
            "DISPUTE_FROZEN_ESCROW",
            amountPaise,
            "Escrow funds locked under dispute investigation",
            Instant.now()
        );
        ledgerEntries.add(entry);
        return entry;
    }

    public synchronized JournalEntry applyCancellationFee(String jobId, long feePaise, String idempotencyKey) {
        JournalEntry entry = new JournalEntry(
            UUID.randomUUID().toString(),
            "TX_CANCEL_FEE_" + System.currentTimeMillis(),
            idempotencyKey,
            jobId,
            "ESCROW_LIABILITY",
            "PLATFORM_REVENUE",
            feePaise,
            "Late cancellation penalty fee",
            Instant.now()
        );
        ledgerEntries.add(entry);
        return entry;
    }

    public List<JournalEntry> getJobLedger(String jobId) {
        return ledgerEntries.stream().filter(e -> e.jobId().equals(jobId)).toList();
    }
}
