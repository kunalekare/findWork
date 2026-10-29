package com.skillbridge.backend.maps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * GoogleMapsUsageTracker:
 * Tracks daily API request volumes against a configurable budget.
 * Issues alert logs at 80% threshold and activates a circuit breaker (kill switch)
 * at 100% threshold, instantly falling back to Euclidean straight-line distance.
 */
@Service
public class GoogleMapsUsageTracker {

    private static final Logger log = LoggerFactory.getLogger(GoogleMapsUsageTracker.class);

    private int dailyBudget = 5000;
    private final AtomicInteger currentDayRequests = new AtomicInteger(0);
    private boolean manualKillSwitch = false;

    public GoogleMapsUsageTracker() {}

    public GoogleMapsUsageTracker(int dailyBudget) {
        this.dailyBudget = dailyBudget;
    }

    public synchronized boolean recordRequest() {
        if (isKillSwitchActive()) {
            log.warn("Google Maps usage budget exhausted or kill switch active. Falling back to straight-line distance.");
            return false;
        }

        int count = currentDayRequests.incrementAndGet();
        double usageRatio = (double) count / (double) dailyBudget;

        if (usageRatio >= 1.0) {
            log.error("ALERT: Google Maps daily budget limit reached ({}/{} requests, 100%). Activating automatic kill switch.", count, dailyBudget);
            return false;
        } else if (usageRatio >= 0.8 && usageRatio < 0.81) {
            log.warn("WARNING: Google Maps daily API usage has crossed 80% ({}/{} requests). Approaching quota ceiling.", count, dailyBudget);
        }

        return true;
    }

    public boolean isKillSwitchActive() {
        return manualKillSwitch || currentDayRequests.get() >= dailyBudget;
    }

    public void setManualKillSwitch(boolean active) {
        this.manualKillSwitch = active;
        log.warn("Google Maps manual kill switch toggled to: {}", active);
    }

    public int getCurrentDayRequests() {
        return currentDayRequests.get();
    }

    public int getDailyBudget() {
        return dailyBudget;
    }

    public void setDailyBudget(int dailyBudget) {
        this.dailyBudget = dailyBudget;
    }

    public void resetDailyCounter() {
        currentDayRequests.set(0);
        log.info("Reset daily Google Maps usage counter to 0.");
    }
}
