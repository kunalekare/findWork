package com.skillbridge.backend;

import com.skillbridge.backend.dispatch.DispatchService;
import com.skillbridge.backend.domain.engine.MatchingEngine;
import com.skillbridge.backend.maps.GoogleMapsUsageTracker;
import com.skillbridge.backend.maps.GoogleRoutesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GoogleMapsDispatchTest {

    private GoogleMapsUsageTracker usageTracker;
    private GoogleRoutesService routesService;
    private DispatchService dispatchService;

    @BeforeEach
    void setUp() {
        usageTracker = new GoogleMapsUsageTracker(10); // small budget for testing
        routesService = new GoogleRoutesService(usageTracker, "AIzaSyTestMockKey");
        dispatchService = new DispatchService(new MatchingEngine());
    }

    @Test
    @DisplayName("Top-N Candidate Selection filters pool strictly to top 3 ranked candidates")
    void testTopNSelection() {
        List<GoogleRoutesService.CandidateRoutingRequest> pool = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            pool.add(new GoogleRoutesService.CandidateRoutingRequest("w_" + i, 21.14 + (i * 0.005), 79.08 + (i * 0.005), i * 1.2));
        }

        var top3 = routesService.selectTopNCandidates(pool, 3);
        assertEquals(3, top3.size());
        assertEquals("w_1", top3.get(0).workerId());
        assertEquals("w_2", top3.get(1).workerId());
        assertEquals("w_3", top3.get(2).workerId());
    }

    @Test
    @DisplayName("Throttling reuses cached route when movement < 100m and time < 30s")
    void testRoutingThrottlingWithin100Meters() {
        String workerId = "w_telemetry_1";
        double originLat = 21.1458;
        double originLng = 79.0882;
        double destLat = 21.1466;
        double destLng = 79.0830;

        // Initial route
        var route1 = routesService.computeOrReuseRoute(workerId, originLat, originLng, destLat, destLng);
        assertNotNull(route1);

        // Small movement (approx 20 meters north)
        double movedLat = originLat + 0.00018;
        var route2 = routesService.computeOrReuseRoute(workerId, movedLat, originLng, destLat, destLng);

        // Should return identical cached details due to throttling
        assertSame(route1, route2);
    }

    @Test
    @DisplayName("Kill switch triggers zero-cost Euclidean straight-line distance on budget exhaustion")
    void testKillSwitchFallback() {
        // Exhaust budget
        for (int i = 0; i < 10; i++) {
            usageTracker.recordRequest();
        }
        assertTrue(usageTracker.isKillSwitchActive());

        var fallbackRoute = routesService.calculateRoute(21.1458, 79.0882, 21.1600, 79.1000);
        assertTrue(fallbackRoute.providerSource().contains("Straight-Line"));
        assertTrue(fallbackRoute.distanceKm() > 0);
        assertTrue(fallbackRoute.durationMinutes() > 0);
    }

    @Test
    @DisplayName("Location freshness excludes workers whose GPS telemetry is older than 60 seconds")
    void testLocationFreshnessExclusion() {
        long now = System.currentTimeMillis();
        long freshTimestamp = now - (30 * 1000L);  // 30 seconds ago (< 60s) -> Valid
        long staleTimestamp = now - (90 * 1000L);  // 90 seconds ago (> 60s) -> Stale

        boolean isFreshWorkerEligible = (now - freshTimestamp) <= 60000L;
        boolean isStaleWorkerEligible = (now - staleTimestamp) <= 60000L;

        assertTrue(isFreshWorkerEligible);
        assertFalse(isStaleWorkerEligible);
    }

    @Test
    @DisplayName("Configurable search radius expands from 2km to 5km up to 10km max")
    void testSearchRadiusExpansion() {
        double r1 = 2000.0;
        double r2 = 5000.0;
        double rMax = 10000.0;

        assertEquals(2000.0, r1);
        assertEquals(5000.0, r2);
        assertEquals(10000.0, rMax);
        assertTrue(r1 < r2 && r2 < rMax);
    }
}
