package com.skillbridge.backend.domain.engine;

import org.springframework.stereotype.Component;

/**
 * Server Source of Truth: Escrow & Double-Entry Ledger Calculation
 * Invariant: Debits MUST strictly equal Credits.
 * All monetary units are handled as 64-bit integer paise (1 INR = 100 paise).
 */
@Component
public class EscrowLedgerEngine {

    public record LedgerJournal(
        long escrowLiabilityDebitPaise,
        long platformRevenueCreditPaise,
        long workerWalletCreditPaise,
        double commissionPercent
    ) {
        public LedgerJournal {
            if (escrowLiabilityDebitPaise != platformRevenueCreditPaise + workerWalletCreditPaise) {
                throw new IllegalStateException("Double-Entry Invariant Violation: Debits must strictly equal Credits");
            }
        }
    }

    public LedgerJournal calculateSettlement(long totalHeldPaise, double commissionPercent) {
        if (totalHeldPaise <= 0) {
            throw new IllegalArgumentException("Held escrow must be strictly positive");
        }
        if (commissionPercent < 10.0 || commissionPercent > 20.0) {
            throw new IllegalArgumentException("Platform commission must be configured between 10% and 20%");
        }

        long commissionPaise = (long) ((totalHeldPaise * commissionPercent) / 100.0);
        long workerPayoutPaise = totalHeldPaise - commissionPaise;

        return new LedgerJournal(
            totalHeldPaise,
            commissionPaise,
            workerPayoutPaise,
            commissionPercent
        );
    }

    public boolean validateRefund(long heldPaise, long requestedRefundPaise) {
        if (requestedRefundPaise <= 0) {
            return false;
        }
        return requestedRefundPaise <= heldPaise;
    }
}
