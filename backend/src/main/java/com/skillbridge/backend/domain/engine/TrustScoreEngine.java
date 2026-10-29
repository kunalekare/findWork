package com.skillbridge.backend.domain.engine;

import org.springframework.stereotype.Component;

/**
 * Server Source of Truth: Trust Score Calculation
 * Weight Allocation Invariant:
 * KYC (25%) + Ratings (35%) + Completion Rate (20%) + Response Time (10%) + Low Cancellation (10%)
 */
@Component
public class TrustScoreEngine {

    public int computeScore(
        boolean isKycVerified,
        float rating,
        double completionRatePercent,
        int responseTimeMinutes,
        double cancellationRatePercent
    ) {
        double score = 0.0;

        // 1. KYC verification (25 pts)
        if (isKycVerified) {
            score += 25.0;
        }

        // 2. Ratings (35 pts max for 5.0 stars, normalized between 1.0 and 5.0)
        float clampedRating = Math.max(1.0f, Math.min(5.0f, rating));
        double normalizedRating = (clampedRating - 1.0) / 4.0;
        score += normalizedRating * 35.0;

        // 3. Completion rate (20 pts max for 100%)
        double clampedCompletion = Math.max(0.0, Math.min(100.0, completionRatePercent));
        score += (clampedCompletion / 100.0) * 20.0;

        // 4. Response time (10 pts for <= 5 mins)
        double responseScore;
        if (responseTimeMinutes <= 5) {
            responseScore = 10.0;
        } else if (responseTimeMinutes <= 15) {
            responseScore = 7.0;
        } else if (responseTimeMinutes <= 30) {
            responseScore = 4.0;
        } else {
            responseScore = 1.0;
        }
        score += responseScore;

        // 5. Low cancellation rate (10 pts max, degrades by 2 pts per % cancellation)
        double cancellationScore = Math.max(0.0, Math.min(10.0, 10.0 - (cancellationRatePercent * 2.0)));
        score += cancellationScore;

        int finalScore = (int) Math.round(score);
        return Math.max(10, Math.min(100, finalScore));
    }
}
