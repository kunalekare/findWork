package com.skillbridge.backend;

import com.skillbridge.backend.domain.engine.EscrowLedgerEngine;
import com.skillbridge.backend.domain.engine.MatchingEngine;
import com.skillbridge.backend.domain.engine.TrustScoreEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Shared Test Vectors — Validates exact mathematical and business invariant parity
 * between Java Server (source of truth) and Kotlin Client preview.
 */
public class SharedTestVectorsTest {

    private TrustScoreEngine trustScoreEngine;
    private EscrowLedgerEngine escrowLedgerEngine;
    private MatchingEngine matchingEngine;

    @BeforeEach
    void setUp() {
        trustScoreEngine = new TrustScoreEngine();
        escrowLedgerEngine = new EscrowLedgerEngine();
        matchingEngine = new MatchingEngine();
    }

    @Test
    @DisplayName("Vector 1: Perfect Worker Trust Score must evaluate to 100")
    void testVector1_PerfectTrustScore() {
        int score = trustScoreEngine.computeScore(
            true,   // isKycVerified (25 pts)
            5.0f,   // rating (35 pts)
            100.0,  // completion (20 pts)
            4,      // response <= 5m (10 pts)
            0.0     // cancellation 0% (10 pts)
        );
        assertEquals(100, score);
    }

    @Test
    @DisplayName("Vector 2: Degraded Worker Trust Score matches client preview")
    void testVector2_DegradedTrustScore() {
        int score = trustScoreEngine.computeScore(
            false,  // isKycVerified (0 pts)
            4.0f,   // rating 4.0 ((3.0/4.0)*35 = 26.25 pts)
            80.0,   // completion (16.0 pts)
            12,     // response 12m (7.0 pts)
            2.0     // cancellation 2% (10 - 4 = 6.0 pts)
        );
        // Total ~ 55
        assertEquals(55, score);
    }

    @Test
    @DisplayName("Vector 3: Double-Entry Escrow 15% Platform Commission in Integer Paise")
    void testVector3_DoubleEntryStandardEscrow() {
        long heldPaise = 100000L; // ₹1,000.00
        var journal = escrowLedgerEngine.calculateSettlement(heldPaise, 15.0);

        assertEquals(100000L, journal.escrowLiabilityDebitPaise());
        assertEquals(15000L, journal.platformRevenueCreditPaise());
        assertEquals(85000L, journal.workerWalletCreditPaise());
        assertEquals(journal.escrowLiabilityDebitPaise(), journal.platformRevenueCreditPaise() + journal.workerWalletCreditPaise());
    }

    @Test
    @DisplayName("Vector 4: Pro Worker 10% Platform Commission in Integer Paise")
    void testVector4_DoubleEntryProWorkerEscrow() {
        long heldPaise = 250000L; // ₹2,500.00
        var journal = escrowLedgerEngine.calculateSettlement(heldPaise, 10.0);

        assertEquals(250000L, journal.escrowLiabilityDebitPaise());
        assertEquals(25000L, journal.platformRevenueCreditPaise());
        assertEquals(225000L, journal.workerWalletCreditPaise());
        assertEquals(journal.escrowLiabilityDebitPaise(), journal.platformRevenueCreditPaise() + journal.workerWalletCreditPaise());
    }

    @Test
    @DisplayName("Vector 5: Refund Ceiling Validation")
    void testVector5_RefundCeiling() {
        long heldPaise = 50000L;
        assertTrue(escrowLedgerEngine.validateRefund(heldPaise, 25000L));
        assertTrue(escrowLedgerEngine.validateRefund(heldPaise, 50000L));
        assertFalse(escrowLedgerEngine.validateRefund(heldPaise, 50001L));
        assertFalse(escrowLedgerEngine.validateRefund(heldPaise, 0L));
        assertFalse(escrowLedgerEngine.validateRefund(heldPaise, -500L));
    }

    @Test
    @DisplayName("Vector 6: Consent-First Rule Enforced on Server")
    void testVector6_ConsentFirstRule() {
        assertFalse(matchingEngine.canCreateApplication(false));
        assertTrue(matchingEngine.canCreateApplication(true));
    }

    @Test
    @DisplayName("Vector 7: Explainable Matching Engine Scoring & Reason Output")
    void testVector7_ExplainableMatchScoring() {
        var result = matchingEngine.calculateMatch(
            true,
            3.2,
            96,
            4.9f,
            true
        );

        assertTrue(result.score() >= 85);
        assertTrue(result.explanation().contains("Skill verified in category"));
        assertTrue(result.explanation().contains("Nearby location"));
        assertTrue(result.explanation().contains("Govt. KYC Verified (96% Trust)"));
        assertTrue(result.explanation().contains("Top Rated"));
    }
}
