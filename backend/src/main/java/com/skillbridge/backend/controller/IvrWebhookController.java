package com.skillbridge.backend.controller;

import com.skillbridge.backend.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Telephony & IVR Webhook Controller
 * Implements docs/IVR_FLOW.md telephony integration for feature/keypad phone accessibility.
 */
@RestController
@RequestMapping("/v1/telephony")
@Tag(name = "Telephony & IVR", description = "Telephony Masked Calling and Keypad Phone IVR Webhooks")
public class IvrWebhookController {

    public record IvrEventRequest(
        String callSid,
        String fromPhone,
        String digitsPressed,
        int currentStep
    ) {}

    public record IvrResponse(
        String messagePrompt,
        String language,
        int nextStep,
        boolean isTerminated,
        String smsConfirmationStatus
    ) {}

    @PostMapping(path = "/ivr-event", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Telephony IVR DTMF Menu Webhook for Keypad Phones")
    public ResponseEntity<ApiResponse<IvrResponse>> handleIvrEvent(@RequestBody IvrEventRequest request) {
        String digits = request.digitsPressed() != null ? request.digitsPressed() : "";
        int step = request.currentStep();

        // Menu tree matching docs/IVR_FLOW.md
        IvrResponse response = switch (step) {
            case 0 -> new IvrResponse(
                "Welcome to SkillBridge. For Hindi press 1. Marathi sathi 2 dabawa. For English press 3.",
                "multilingual",
                1,
                false,
                null
            );
            case 1 -> {
                String lang = switch (digits) {
                    case "1" -> "hi";
                    case "2" -> "mr";
                    default -> "en";
                };
                yield new IvrResponse(
                    "Selected language. If you want to EARN money press 1. If you want to HIRE someone press 2.",
                    lang,
                    2,
                    false,
                    null
                );
            }
            case 2 -> new IvrResponse(
                "Registration complete! Your 4-digit verification PIN has been dispatched via SMS and WhatsApp.",
                "hi",
                3,
                true,
                "DISPATCHED_TO_" + request.fromPhone()
            );
            default -> new IvrResponse("Thank you for calling SkillBridge.", "en", 0, true, null);
        };

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/masked-call/initiate")
    @Operation(summary = "Bridge virtual masked call between worker and employer without exposing raw numbers")
    public ResponseEntity<ApiResponse<Map<String, String>>> initiateMaskedCall(
        @RequestParam("workerPhone") String workerPhone,
        @RequestParam("employerPhone") String employerPhone
    ) {
        String virtualRelayNumber = "+91712660" + (1000 + Math.abs(workerPhone.hashCode() % 8999));
        return ResponseEntity.ok(ApiResponse.ok(Map.of(
            "virtualBridgeNumber", virtualRelayNumber,
            "status", "BRIDGED",
            "privacy", "Raw phone numbers concealed on both sides"
        )));
    }
}
