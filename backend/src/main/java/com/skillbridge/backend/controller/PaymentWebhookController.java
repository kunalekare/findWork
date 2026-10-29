package com.skillbridge.backend.controller;

import com.skillbridge.backend.common.ApiResponse;
import com.skillbridge.backend.payment.LedgerService;
import com.skillbridge.backend.payment.RazorpayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/v1/payments")
@Tag(name = "Payments & Escrow", description = "Razorpay Gateway Integration and Webhooks")
public class PaymentWebhookController {

    private final RazorpayService razorpayService;
    private final LedgerService ledgerService;

    public PaymentWebhookController(RazorpayService razorpayService, LedgerService ledgerService) {
        this.razorpayService = razorpayService;
        this.ledgerService = ledgerService;
    }

    public record CreateOrderRequest(long amountPaise, String jobId) {}

    @PostMapping("/create-order")
    @Operation(summary = "Create Razorpay TEST mode payment order with escrow reservation")
    public ResponseEntity<ApiResponse<RazorpayService.RazorpayOrder>> createOrder(
        @RequestBody CreateOrderRequest request
    ) {
        var order = razorpayService.createOrder(request.amountPaise(), request.jobId());
        return ResponseEntity.ok(ApiResponse.ok(order));
    }

    @PostMapping("/razorpay/webhook")
    @Operation(summary = "Idempotent signed webhook endpoint for Razorpay payment capture and refunds")
    public ResponseEntity<ApiResponse<Map<String, String>>> handleRazorpayWebhook(
        @RequestHeader(value = "X-Razorpay-Signature", required = true) String signature,
        @RequestBody Map<String, Object> payload
    ) {
        String eventId = (String) payload.getOrDefault("event_id", "evt_" + System.currentTimeMillis());
        String event = (String) payload.getOrDefault("event", "payment.captured");

        // 1. Verify webhook signature
        boolean isSignatureValid = razorpayService.verifyWebhookSignature(payload.toString(), signature);
        if (!isSignatureValid) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ApiResponse.error("INVALID_SIGNATURE", "Webhook signature verification failed", null)
            );
        }

        // 2. Process idempotently
        boolean isFirstTime = razorpayService.processWebhookIdempotently(eventId, event, payload);
        if (!isFirstTime) {
            return ResponseEntity.ok(ApiResponse.ok(Map.of(
                "status", "DUPLICATE_IGNORED",
                "message", "Webhook event already processed previously"
            )));
        }

        // 3. Handle event logic (e.g. payment.captured holds escrow in ledger)
        if ("payment.captured".equals(event)) {
            String jobId = (String) payload.getOrDefault("job_id", "job_general");
            ledgerService.recordEscrowHold(jobId, 85000L, eventId);
        }

        return ResponseEntity.ok(ApiResponse.ok(Map.of(
            "status", "PROCESSED",
            "event", event,
            "eventId", eventId
        )));
    }
}
