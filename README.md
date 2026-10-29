# SkillBridge — Work Marketplace for India (v2)

Production-grade, two-sided, real-time work marketplace connecting Workers/Providers and Task-Posters/Employers across India with voice accessibility, double-entry escrow, live dispatch tracking, and verified Skill Passports.

---

## 1. Target Architecture & Component Topology

```
┌────────────────────────────────────────────────────────────────────────┐
│                        SkillBridge Mobile App                         │
│   (Food-Delivery Style: White #FFFFFF + Single Accent Orange #EA580C)   │
└────────────────────────────────────────────────────────────────────────┘
          │                                           │
  [Task-Poster Mode]                          [Worker Mode]
  - 3-Tap Service Request                     - Online/Offline Toggle
  - Post Skilled Project (AI desc)            - 20s Incoming Countdown Ring
  - Live Radar & OSRM Map Tracking            - AI Opportunity Feed (Explainable)
  - Escrow Release & 5-Star Reviews           - Consent-First Applications
  - Masked Calling & In-App Chat              - Instant UPI Wallet Payout
          │                                           │
          └─────────────────────┬─────────────────────┘
                                │
               [Unified State & Event Repository]
                                │
    ┌───────────────────────────┼───────────────────────────┐
    │                           │                           │
┌──────────────┐      ┌──────────────────┐       ┌────────────────────┐
│ Voice Engine │      │  State Machines  │       │   Escrow Ledger    │
│ - TTS (Hi/Mr)│      │ - Local Cascade  │       │ - 15% Platform Fee │
│ - Waveforms  │      │ - Project Stages │       │ - Double-Entry Tx  │
│ - Entities   │      │ - Audit Logging  │       │ - Instant UPI Payout│
└──────────────┘      └──────────────────┘       └────────────────────┘
```

---

## 2. Database Schema (ERD & State Models)

### Core Tables & Models
1. **`users`**: `id`, `name`, `phone`, `role` (WORKER, TASK_POSTER, ADMIN), `avatar`, `trust_score` (0-100), `rating`, `jobs_completed`, `kyc_status`, `wallet_balance`, `language` (EN, HI, MR).
2. **`categories`**: `id`, `name`, `name_hi`, `name_mr`, `type` (LOCAL_ON_DEMAND vs PROJECT_SKILLED), `base_price`, `is_hotspot`.
3. **`jobs`**: `id`, `category_id`, `task_poster_id`, `worker_id`, `type`, `status`, `budget`, `escrow_held`, `commission_rate` (0.15), `location_city`, `location_address`, `urgency`, `eta_minutes`, `distance_km`, `completion_proof_photo`.
4. **`proposals`**: `id`, `job_id`, `worker_id`, `quoted_price`, `cover_note`, `days_to_deliver`, `status`.
5. **`transactions` (Double-entry Ledger)**: `id`, `job_id`, `amount`, `type` (`ESCROW_HOLD`, `ESCROW_RELEASE`, `COMMISSION`, `WORKER_PAYOUT`), `is_credit`, `upi_reference`, `timestamp`.
6. **`disputes`**: `id`, `job_id`, `raised_by_role`, `reason`, `status` (`OPEN`, `RESOLVED`), `escrow_amount`.
7. **`kyc_records`**: `id`, `user_id`, `document_type`, `doc_number_masked`, `status` (`PENDING`, `APPROVED`, `REJECTED`).
8. **`chat_messages`**: `id`, `job_id`, `sender_name`, `is_from_worker`, `message`, `timestamp`.

### State Machine Lifecycle
- **Local Service**: `REQUESTED` ➔ `MATCHED` ➔ `EN_ROUTE` ➔ `ARRIVED` ➔ `IN_PROGRESS` ➔ `COMPLETED` (or `DISPUTED` / `CANCELLED`).
- **Skilled Project**: `POSTED` ➔ `PROPOSALS` ➔ `HIRED` ➔ `IN_PROGRESS` ➔ `DELIVERED` ➔ `COMPLETED`.

---

## 3. Phase 3 Final Status Table (Real Backend, Auth, Data Layer, Dispatch & Payments)

Per Phase 3 specifications, features are strictly classified as **Implemented**, **Partial**, or **Mocked** (naming the exact provider or fallback class).

| Feature Area | Feature | Phase 3 Status | Component / Provider Class | Verification & Implementation Details |
|---|---|---|---|---|
| **Design System** | Food-delivery UI, White `#FFFFFF`, Orange `#EA580C` | **Implemented** | `Theme.kt`, `Color.kt` | 8-px spatial grid, WCAG AA contrast, edge-to-edge support |
| **RBAC & Staff Gate** | Consumer Mode Switch + Server Staff Auth | **Implemented** | `StaffLoginDialog.kt`, `AuthService.java` | Zero hard-coded secrets; server checks, rate-limiting (5 attempts), 15m lockout, audit logging |
| **Backend & DB** | Spring Boot 3 + PostgreSQL/PostGIS + Redis | **Implemented** | `backend/`, `docker-compose.yml` | 18 Flyway tables, seed data (14 categories, 50 workers, 20 posters, 30 jobs), OpenAPI 3.0 spec |
| **Logic Engines** | Shared Math Engines (Trust, Escrow, Matching) | **Implemented** | `backend/domain/engine/`, `SharedTestVectorsTest` | 100% vector parity between Kotlin client and Java backend |
| **Auth & Keystore** | JWT Access & Refresh Tokens + Encrypted Storage | **Implemented** | `TokenStorage.kt`, `RemoteAuthProvider.kt`, `JwtService.java` | AES-256 GCM in Android Keystore; 6-digit phone OTP with 5m expiry |
| **Data Layer** | Room Cache + Retrofit + Offline Queue | **Implemented** | `SkillBridgeDatabase.kt`, `OfflineActionQueue.kt` | Room entities, Retrofit service, offline queue for `APPLY`, `ACCEPT_JOB`, `MARK_COMPLETE`, `RATE_JOB` with UUID idempotency |
| **Real-Time Dispatch** | Expanding PostGIS Search + Redis Cascade | **Implemented** | `DispatchService.java`, `JobStateMachineService.java` | 2km ➔ 5km radius expansion, 20s Redis TTL cascade, forward-only state progression |
| **Telemetry & Maps** | SSE Gateway & MapLibre / OpenStreetMap | **Implemented** | `LiveTrackingEventGateway.java`, `MapLibreMapsProvider.kt` | SSE tracking stream, OSRM routing engine, deterministic `MockMapsProvider` offline fallback |
| **Payments & Escrow**| Razorpay TEST Mode + Double-Entry Ledger | **Implemented** | `RazorpayService.java`, `LedgerService.java` | HMAC-SHA256 signed webhooks, idempotent deduplication, balanced integer paise ledger, dispute freeze |
| **Tax Invoicing** | GST-Ready Tax Invoice (SAC `998713`) | **Implemented** | `GstInvoiceService.java`, `GstInvoiceDialog.kt` | 18% GST calculation, explicit test-mode placeholder disclaimer for SAC & GSTIN |
| **Speech & Voice** | Indic Speech Pipeline (ASR + TTS) | **Partial** | `BhashiniSpeechProvider.kt`, `MockSpeechProvider.kt` | Bhashini REST client interface with native Android TTS fallback |
| **Resume Parser** | Document Parsing & User Confirmation | **Implemented** | `RemoteResumeParserProvider.kt`, `ActionDialogs.kt` | Multipart upload with mandatory pre-save user confirmation hook |
| **KYC & DPDP 2023** | DigiLocker eKYC & Zero-Raw Aadhaar Rule | **Implemented** | `ApiSetuDigiLockerKycProvider.kt`, `V1__initial_schema.sql` | Strictly masks Aadhaar (`XXXX-XXXX-1234`), right to erasure dialog |
| **Telephony & IVR** | Masked Calling & Telephony Webhook | **Implemented** | `ExotelMaskedCallProvider.kt`, `IvrWebhookController.java` | Exotel bridge call trigger, IVR webhook matching `docs/IVR_FLOW.md` |
| **Push Alerts** | FCM Cloud Messaging & Worker Alerts | **Implemented** | `FcmPushProvider.kt` | Firebase Cloud Messaging push dispatch for jobs, payments, and safety |
| **Support & Tickets**| 24/7 Help Center & Dispute Escalation | **Implemented** | `HelpCenterDialog.kt`, `ProfileScreen.kt` | In-app ticket submission and live dispute tracking |
| **Refer & Earn** | Artisan & Client Referral Program | **Implemented** | `ReferralProgramDialog.kt`, `ProfileScreen.kt` | Unique referral code generator (`SKILL<id>`) with ₹200 bonus flow |
| **Admin Operations** | Live Geographic Demand Heatmap & Broadcast | **Implemented** | `AdminConsoleScreen.kt` | Real-time Bangalore/Mumbai/Delhi demand density & push broadcast |
| **Safety & Trust** | Report/Block & Family Contact Sharing | **Implemented** | `ReportBlockUserDialog.kt`, `LiveTrackingScreen.kt` | Instant user blocking, 1-tap live emergency SMS location sharing |
| **Localization** | English, Hindi (हिन्दी), Marathi (मराठी) | **Implemented** | `res/values`, `values-hi`, `values-mr` | Native script support across all screens and system messages |

---

## 4. Documentation Index

The following engineering artifacts are maintained in `/docs`:
- `docs/AUDIT.md`: Complete module map, feature truth table, spec coverage gaps, and risk register.
- `docs/ARCHITECTURE.md`: Mermaid target architecture diagram, Request Now sequence, Escrow release sequence, and ERD.
- `docs/PRIVACY.md`: DPDP Act 2023 compliance, data minimization rules, consent logging, and right to erasure.
- `docs/API.md`: OpenAPI 3.0 uniform JSON envelope, correlation IDs, and idempotent endpoint definitions.
- `docs/IVR_FLOW.md`: 3x4 dialer tree, voice XML webhook payloads, and SMS/WhatsApp PIN dispatch.
- `docs/RUNBOOK.md`: Docker compose local stack, Flyway migrations, health checks, and disaster recovery.

