package com.skillbridge.backend;

import com.skillbridge.backend.invoice.GstInvoiceService;
import com.skillbridge.backend.payment.LedgerService;
import com.skillbridge.backend.payment.RazorpayService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class LedgerAndPaymentTest {

    private LedgerService ledgerService;
    private RazorpayService razorpayService;
    private GstInvoiceService gstInvoiceService;

    @BeforeEach
    void setUp() {
        ledgerService = new LedgerService();
        razorpayService = new RazorpayService("rzp_test_mock", "mock_secret", "mock_webhook_secret");
        gstInvoiceService = new GstInvoiceService("27AABCS1423Q1ZP", "998713", 18.0);
    }

    @Test
    @DisplayName("Ledger enforces standard 15% commission with perfect double-entry equality")
    void testStandardEscrowSettlement() {
        String jobId = "job_test_escrow_1";
        long totalHeldPaise = 85000L; // ₹850.00

        var entries = ledgerService.recordSettlement(jobId, totalHeldPaise, false, "IDEMP_SETTLE_1");
        assertEquals(2, entries.size());

        var commissionEntry = entries.get(0);
        var payoutEntry = entries.get(1);

        assertEquals(12750L, commissionEntry.amountPaise()); // 15% of 85,000 = 12,750
        assertEquals(72250L, payoutEntry.amountPaise());      // 85% of 85,000 = 72,250
        assertEquals(totalHeldPaise, commissionEntry.amountPaise() + payoutEntry.amountPaise());
    }

    @Test
    @DisplayName("Worker Pro tier receives 10% commission discount on settlement")
    void testWorkerProDiscountSettlement() {
        String jobId = "job_test_escrow_2";
        long totalHeldPaise = 100000L; // ₹1,000.00

        var entries = ledgerService.recordSettlement(jobId, totalHeldPaise, true, "IDEMP_SETTLE_PRO");
        assertEquals(2, entries.size());

        var commissionEntry = entries.get(0);
        var payoutEntry = entries.get(1);

        assertEquals(10000L, commissionEntry.amountPaise()); // 10% of 100,000 = 10,000
        assertEquals(90000L, payoutEntry.amountPaise());      // 90% of 100,000 = 90,000
        assertEquals(totalHeldPaise, commissionEntry.amountPaise() + payoutEntry.amountPaise());
    }

    @Test
    @DisplayName("Webhook signature verification and idempotent deduplication")
    void testWebhookSignatureAndIdempotency() {
        String payload = "{\"event\":\"payment.captured\",\"job_id\":\"job_301\"}";
        assertTrue(razorpayService.verifyWebhookSignature(payload, "mock_valid_webhook_sig"));

        String eventId = "evt_unique_101";
        // First process must return true
        assertTrue(razorpayService.processWebhookIdempotently(eventId, "payment.captured", Map.of()));
        // Duplicate replay must return false
        assertFalse(razorpayService.processWebhookIdempotently(eventId, "payment.captured", Map.of()));
    }

    @Test
    @DisplayName("GST invoice carries prominent test-mode configuration disclaimer")
    void testGstInvoiceDisclaimer() {
        var invoice = gstInvoiceService.generateInvoice(
            "job_301",
            "Emergency Electrical Wiring",
            85000L,
            "Ramesh Sharma",
            "Anjali Deshmukh"
        );

        assertTrue(invoice.isPlaceholderMode());
        assertTrue(invoice.complianceDisclaimer().contains("TEST-MODE PLACEHOLDERS"));
        assertEquals("998713", invoice.sacCode());
        assertEquals("27AABCS1423Q1ZP", invoice.gstin());
        assertEquals(85000L, invoice.totalAmountPaise());
    }
}
