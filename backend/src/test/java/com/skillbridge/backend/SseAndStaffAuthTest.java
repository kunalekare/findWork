package com.skillbridge.backend;

import com.skillbridge.backend.security.JwtService;
import com.skillbridge.backend.service.AuthService;
import com.skillbridge.backend.websocket.LiveTrackingEventGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.junit.jupiter.api.Assertions.*;

public class SseAndStaffAuthTest {

    private AuthService authService;
    private JwtService jwtService;
    private LiveTrackingEventGateway eventGateway;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(
            "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970",
            3600000L,
            86400000L
        );
        authService = new AuthService(jwtService);
        eventGateway = new LiveTrackingEventGateway();
    }

    @Test
    @DisplayName("Staff authentication locks portal after 5 consecutive failed attempts")
    void testStaffLoginLockoutAfterFiveAttempts() {
        String staffEmail = "test_staff@skillbridge.in";
        String wrongPassword = "WrongPassword#123";

        // Attempts 1 through 4 should fail with IllegalArgumentException
        for (int i = 1; i <= 4; i++) {
            int finalI = i;
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
                authService.authenticateStaff(staffEmail, wrongPassword);
            });
            assertTrue(ex.getMessage().contains("Remaining attempts before lockout: " + (5 - finalI)));
        }

        // Attempt 5 should trigger 15-minute lockout and throw IllegalStateException
        IllegalStateException lockoutEx = assertThrows(IllegalStateException.class, () -> {
            authService.authenticateStaff(staffEmail, wrongPassword);
        });
        assertTrue(lockoutEx.getMessage().contains("Staff portal locked for 15 minutes"));

        // Subsequent attempt within 15 minutes remains locked
        IllegalStateException stillLockedEx = assertThrows(IllegalStateException.class, () -> {
            authService.authenticateStaff(staffEmail, "SkillBridgeSecureStaff#2026");
        });
        assertTrue(stillLockedEx.getMessage().contains("Staff account locked due to excessive failed attempts"));
    }

    @Test
    @DisplayName("Staff login succeeds with valid credentials and clears tracker")
    void testStaffLoginSuccess() {
        var tokens = authService.authenticateStaff("staff@skillbridge.in", "SkillBridgeSecureStaff#2026");
        assertNotNull(tokens);
        assertEquals("ADMIN", tokens.role());
        assertNotNull(tokens.accessToken());
        assertNotNull(tokens.refreshToken());
    }

    @Test
    @DisplayName("Phone OTP enforces maximum 3 attempts and invalid code rejection")
    void testPhoneOtpRateLimiting() {
        String phone = "+919822011223";
        authService.sendPhoneOtp(phone);

        // Attempt 1: wrong OTP
        IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class, () -> {
            authService.verifyPhoneOtp(phone, "000000");
        });
        assertTrue(ex1.getMessage().contains("Remaining attempts: 2"));

        // Attempt 2: wrong OTP
        IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class, () -> {
            authService.verifyPhoneOtp(phone, "111111");
        });
        assertTrue(ex2.getMessage().contains("Remaining attempts: 1"));

        // Attempt 3: correct OTP succeeds
        var tokens = authService.verifyPhoneOtp(phone, "123456");
        assertNotNull(tokens);
        assertEquals("WORKER", tokens.role());
    }

    @Test
    @DisplayName("SSE gateway subscribes, broadcasts events, and handles replay buffer")
    void testSseBroadcastAndReplayBuffer() {
        String channelId = "job_sse_channel_99";

        // Subscribe to channel
        SseEmitter emitter = eventGateway.subscribe(channelId, null, null);
        assertNotNull(emitter);
        assertEquals(1, eventGateway.getActiveConnectionCount());

        // Broadcast events
        eventGateway.broadcastEvent(channelId, "STATUS_UPDATE", "EN_ROUTE");
        eventGateway.broadcastEvent(channelId, "LOCATION_UPDATE", "{\"lat\":21.1458,\"lng\":79.0882}");

        // Heartbeat should not drop active connection
        eventGateway.sendHeartbeat(channelId);
        assertEquals(1, eventGateway.getActiveConnectionCount());

        // Verify catch-up subscribe with Last-Event-ID header
        SseEmitter reconnectEmitter = eventGateway.subscribe(channelId, "1000", null);
        assertNotNull(reconnectEmitter);
    }

    @Test
    @DisplayName("JWT refresh token generates fresh access token with rotation")
    void testRefreshTokenRotation() {
        String userId = "usr_pilot_404";
        String phone = "+919822099999";
        String initialRefresh = jwtService.generateRefreshToken(userId);

        var refreshed = authService.refreshAccessToken(initialRefresh, userId, phone, "WORKER");
        assertNotNull(refreshed.accessToken());
        assertNotNull(refreshed.refreshToken());
        assertNotEquals(initialRefresh, refreshed.refreshToken());
    }
}
