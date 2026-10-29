# SkillBridge — System Threat Model (STRIDE Methodology)

**System:** SkillBridge Two-Sided Marketplace & Real-Time Dispatch  
**Target:** Nagpur Controlled Pilot Launch  
**Methodology:** Microsoft STRIDE (Spoofing, Tampering, Repudiation, Information Disclosure, Denial of Service, Elevation of Privilege)  
**Date:** October 2026

---

## 1. Threat Analysis Matrix

| # | Threat Scenario | STRIDE Category | Risk Level | Attack Vector & Impact | Technical Mitigation in Codebase |
|---|---|---|---|---|---|
| **T-1** | **Worker GPS Telemetry Spoofing** | Spoofing / Tampering | **Medium** | Worker uses mock location apps to simulate fake proximity to high-demand Nagpur clusters to capture jobs. | 1. Android FusedLocationProviderClient checks `isMockLocation` flag.<br>2. Backend `GoogleRoutesService` verifies speed plausibility (rejects speed > 100 km/h for city transit).<br>3. 60-second telemetry staleness threshold rejects outdated coordinates. |
| **T-2** | **Escrow Double-Spend / Front-Running** | Tampering / Repudiation | **Critical** | Malicious consumer attempts simultaneous cancellation while worker marks completion, or replays payment webhook. | 1. Database transactions enforce strict state machine transitions (`JobStateMachine`).<br>2. Double-entry ledger with integer paise balancing (`LedgerService`).<br>3. Webhook idempotency table deduplicates `eventId` using Redis/SQL keys. |
| **T-3** | **Aadhaar PII & Identity Leakage** | Information Disclosure | **Critical** | Interception or leakage of worker government identification numbers violating Digital Personal Data Protection Act 2023. | 1. Zero raw Aadhaar storage rule: only last 4 digits stored (`XXXX-XXXX-1234`).<br>2. Verification delegated entirely to DigiLocker / API Setu OAuth flow.<br>3. No raw national identity numbers logged in application logs. |
| **T-4** | **SMS / OTP Exhaustion & Toll Fraud** | Denial of Service | **Medium** | Botnet triggers thousands of SMS OTP requests to drain Twilio/Exotel balance. | 1. Max 1 OTP generation per phone per 60 seconds.<br>2. Max 3 verification attempts per OTP with 300-second expiration.<br>3. IP and phone rate-limiting enforced in `AuthService.java`. |
| **T-5** | **IDOR on Phone Numbers / Direct Calling** | Information Disclosure | **High** | Attacker queries job metadata to obtain personal phone numbers of female consumers or vulnerable workers. | 1. Direct phone numbers never returned in public API payloads.<br>2. Voice telephony routed exclusively via masked virtual bridge numbers (Exotel).<br>3. In-app chat channels gated by active job participation tokens. |
| **T-6** | **Concurrent Worker Accept Race Condition** | Tampering / Concurrency | **High** | Two workers simultaneously click "Accept" on a broadcasted emergency job, causing duplicate dispatch. | 1. Redis single-worker offer lock with 20s TTL (`DispatchService`).<br>2. Atomic Compare-And-Swap (CAS) SQL update: `UPDATE jobs SET status = 'MATCHED', worker_id = ? WHERE id = ? AND status = 'REQUESTED'`.<br>3. Winner receives job; loser receives HTTP 409 Conflict. |
| **T-7** | **Staff Account Privilege Escalation** | Elevation of Privilege | **High** | Attacker attempts credential brute forcing against staff admin endpoint to access escrow dispute releases. | 1. Dedicated staff credentials separate from consumer DB.<br>2. 5-attempt threshold triggers automatic 15-minute lockout.<br>3. All administrative overrides create immutable audit logs in `audit_logs` table. |

---

## 2. Security Boundaries & Trust Zones

```
[ Untrusted Public Internet ]
           │
           ▼
[ Cloudflare / Caddy TLS 1.3 Proxy ] (DDoS mitigation, rate-limiting)
           │
           ▼
[ Spring Boot 3 Backend API ] (JWT Validation, RBAC, Input Sanitization)
     │                     │
     ▼                     ▼
[ PostgreSQL + PostGIS ]  [ Redis 7 Cluster ]
(ACID Ledger, Encrypted)  (AOF Persistence, Atomic Locks)
```

---

## 3. Residual Risk Assessment for Pilot Launch

The residual risk across all 7 threat vectors is evaluated as **ACCEPTABLE FOR PILOT (Nagpur)** provided:
1. Pilot enrollment is gated behind invitation codes.
2. Escrow holding per single job is capped at ₹5,000.
3. Daily platform volume limit of 50 active transactions.
