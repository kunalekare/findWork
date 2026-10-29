# SkillBridge — System & Architecture Audit (WS-0)
**Date:** 2026-10-29  
**Reviewer:** Principal Product Engineer, QA Lead & Security Reviewer  
**Target:** SkillBridge Android Core & Work Marketplace Platform (v2)

---

## 1. Module Map & Codebase Dependency Graph

### Current Android Packages
```
com.example
├── MainActivity.kt                      (App lifecycle, Scaffold, TopBar, BottomNav, Navigation State)
├── skillbridge
│   ├── data
│   │   └── SkillBridgeRepository.kt     (State machines, in-memory collections, dispatch simulation)
│   ├── model
│   │   └── Models.kt                    (User, Job, Category, Proposal, Transaction, Dispute, KycItem, etc.)
│   ├── ui
│   │   ├── components
│   │   │   ├── CommonComponents.kt      (TopBar, BottomNav, JobStatusStepper, IncomingRequestCard, VoiceAssistantDialog, SosDialog, MaskedCallDialog, SkillQuizDialog, IvrSimulatorDialog)
│   │   │   ├── ActionDialogs.kt         (RequestServiceDialog, PostProjectDialog, InAppChatDialog, RatingReviewDialog)
│   │   │   └── OnboardingFlowDialog.kt  (VoiceOnboardingDialog, FormOnboardingDialog)
│   │   ├── screens
│   │   │   ├── TaskPosterHomeScreen.kt  (Category grid, pinned active job, hero banners, book again)
│   │   │   ├── WorkerHomeScreen.kt      (Online/Offline switch, earnings card, 20s incoming card, AI feed)
│   │   │   ├── LiveTrackingScreen.kt    (Simulated Canvas route, radar pulse, status stepper, escrow release)
│   │   │   ├── OrdersListScreen.kt      (Tabs: All, Active, Completed, Proposals)
│   │   │   ├── WalletScreen.kt          (Escrow ledger, instant UPI payout dialog, transaction list)
│   │   │   ├── ProfileScreen.kt         (Skill passport, QR share, trust score formula, learning hub)
│   │   │   └── AdminConsoleScreen.kt    (Live ops KPIs, KYC queue, dispute console, categories)
│   │   └── theme
│   │       ├── Color.kt                 (Brand tokens: #FFFFFF bg, #EA580C primary, #FFF7ED surface-muted)
│   │       ├── Theme.kt                 (SkillBridgeTheme adhering to food-delivery design system)
│   │       └── Type.kt                  (Class-5 accessible typography styles)
│   └── voice
│       └── VoiceAccessibilityManager.kt (Android native TextToSpeech + simulated entity extraction)
└── ui.theme
    ├── Color.kt
    ├── Theme.kt
    └── Type.kt
```

### Dependency Graph
```
[MainActivity] ──> [SkillBridgeRepository] <── [Models]
      │                     │
      ├──> [VoiceAccessibilityManager]
      │
      ├──> [TaskPosterHomeScreen] ──> [CategoryCard, WorkerSpotlightCard]
      ├──> [WorkerHomeScreen] ──> [IncomingRequestCard, WorkerJobCard]
      ├──> [LiveTrackingScreen] ──> [JobStatusStepper, CanvasRoute]
      ├──> [OrdersListScreen]
      ├──> [WalletScreen]
      ├──> [ProfileScreen] ──> [SkillQuizDialog, ShareQrDialog]
      └──> [AdminConsoleScreen]
```

---

## 2. Feature Truth Table

| Feature | Screen / Class | Real or Simulated | What is Simulated | Gap to Production |
|---|---|---|---|---|
| **OTP Login** | `MainActivity` / `ProfileScreen` | Simulated | Fixed demo user profiles; no SMS gateway or OTP verification. | Real SMS OTP gateway (MSG91/Twilio), attempt rate-limiting, JWT access/refresh token pair, device binding. |
| **Voice Onboarding (STT/TTS)** | `VoiceOnboardingDialog`, `VoiceAccessibilityManager` | Partially Real | Uses native Android `TextToSpeech` for playback, but speech recognition & entity extraction are simulated keyword checks. | Remote `/stt` and `/extract-entities` via Whisper/IndicConformer models; dynamic multi-turn slot filling. |
| **Form Onboarding** | `FormOnboardingDialog` | Real UI / Local | Local form state; profile saved only in local repository memory. | Persist to backend database via REST API with photo/cert attachment upload. |
| **Resume Parsing** | `FormOnboardingDialog` | Simulated | Hardcoded extraction on button click. | Server-side PDF parser using Apache Tika / OCR + LLM entity extractor. |
| **Phone IVR Flow** | `IvrSimulatorDialog` | Simulated | On-screen 3x4 keypad simulation with local string prompt progression. | Cloud telephony webhook integration (Exotel / Twilio Voice XML) with DTMF capture and audio stream playback. |
| **Skill Passport & QR** | `ProfileScreen` | Partially Real | Dynamic calculation of trust score, but QR code is a simulated vector graphic. | Signed JWT / verifiable credential payload in QR code with public URL verification endpoint (`skillbridge.in/p/{id}`). |
| **Skill Quiz** | `SkillQuizDialog` | Real | Interactive 3-question MCQ quiz with immediate scoring and badge unlock. | Server-side question pool randomization, cheat detection, and cryptographic badge issuance. |
| **Trust Score Calculation** | `SkillBridgeRepository` | Real (Local) | Implements documented formula: KYC 25%, Ratings 35%, Completion 20%, Response 10%, Cancellation 10%. | Move computation to server-side event pipeline; protect against client-side score manipulation. |
| **Local Request Flow (3 Taps)** | `RequestServiceDialog`, `TaskPosterHomeScreen` | Real UI / Local Backend | Creates Job and locks funds in local state repository. | Backend booking endpoint, geo-clustering query, PostGIS worker lookup, payment gateway auth hold. |
| **Project Flow & Proposals** | `PostProjectDialog`, `OrdersListScreen` | Real UI / Local Backend | Generates project job with milestones and proposals. | Multi-milestone escrow backend, bid bidding deadlines, PDF proposal attachments. |
| **Matching & Dispatch Cascade** | `SkillBridgeRepository` | Simulated (Local) | Selects worker `w_101` and triggers local 20s countdown. | PostGIS spatial radius query (2km expanding to 10km), Redis TTL keys for 20s lock, automated cascade to next candidate. |
| **Live Tracking & ETA** | `LiveTrackingScreen` | Simulated UI | Canvas-drawn mock route and linear progress animation. | MapLibre SDK + OpenStreetMap vector tiles, OSRM routing engine API for turn-by-turn ETA and WebSocket telemetry. |
| **In-App Messaging** | `ActionDialogs.InAppChatDialog` | Real UI / Local Memory | Messages stored in repository memory map. | WebSocket / Firebase Cloud Messaging (FCM) transport with end-to-end TLS, profanity filtering, and contact leakage blocking. |
| **Masked Calling** | `MaskedCallDialog` | Simulated | Shows dialog with virtual number (`+91 712 660 XXXX`). | Telephony provider bridge (Exotel/Twilio voice proxy) bridging employer and worker without revealing numbers. |
| **SOS Emergency** | `SosDialog` | Real UI / Local Broadcast | Shows coordinates dialog and logs broadcast. | Server-side incident dispatch, SMS fallback to trusted contacts, webhook to 112 emergency and safety operations dashboard. |
| **Escrow & Double-Entry Ledger** | `SkillBridgeRepository`, `WalletScreen` | Real In-Memory Logic | Records hold, fee, and payout transactions accurately, but balances are in RAM integers. | Server-side immutable ledger in PostgreSQL stored as integer paise (`BIGINT`), ACID transaction guarantees, idempotency keys. |
| **UPI Payouts** | `WalletScreen` | Simulated | Updates RAM wallet balance and creates mock transaction reference. | RazorpayX / Cashfree Payout API integration with instant IMPS/UPI webhook reconciliation. |
| **Disputes & KYC Admin** | `AdminConsoleScreen` | Real UI / Local Logic | Immediate approve/reject updates local memory. | Role-based authorization (`ADMIN`, `SUPPORT`, `FINANCE`), audit log trail, evidence file storage in S3/GCS. |
| **Localization (i18n)** | `AppLanguage`, UI files | Partially Real | Categories and selected prompts have Hindi/Marathi; hardcoded strings exist in certain dialogs. | Full externalization to `res/values/strings.xml`, `res/values-hi/`, `res/values-mr/` with zero hardcoded strings. |
| **Offline Queue** | Repository | Missing | No persistent local database queue for offline retries. | Room database with WorkManager synchronization for reliable action replay on reconnect. |

---

## 3. Spec Coverage Gaps Against Master Product Spec

1. **Application Tracker (`Applied → Shortlisted → Interview → Hired → Rejected`):** Missing formal stepper UI for project applicants.
2. **Side-by-side Proposal Comparison:** Proposals are listed linearly; employer cannot view a side-by-side comparison table of ratings, price, and past jobs.
3. **Milestone Escrow UI & Release:** Milestone data structure exists, but UI needs individual milestone approval and release actions.
4. **Favourites & "Book Again":** Spotlight exists, but explicit "Add to Favourites" toggle and quick re-booking flow need wiring.
5. **GST-Ready Invoices:** Invoices are mentioned in text; printable/exportable tax breakdown invoice view is missing.
6. **Business Accounts with Team Roles:** Employer profile only supports individual mode; multi-user team roles (`ADMIN`, `DISPATCHER`, `FINANCE`) are absent.
7. **Report & Block:** Safety features currently lack explicit "Report User" and "Block from Dispatch" modal workflows.
8. **Trusted-Contact Live-Location Sharing:** Location broadcast is present in SOS, but passive sharing with a designated family member contact is missing.
9. **Availability Calendar & Portfolio Gallery:** Worker profile shows skill tags but lacks a work schedule picker and photo portfolio viewer.
10. **Learning Hub:** Micro-courses are displayed in a basic list without interactive content or quiz-linked completion certificates.
11. **Worker Pro Subscription & Featured Listings:** Reduced platform fee tier (e.g. 10% vs 15%) and highlighted badges are missing.
12. **Content Moderation:** In-app chat allows raw text; regex/filter for phone numbers and profanity is not enforced.
13. **Low-Data Mode:** Mode toggle to disable radar animations and map rendering is not exposed in preferences.
14. **RBAC Isolation:** Admin console was accessible via consumer top-bar toggle instead of restricted staff authentication.

---

## 4. Risk Register

| Risk ID | Category | Description | Severity | Fix Owner | Remediation Plan |
|---|---|---|---|---|---|
| **R-01** | Security | Admin console exposed as a client UI toggle. Any user can approve KYC or resolve disputes. | **HIGH** | WS-1 (Backend) | Enforce server-side JWT RBAC (`ADMIN`, `SUPPORT`, `FINANCE`). Replace consumer UI toggle with separate staff login gate. |
| **R-02** | Financial Integrity | Floating point numbers or unvalidated client ledger updates could cause balance drift or negative balances. | **HIGH** | WS-3 (Ledger) | Store all monetary values as integer paise (`BIGINT`). Enforce double-entry accounting where `Debit = Credit` and balance $\ge 0$. |
| **R-03** | Privacy | Raw phone numbers could be exposed or exchanged in in-app chat. | **HIGH** | WS-4 (Safety) | Telephony masking via virtual numbers; regex pattern masking on chat messages blocking 10-digit numbers and external links. |
| **R-04** | Privacy | Aadhaar/Identity data mishandling under DPDP Act 2023. | **HIGH** | WS-4 (KYC) | Never store raw Aadhaar numbers or scans locally. Store only masked reference (`XXXX-XXXX-1234`), verification timestamp, and consent log. |
| **R-05** | Reliability | Race conditions during 20-second worker dispatch cascade (two workers accepting the same job). | **HIGH** | WS-2 (Dispatch) | Server-side Redis distributed lock (`SET job:dispatch:{id} worker_id NX PX 20000`) ensuring exactly-once assignment. |
| **R-06** | Financial Integrity | Double payouts or duplicate escrow release on network retry. | **HIGH** | WS-3 (Ledger) | Require unique client-generated `Idempotency-Key` on all payment, hold, and payout mutations. |
| **R-07** | Compliance | Missing purpose-specific consent logs for DPDP Act 2023. | **MEDIUM** | WS-4 (Privacy) | Implement explicit consent capture modal before profile creation and KYC submission; generate privacy documentation. |
| **R-08** | Reliability | App crashing or losing state on low-memory devices (2 GB RAM) or network drops. | **MEDIUM** | WS-7 (Resilience) | Room database persistence for offline caching; low-data mode toggle; graceful degrade when speech services are unavailable. |

---

## 5. Defects Identified in Current Prototype Code

1. **Missing Localization Files:** Strings were partly hardcoded in UI composables instead of being fully resolved through `res/values/strings.xml`, `values-hi/`, and `values-mr/`.
2. **Missing Provider Abstractions:** Services (Speech, Auth, Payment, KYC, Telephony, Maps, Push) were directly coupled to simulated repository code rather than clean provider interfaces with swappable Mock/Real implementations.
3. **Admin Toggle in Consumer TopBar:** The top-bar role selector allowed consumers to switch straight into "Admin" without credential verification.
4. **Client as Source of Truth:** Job state transitions and escrow releases were executed purely in RAM without validation against an invariant engine or idempotency checks.
5. **Missing TalkBack & Touch Target Semantics:** Several icons lacked explicit content descriptions and certain interactive pills were under the 48dp touch target standard.
