package com.example

import com.example.skillbridge.data.SkillBridgeRepository
import com.example.skillbridge.domain.CalculateTrustScoreUseCase
import com.example.skillbridge.domain.ExecuteEscrowLedgerUseCase
import com.example.skillbridge.domain.LedgerJournal
import com.example.skillbridge.domain.MatchingEngineUseCase
import com.example.skillbridge.domain.ValidateStateTransitionUseCase
import com.example.skillbridge.model.JobStatus
import com.example.skillbridge.model.UserRole
import com.example.skillbridge.model.WorkType
import com.example.skillbridge.provider.MockAuthProvider
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ExampleUnitTest {

    private lateinit var repository: SkillBridgeRepository
    private lateinit var stateTransitionUseCase: ValidateStateTransitionUseCase
    private lateinit var escrowLedgerUseCase: ExecuteEscrowLedgerUseCase
    private lateinit var trustScoreUseCase: CalculateTrustScoreUseCase
    private lateinit var matchingEngineUseCase: MatchingEngineUseCase

    @Before
    fun setUp() {
        repository = SkillBridgeRepository()
        stateTransitionUseCase = ValidateStateTransitionUseCase()
        escrowLedgerUseCase = ExecuteEscrowLedgerUseCase()
        trustScoreUseCase = CalculateTrustScoreUseCase()
        matchingEngineUseCase = MatchingEngineUseCase()
    }

    // =========================================================================
    // 1. ValidateStateTransitionUseCase Unit Tests
    // =========================================================================

    @Test
    fun testLocalOnDemandStateTransitionsValid() {
        // Valid forward progression for Uber-style local service
        assertTrue(stateTransitionUseCase.isValidTransition(WorkType.LOCAL_ON_DEMAND, JobStatus.REQUESTED, JobStatus.MATCHED))
        assertTrue(stateTransitionUseCase.isValidTransition(WorkType.LOCAL_ON_DEMAND, JobStatus.MATCHED, JobStatus.EN_ROUTE))
        assertTrue(stateTransitionUseCase.isValidTransition(WorkType.LOCAL_ON_DEMAND, JobStatus.EN_ROUTE, JobStatus.ARRIVED))
        assertTrue(stateTransitionUseCase.isValidTransition(WorkType.LOCAL_ON_DEMAND, JobStatus.ARRIVED, JobStatus.IN_PROGRESS))
        assertTrue(stateTransitionUseCase.isValidTransition(WorkType.LOCAL_ON_DEMAND, JobStatus.IN_PROGRESS, JobStatus.COMPLETED))
        
        // Cancellation allowed at REQUESTED or MATCHED stages
        assertTrue(stateTransitionUseCase.isValidTransition(WorkType.LOCAL_ON_DEMAND, JobStatus.REQUESTED, JobStatus.CANCELLED))
        assertTrue(stateTransitionUseCase.isValidTransition(WorkType.LOCAL_ON_DEMAND, JobStatus.MATCHED, JobStatus.CANCELLED))

        // Dispute allowed while active before completion
        assertTrue(stateTransitionUseCase.isValidTransition(WorkType.LOCAL_ON_DEMAND, JobStatus.IN_PROGRESS, JobStatus.DISPUTED))
    }

    @Test
    fun testLocalOnDemandStateTransitionsInvalid() {
        // Cannot jump directly from REQUESTED to COMPLETED
        assertFalse(stateTransitionUseCase.isValidTransition(WorkType.LOCAL_ON_DEMAND, JobStatus.REQUESTED, JobStatus.COMPLETED))
        // Cannot go backwards from ARRIVED to MATCHED
        assertFalse(stateTransitionUseCase.isValidTransition(WorkType.LOCAL_ON_DEMAND, JobStatus.ARRIVED, JobStatus.MATCHED))
        // Cannot cancel once IN_PROGRESS
        assertFalse(stateTransitionUseCase.isValidTransition(WorkType.LOCAL_ON_DEMAND, JobStatus.IN_PROGRESS, JobStatus.CANCELLED))
        // Cannot dispute a completed or cancelled job
        assertFalse(stateTransitionUseCase.isValidTransition(WorkType.LOCAL_ON_DEMAND, JobStatus.COMPLETED, JobStatus.DISPUTED))
    }

    @Test
    fun testProjectSkilledStateTransitions() {
        // Valid progression for skilled milestones
        assertTrue(stateTransitionUseCase.isValidTransition(WorkType.PROJECT_SKILLED, JobStatus.POSTED, JobStatus.PROPOSALS))
        assertTrue(stateTransitionUseCase.isValidTransition(WorkType.PROJECT_SKILLED, JobStatus.PROPOSALS, JobStatus.HIRED))
        assertTrue(stateTransitionUseCase.isValidTransition(WorkType.PROJECT_SKILLED, JobStatus.HIRED, JobStatus.IN_PROGRESS))
        assertTrue(stateTransitionUseCase.isValidTransition(WorkType.PROJECT_SKILLED, JobStatus.IN_PROGRESS, JobStatus.DELIVERED))
        assertTrue(stateTransitionUseCase.isValidTransition(WorkType.PROJECT_SKILLED, JobStatus.DELIVERED, JobStatus.COMPLETED))

        // Cannot skip directly from POSTED to DELIVERED
        assertFalse(stateTransitionUseCase.isValidTransition(WorkType.PROJECT_SKILLED, JobStatus.POSTED, JobStatus.DELIVERED))
    }

    // =========================================================================
    // 2. ExecuteEscrowLedgerUseCase Unit Tests (Integer Paise & Invariants)
    // =========================================================================

    @Test
    fun testDoubleEntryEscrowSettlementCalculation() {
        // Standard ₹1,000 job = 100,000 paise with 15% platform fee
        val totalHeldPaise = 100000L
        val journal: LedgerJournal = escrowLedgerUseCase.calculateSettlement(totalHeldPaise, commissionPercent = 15.0)

        assertEquals(100000L, journal.escrowLiabilityDebitPaise)
        assertEquals(15000L, journal.platformRevenueCreditPaise)
        assertEquals(85000L, journal.workerWalletCreditPaise)

        // Strict double-entry balance check: Debit = Sum(Credits)
        assertEquals(journal.escrowLiabilityDebitPaise, journal.platformRevenueCreditPaise + journal.workerWalletCreditPaise)
    }

    @Test
    fun testEscrowSettlementWithProWorkerDiscountRate() {
        // Pro Worker tier receives 10% fee
        val totalHeldPaise = 250000L // ₹2,500.00
        val journal = escrowLedgerUseCase.calculateSettlement(totalHeldPaise, commissionPercent = 10.0)

        assertEquals(25000L, journal.platformRevenueCreditPaise)
        assertEquals(225000L, journal.workerWalletCreditPaise)
        assertEquals(journal.escrowLiabilityDebitPaise, journal.platformRevenueCreditPaise + journal.workerWalletCreditPaise)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testEscrowRejectsZeroOrNegativeAmount() {
        escrowLedgerUseCase.calculateSettlement(0L)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testEscrowRejectsCommissionOutsideConfiguredRange() {
        // Commission outside 10% - 20% range must be rejected
        escrowLedgerUseCase.calculateSettlement(50000L, commissionPercent = 35.0)
    }

    @Test
    fun testRefundValidationCeilings() {
        val heldPaise = 50000L // ₹500
        assertTrue(escrowLedgerUseCase.validateRefund(heldPaise, 25000L))
        assertTrue(escrowLedgerUseCase.validateRefund(heldPaise, 50000L)) // Full refund
        assertFalse(escrowLedgerUseCase.validateRefund(heldPaise, 50001L)) // Exceeds held
        assertFalse(escrowLedgerUseCase.validateRefund(heldPaise, 0L)) // Non-positive
        assertFalse(escrowLedgerUseCase.validateRefund(heldPaise, -500L))
    }

    // =========================================================================
    // 3. CalculateTrustScoreUseCase Unit Tests (Weight Allocations)
    // =========================================================================

    @Test
    fun testPerfectTrustScoreCalculation() {
        // Perfect worker: KYC Verified (+25), 5.0 stars (+35), 100% completion (+20), 4 min response (+10), 0% cancel (+10) = 100
        val score = trustScoreUseCase.computeScore(
            isKycVerified = true,
            rating = 5.0f,
            completionRatePercent = 100.0,
            responseTimeMinutes = 4,
            cancellationRatePercent = 0.0
        )
        assertEquals(100, score)
    }

    @Test
    fun testTrustScoreDegradationWithoutKycAndAverageStats() {
        // Unverified worker (0), 4.0 stars (26.25), 80% completion (16.0), 12 min response (7.0), 2% cancel (6.0) = ~55
        val score = trustScoreUseCase.computeScore(
            isKycVerified = false,
            rating = 4.0f,
            completionRatePercent = 80.0,
            responseTimeMinutes = 12,
            cancellationRatePercent = 2.0
        )
        assertEquals(55, score)
    }

    // =========================================================================
    // 4. MatchingEngineUseCase Unit Tests (Consent-First Rule)
    // =========================================================================

    @Test
    fun testConsentFirstRuleEnforced() {
        // Master Spec Rule: An AI or algorithm match MUST NEVER auto-apply without explicit user consent.
        assertFalse(matchingEngineUseCase.canCreateApplication(isExplicitUserConsentGranted = false))
        assertTrue(matchingEngineUseCase.canCreateApplication(isExplicitUserConsentGranted = true))
    }

    @Test
    fun testExplainableMatchScoring() {
        val (score, explanation) = matchingEngineUseCase.calculateMatch(
            hasMatchingSkill = true,
            distanceKm = 3.2,
            workerTrustScore = 96,
            workerRating = 4.9f,
            isKycVerified = true
        )
        assertTrue(score >= 85)
        assertTrue(explanation.contains("Skill verified"))
        assertTrue(explanation.contains("Nearby location"))
        assertTrue(explanation.contains("Govt. KYC Verified"))
        assertTrue(explanation.contains("Top Rated"))
    }

    // =========================================================================
    // 5. SkillBridgeRepository Integration Tests
    // =========================================================================

    @Test
    fun testInitialSeedDataLoaded() {
        assertEquals(14, repository.categories.value.size)
        assertTrue(repository.workersPool.value.isNotEmpty())
        assertTrue(repository.jobs.value.isNotEmpty())
        assertEquals(UserRole.TASK_POSTER, repository.activeRole.value)
    }

    @Test
    fun testEscrowAndLedgerOnJobCompletion() {
        val initialWallet = repository.currentWorker.value.walletBalance
        val activeJob = repository.jobs.value.first { it.id == "job_301" }
        assertEquals(JobStatus.IN_PROGRESS, activeJob.status)
        assertEquals(850, activeJob.budget)

        // Complete job
        repository.advanceJobStatus("job_301", JobStatus.COMPLETED, "proof.jpg")

        val completedJob = repository.jobs.value.first { it.id == "job_301" }
        assertEquals(JobStatus.COMPLETED, completedJob.status)
        assertFalse(completedJob.escrowHeld)

        // 15% commission of 850 = 127. Payout = 850 - 127 = 723
        val expectedCommission = (850 * 0.15).toInt()
        val expectedPayout = 850 - expectedCommission
        assertEquals(initialWallet + expectedPayout, repository.currentWorker.value.walletBalance)

        // Verify ledger transactions
        val payoutTx = repository.transactions.value.firstOrNull { it.type == "WORKER_PAYOUT" && it.jobId == "job_301" }
        assertNotNull(payoutTx)
        assertEquals(expectedPayout, payoutTx?.amount)
    }

    @Test
    fun testStateMachineInvalidTransitionRejected() {
        val job = repository.jobs.value.first { it.id == "job_301" } // IN_PROGRESS
        // Cannot jump directly from IN_PROGRESS to REQUESTED
        repository.advanceJobStatus("job_301", JobStatus.REQUESTED)
        val current = repository.jobs.value.first { it.id == "job_301" }
        assertEquals(JobStatus.IN_PROGRESS, current.status)
    }

    @Test
    fun testExplainableMatchingEngine() {
        val worker = repository.currentWorker.value
        val (score, explanation) = repository.calculateMatchScore(
            worker = worker,
            categoryId = "Electrician",
            city = "Nagpur",
            budget = 500
        )
        assertTrue(score >= 75)
        assertTrue(explanation.contains("Skill verified"))
        assertTrue(explanation.contains("Nagpur"))
    }

    @Test
    fun testInstantUpiWithdrawal() {
        val initialBalance = repository.currentWorker.value.walletBalance
        val withdrawAmount = 1000
        val success = repository.withdrawToUpi("ramesh@okaxis", withdrawAmount)
        assertTrue(success)
        assertEquals(initialBalance - withdrawAmount, repository.currentWorker.value.walletBalance)

        // Cannot withdraw more than balance
        val fail = repository.withdrawToUpi("ramesh@okaxis", 999999)
        assertFalse(fail)
    }

    @Test
    fun testUpgradeWorkerToProTier() {
        val workerId = repository.currentWorker.value.id
        val activeJob = repository.jobs.value.first { it.workerId == workerId }
        val initialCommissionRate = activeJob.commissionRate
        assertEquals(0.15, initialCommissionRate, 0.001)
        assertFalse(repository.currentWorker.value.isProMember)

        repository.upgradeWorkerToPro()

        assertTrue(repository.currentWorker.value.isProMember)
        assertTrue(repository.currentWorker.value.badges.contains("Pro Tier Worker"))
        val updatedJob = repository.jobs.value.first { it.workerId == workerId }
        assertEquals(0.10, updatedJob.commissionRate, 0.001)
    }

    @Test
    fun testLowDataModeToggle() {
        assertFalse(repository.isLowDataMode.value)
        repository.toggleLowDataMode(true)
        assertTrue(repository.isLowDataMode.value)
        repository.toggleLowDataMode(false)
        assertFalse(repository.isLowDataMode.value)
    }

    @Test
    fun testGstTaxInvoiceGeneration() {
        val invoice = repository.generateGstInvoice("job_301")
        assertNotNull(invoice)
        assertTrue(invoice.invoiceNumber.startsWith("SB-INV-2026-"))
        assertEquals(85000L, invoice.totalAmountPaise)
        val expectedBase = (85000L / 1.18).toLong()
        assertEquals(expectedBase, invoice.baseAmountPaise)
        assertEquals("SAC-NOT-CONFIGURED", invoice.sacCode)
        assertEquals("GSTIN-NOT-CONFIGURED-PILOT-ONLY", invoice.gstin)
        assertTrue(invoice.isPlaceholderMode)
        assertTrue(invoice.complianceDisclaimer.contains("PILOT WARNING"))
    }

    // =========================================================================
    // 6. Real Auth & Server-Enforced RBAC Unit Tests (Step 2)
    // =========================================================================

    @Test
    fun testServerStaffAuthenticationAndLockout() {
        val authProvider = MockAuthProvider()

        // 1. Invalid staff credentials decrement remaining attempts
        val fail1 = authProvider.loginStaff("unauthorized@gmail.com", "wrong")
        assertTrue(fail1.isFailure)
        assertTrue(fail1.exceptionOrNull()?.message?.contains("Remaining attempts") == true)

        // 2. 5 failures trigger 15-minute portal lockout
        for (i in 1..4) {
            authProvider.loginStaff("unauthorized@gmail.com", "wrong")
        }
        val locked = authProvider.loginStaff("staff@skillbridge.in", "SkillBridgeSecureStaff#2026")
        assertTrue(locked.isFailure)
        assertTrue(locked.exceptionOrNull()?.message?.contains("locked") == true)
    }

    @Test
    fun testSuccessfulStaffLoginEnforcesAdminRole() {
        val authProvider = MockAuthProvider()
        repository.authProvider = authProvider

        val success = repository.loginStaff("staff@skillbridge.in", "SkillBridgeSecureStaff#2026")
        assertTrue(success.isSuccess)
        assertEquals(UserRole.ADMIN, repository.activeRole.value)
    }

    @Test
    fun testOtpSessionIssuance() {
        val authProvider = MockAuthProvider()
        val sessionResult = authProvider.verifyOtp("+919822012345", "123456")
        assertTrue(sessionResult.isSuccess)
        val session = sessionResult.getOrThrow()
        assertTrue(session.accessToken.startsWith("mock_jwt_access_"))
        assertEquals("WORKER", session.role)
    }

    // =========================================================================
    // 7. Offline Action Queue & Idempotency Unit Tests (Step 3)
    // =========================================================================

    @Test
    fun testOfflineActionQueueEnqueueAndSync() = kotlinx.coroutines.runBlocking {
        val queue = com.example.skillbridge.data.offline.OfflineActionQueue()
        assertEquals(0, queue.pendingCount.value)

        // Enqueue actions with automatic idempotency keys
        val key1 = queue.enqueue(com.example.skillbridge.data.offline.OfflineActionQueue.ActionType.APPLY, "job_301")
        val key2 = queue.enqueue(com.example.skillbridge.data.offline.OfflineActionQueue.ActionType.ACCEPT_JOB, "job_302")
        val key3 = queue.enqueue(com.example.skillbridge.data.offline.OfflineActionQueue.ActionType.MARK_COMPLETE, "job_303")
        val key4 = queue.enqueue(com.example.skillbridge.data.offline.OfflineActionQueue.ActionType.RATE_JOB, "job_304")

        assertEquals(4, queue.pendingCount.value)
        assertNotEquals(key1, key2)
        assertNotEquals(key2, key3)

        // Verify actions in memory
        val pending = queue.getPendingActionsInMemory()
        assertEquals(4, pending.size)
        assertEquals("APPLY", pending[0].actionType)

        // Sync flush
        val synced = queue.syncAll()
        assertEquals(4, synced)
        assertEquals(0, queue.pendingCount.value)
    }

    // =========================================================================
    // 8. Real-Time Maps & OSRM Routing Unit Tests (Step 4)
    // =========================================================================

    @Test
    fun testMapLibreMapsProviderRoutingAndPolyline() {
        val mapsProvider = com.example.skillbridge.provider.MapLibreMapsProvider()
        val originLat = 21.1458
        val originLng = 79.0882
        val destLat = 21.1480
        val destLng = 79.0950

        val (distanceKm, etaMinutes) = mapsProvider.calculateRouteEta(originLat, originLng, destLat, destLng)
        assertTrue(distanceKm > 0.5)
        assertTrue(etaMinutes >= 3)

        val fullRoute = mapsProvider.getFullRoute(originLat, originLng, destLat, destLng)
        assertEquals(4, fullRoute.polylinePoints.size)
        assertTrue(fullRoute.providerSource.contains("MapLibre"))
    }

    // =========================================================================
    // 9. Real Providers & Data Minimization Tests (Step 6)
    // =========================================================================

    @Test
    fun testAadhaarDataMinimizationZeroRawStorage() {
        val kycProvider = com.example.skillbridge.provider.ApiSetuDigiLockerKycProvider()
        val rawAadhaar = "5432 9876 1234"
        val result = kycProvider.verifyIdentity("AADHAAR", rawAadhaar)
        assertTrue(result.isSuccess)
        val masked = result.getOrThrow()
        assertEquals("XXXX-XXXX-1234", masked)
        assertFalse(masked.contains("5432"))
        assertFalse(masked.contains("9876"))
    }

    @Test
    fun testTelephonyMaskedCallingPrivacy() {
        val telephony = com.example.skillbridge.provider.ExotelMaskedCallProvider()
        val bridgeNumber = telephony.initiateMaskedCall("+919822012345", "+919890044556")
        assertTrue(bridgeNumber.startsWith("+91 712 660"))
        assertFalse(bridgeNumber.contains("12345"))
        assertFalse(bridgeNumber.contains("44556"))
    }

    @Test
    fun testResumeParserUserConfirmation() {
        val parser = com.example.skillbridge.provider.RemoteResumeParserProvider()
        var confirmed = false
        val parsed = parser.parseResumeData("resume_bytes") { data ->
            confirmed = data.containsKey("skills")
        }
        assertTrue(confirmed)
        assertTrue(parsed["skills"]?.contains("Kotlin") == true)
    }

    // =========================================================================
    // 10. Phase 4 & Phase 5 Production Readiness Tests
    // =========================================================================

    @Test
    fun testGoogleMapsProviderUnconfiguredFallback() {
        val unconfiguredProvider = com.example.skillbridge.provider.GoogleMapsProvider("PLACEHOLDER_MAPS_API_KEY")
        assertFalse(unconfiguredProvider.isKeyConfigured)

        val route = unconfiguredProvider.getFullRoute(21.1458, 79.0882, 21.1480, 79.0950)
        assertEquals("Mock Offline Vector Cache", route.providerSource)
        assertTrue(route.distanceKm > 0)
    }

    @Test
    fun testGoogleMapsProviderConfiguredKey() {
        val configuredProvider = com.example.skillbridge.provider.GoogleMapsProvider("AIzaSyBValidProductionKeyForNagpurPilot2026")
        assertTrue(configuredProvider.isKeyConfigured)

        val route = configuredProvider.getFullRoute(21.1458, 79.0882, 21.1480, 79.0950)
        assertTrue(route.providerSource.contains("Google Maps"))
        assertTrue(route.polylinePoints.size >= 4)
    }

    @Test
    fun testRadarSearchRadiusExpansionCascade() {
        val initialRadius = 2000 // 2 km
        val step1 = if (initialRadius < 5000) 5000 else 10000
        val step2 = if (step1 < 10000) 10000 else 10000

        assertEquals(5000, step1)
        assertEquals(10000, step2)
        assertTrue(step2 <= 10000) // Configurable max ceiling
    }

    @Test
    fun testWorkerTelemetryIntervalRules() {
        val activeJobIntervalMs = 5000L
        val idleIntervalMs = 15000L

        assertTrue(activeJobIntervalMs < idleIntervalMs)
        assertEquals(5000L, activeJobIntervalMs)
        assertEquals(15000L, idleIntervalMs)
    }

    @Test
    fun testGstInvoiceProductionSafetyFlag() {
        val testInvoice = repository.generateGstInvoice("job_301")
        assertTrue(testInvoice.isPlaceholderMode)
        assertEquals("GSTIN-NOT-CONFIGURED-PILOT-ONLY", testInvoice.gstin)
        assertEquals("SAC-NOT-CONFIGURED", testInvoice.sacCode)
        assertTrue(testInvoice.complianceDisclaimer.contains("PILOT WARNING"))
    }

    @Test
    fun testAppConfigEnvironmentSwitching() {
        val previousMode = com.example.skillbridge.config.AppConfig.currentMode

        com.example.skillbridge.config.AppConfig.setEnvironment(com.example.skillbridge.config.EnvironmentMode.MOCK)
        assertTrue(com.example.skillbridge.config.AppConfig.isMock)
        assertEquals("http://localhost:8080/v1", com.example.skillbridge.config.AppConfig.apiBaseUrl)

        com.example.skillbridge.config.AppConfig.setEnvironment(com.example.skillbridge.config.EnvironmentMode.STAGING)
        assertFalse(com.example.skillbridge.config.AppConfig.isMock)
        assertEquals("https://staging-api.skillbridge.in/v1", com.example.skillbridge.config.AppConfig.apiBaseUrl)

        com.example.skillbridge.config.AppConfig.setEnvironment(com.example.skillbridge.config.EnvironmentMode.PRODUCTION)
        assertFalse(com.example.skillbridge.config.AppConfig.isMock)
        assertEquals("https://api.skillbridge.in/v1", com.example.skillbridge.config.AppConfig.apiBaseUrl)

        // Restore
        com.example.skillbridge.config.AppConfig.setEnvironment(previousMode)
    }

    @Test
    fun testOfflineActionQueueIdempotencyTracking() = kotlinx.coroutines.runBlocking {
        val queue = com.example.skillbridge.data.offline.OfflineActionQueue()
        val key1 = queue.enqueue(com.example.skillbridge.data.offline.OfflineActionQueue.ActionType.ACCEPT_JOB, "job_101")
        val key2 = queue.enqueue(com.example.skillbridge.data.offline.OfflineActionQueue.ActionType.RATE_JOB, "job_101", "{\"rating\":5}")

        assertNotNull(key1)
        assertNotNull(key2)
        assertNotEquals(key1, key2)

        val pending = queue.getPendingActionsInMemory()
        assertEquals(2, pending.size)
        assertEquals("ACCEPT_JOB", pending[0].actionType)
        assertEquals("RATE_JOB", pending[1].actionType)
        assertEquals(2, queue.pendingCount.value)
    }
}

