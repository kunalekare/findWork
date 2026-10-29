package com.skillbridge.backend.controller;

import com.skillbridge.backend.common.ApiResponse;
import com.skillbridge.backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/v1/auth")
@Tag(name = "Authentication", description = "Phone OTP and Staff Authentication Endpoints")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    public record SendOtpRequest(@NotBlank String phone) {}

    @PostMapping("/otp/send")
    @Operation(summary = "Send rate-limited 6-digit OTP to phone")
    public ResponseEntity<ApiResponse<Map<String, String>>> sendOtp(@Valid @RequestBody SendOtpRequest request) {
        authService.sendPhoneOtp(request.phone());
        return ResponseEntity.ok(ApiResponse.ok(Map.of(
            "phone", request.phone(),
            "status", "OTP_SENT",
            "message", "6-digit OTP code sent successfully"
        )));
    }

    public record VerifyOtpRequest(
        @NotBlank String phone,
        @NotBlank String otp
    ) {}

    @PostMapping("/otp/verify")
    @Operation(summary = "Verify OTP and issue JWT access and refresh tokens")
    public ResponseEntity<ApiResponse<AuthService.AuthTokens>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        var tokens = authService.verifyPhoneOtp(request.phone(), request.otp());
        return ResponseEntity.ok(ApiResponse.ok(tokens));
    }

    public record StaffLoginRequest(
        @NotBlank @Email String email,
        @NotBlank String password
    ) {}

    @PostMapping("/staff/login")
    @Operation(summary = "Staff authentication with server checks, rate limiting and lockout protection")
    public ResponseEntity<ApiResponse<AuthService.AuthTokens>> staffLogin(@Valid @RequestBody StaffLoginRequest request) {
        var tokens = authService.authenticateStaff(request.email(), request.password());
        return ResponseEntity.ok(ApiResponse.ok(tokens));
    }
}
