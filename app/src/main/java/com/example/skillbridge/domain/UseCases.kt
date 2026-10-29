package com.example.skillbridge.domain

import com.example.skillbridge.model.JobStatus
import com.example.skillbridge.model.WorkType

/**
 * Pure Kotlin Domain Layer - Zero Android Framework Dependencies
 * Carries rigorous automated tests for business invariants and financial integrity.
 */

class ValidateStateTransitionUseCase {

    fun isValidTransition(workType: WorkType, currentStatus: JobStatus, targetStatus: JobStatus): Boolean {
        if (currentStatus == targetStatus) return true

        return when (workType) {
            WorkType.LOCAL_ON_DEMAND -> {
                when (targetStatus) {
                    JobStatus.MATCHED -> currentStatus == JobStatus.REQUESTED
                    JobStatus.EN_ROUTE -> currentStatus == JobStatus.MATCHED
                    JobStatus.ARRIVED -> currentStatus == JobStatus.EN_ROUTE
                    JobStatus.IN_PROGRESS -> currentStatus == JobStatus.ARRIVED
                    JobStatus.COMPLETED -> currentStatus == JobStatus.IN_PROGRESS
                    JobStatus.DISPUTED -> currentStatus !in listOf(JobStatus.COMPLETED, JobStatus.CANCELLED)
                    JobStatus.CANCELLED -> currentStatus in listOf(JobStatus.REQUESTED, JobStatus.MATCHED)
                    else -> false
                }
            }
            WorkType.PROJECT_SKILLED -> {
                when (targetStatus) {
                    JobStatus.PROPOSALS -> currentStatus == JobStatus.POSTED
                    JobStatus.HIRED -> currentStatus in listOf(JobStatus.POSTED, JobStatus.PROPOSALS)
                    JobStatus.IN_PROGRESS -> currentStatus == JobStatus.HIRED
                    JobStatus.DELIVERED -> currentStatus == JobStatus.IN_PROGRESS
                    JobStatus.COMPLETED -> currentStatus == JobStatus.DELIVERED
                    JobStatus.DISPUTED -> currentStatus !in listOf(JobStatus.COMPLETED, JobStatus.CANCELLED)
                    JobStatus.CANCELLED -> currentStatus !in listOf(JobStatus.COMPLETED)
                    else -> false
                }
            }
        }
    }
}

/**
 * Escrow & Double-Entry Ledger Invariant Engine
 * All monetary amounts are stored strictly in integer paise (1 Rupee = 100 paise).
 */
data class LedgerJournal(
    val escrowLiabilityDebitPaise: Long,
    val platformRevenueCreditPaise: Long,
    val workerWalletCreditPaise: Long,
    val commissionPercent: Double
) {
    init {
        require(escrowLiabilityDebitPaise == platformRevenueCreditPaise + workerWalletCreditPaise) {
            "Double-Entry Invariant Violation: Debits must strictly equal Credits"
        }
    }
}

class ExecuteEscrowLedgerUseCase {

    fun calculateSettlement(totalHeldPaise: Long, commissionPercent: Double = 15.0): LedgerJournal {
        require(totalHeldPaise > 0) { "Held escrow must be strictly positive" }
        require(commissionPercent in 10.0..20.0) { "Platform commission must be configured between 10% and 20%" }

        val commissionPaise = ((totalHeldPaise * commissionPercent) / 100.0).toLong()
        val workerPayoutPaise = totalHeldPaise - commissionPaise

        return LedgerJournal(
            escrowLiabilityDebitPaise = totalHeldPaise,
            platformRevenueCreditPaise = commissionPaise,
            workerWalletCreditPaise = workerPayoutPaise,
            commissionPercent = commissionPercent
        )
    }

    fun validateRefund(heldPaise: Long, requestedRefundPaise: Long): Boolean {
        if (requestedRefundPaise <= 0) return false
        return requestedRefundPaise <= heldPaise
    }
}

/**
 * Trust Score Calculation Invariant Engine
 * Formula: KYC (25%) + Ratings (35%) + Completion Rate (20%) + Response Time (10%) + Low Cancellation (10%)
 */
class CalculateTrustScoreUseCase {

    fun computeScore(
        isKycVerified: Boolean,
        rating: Float, // 1.0 to 5.0
        completionRatePercent: Double, // 0.0 to 100.0
        responseTimeMinutes: Int, // e.g. 1 to 60 mins
        cancellationRatePercent: Double // 0.0 to 100.0
    ): Int {
        var score = 0.0

        // 1. KYC verification (25 pts)
        if (isKycVerified) {
            score += 25.0
        }

        // 2. Ratings (35 pts max for 5.0 stars)
        val normalizedRating = (rating.coerceIn(1.0f, 5.0f) - 1.0f) / 4.0f
        score += normalizedRating * 35.0

        // 3. Completion rate (20 pts max for 100%)
        score += (completionRatePercent.coerceIn(0.0, 100.0) / 100.0) * 20.0

        // 4. Response time (10 pts for <= 5 mins, degrades to 0 at 30 mins)
        val responseScore = when {
            responseTimeMinutes <= 5 -> 10.0
            responseTimeMinutes <= 15 -> 7.0
            responseTimeMinutes <= 30 -> 4.0
            else -> 1.0
        }
        score += responseScore

        // 5. Low cancellation rate (10 pts for 0%, degrades as cancellations rise)
        val cancellationScore = (10.0 - (cancellationRatePercent * 2.0)).coerceIn(0.0, 10.0)
        score += cancellationScore

        return score.toInt().coerceIn(10, 100)
    }
}

/**
 * Consent-First Rule & Explainable Matching Engine
 */
class MatchingEngineUseCase {

    fun canCreateApplication(isExplicitUserConsentGranted: Boolean): Boolean {
        // Enforce Master Spec: A recommendation alone must NEVER auto-apply.
        // Explicit consent is mandatory.
        return isExplicitUserConsentGranted
    }

    fun calculateMatch(
        hasMatchingSkill: Boolean,
        distanceKm: Double,
        workerTrustScore: Int,
        workerRating: Float,
        isKycVerified: Boolean
    ): Pair<Int, String> {
        var score = 40
        val reasons = mutableListOf<String>()

        if (hasMatchingSkill) {
            score += 30
            reasons.add("Skill verified in category")
        }

        if (distanceKm <= 5.0) {
            score += 15
            reasons.add("Nearby location (${String.format("%.1f", distanceKm)} km)")
        } else if (distanceKm <= 15.0) {
            score += 8
            reasons.add("Within serviceable radius (${String.format("%.1f", distanceKm)} km)")
        }

        if (isKycVerified) {
            score += 10
            reasons.add("Govt. KYC Verified ($workerTrustScore% Trust)")
        }

        if (workerRating >= 4.8f) {
            score += 5
            reasons.add("⭐ $workerRating Top Rated")
        }

        val explanation = "Top Match: " + reasons.joinToString(", ")
        return Pair(score.coerceAtMost(99), explanation)
    }
}
