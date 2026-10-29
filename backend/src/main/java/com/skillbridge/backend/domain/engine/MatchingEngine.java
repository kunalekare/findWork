package com.skillbridge.backend.domain.engine;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Server Source of Truth: Consent-First & Explainable Matching Engine
 */
@Component
public class MatchingEngine {

    public record MatchResult(int score, String explanation) {}

    public boolean canCreateApplication(boolean isExplicitUserConsentGranted) {
        // Enforce Master Policy: An AI or recommendation system must NEVER auto-apply without explicit consent.
        return isExplicitUserConsentGranted;
    }

    public MatchResult calculateMatch(
        boolean hasMatchingSkill,
        double distanceKm,
        int workerTrustScore,
        float workerRating,
        boolean isKycVerified
    ) {
        int score = 40;
        List<String> reasons = new ArrayList<>();

        if (hasMatchingSkill) {
            score += 30;
            reasons.add("Skill verified in category");
        }

        if (distanceKm <= 5.0) {
            score += 15;
            reasons.add(String.format(Locale.ROOT, "Nearby location (%.1f km)", distanceKm));
        } else if (distanceKm <= 15.0) {
            score += 8;
            reasons.add(String.format(Locale.ROOT, "Within serviceable radius (%.1f km)", distanceKm));
        }

        if (isKycVerified) {
            score += 10;
            reasons.add(String.format(Locale.ROOT, "Govt. KYC Verified (%d%% Trust)", workerTrustScore));
        }

        if (workerRating >= 4.8f) {
            score += 5;
            reasons.add(String.format(Locale.ROOT, "⭐ %.2f Top Rated", workerRating));
        }

        String explanation = "Top Match: " + String.join(", ", reasons);
        return new MatchResult(Math.min(99, score), explanation);
    }
}
