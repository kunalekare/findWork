# SkillBridge — Target Production Architecture (WS-0)
**Document Version:** 2.0.0  
**Author:** Principal Product Engineer & Security Architect

---

## 1. High-Level System Architecture Diagram (Mermaid)

```mermaid
graph TB
    subgraph ClientLayer ["SkillBridge Android Mobile App (Clean Architecture)"]
        UI["Presentation Layer<br/>(Jetpack Compose + Material 3 Tokens)"]
        VM["ViewModels<br/>(Immutable StateFlow + Single Directional Data Flow)"]
        Domain["Domain Layer (Pure Kotlin)<br/>UseCases: EscrowInvariants, StateMachines, Matching, TrustScore"]
        DataRepo["Data Layer (Repository Pattern)<br/>Offline Action Queue + Room Cache"]
        
        subgraph Providers ["Provider Interfaces (Swappable Mock / Real)"]
            P_Auth["AuthProvider<br/>(Phone OTP + JWT)"]
            P_Pay["PaymentProvider<br/>(Razorpay / UPI Escrow)"]
            P_Speech["SpeechProvider<br/>(Android TTS / AI STT)"]
            P_Kyc["KycProvider<br/>(DigiLocker / Aadhaar Masked)"]
            P_Map["MapsProvider<br/>(MapLibre + OSRM)"]
            P_Tel["TelephonyProvider<br/>(Exotel Virtual Masked Calls)"]
            P_Push["PushProvider<br/>(Firebase Cloud Messaging)"]
            P_Resume["ResumeParserProvider<br/>(PDF Entity Extractor)"]
        end
    end

    subgraph BackendAPI ["Spring Boot 3 / Java 21 Enterprise Gateway"]
        SecurityFilter["Security & RBAC Filter<br/>(JWT Claims: WORKER, TASK_POSTER, ADMIN, FINANCE)"]
        DispatchService["Dispatch & Cascade Engine<br/>(20s TTL Cascade + Fairness Rotation)"]
        EscrowService["Escrow & Double-Entry Ledger<br/>(Integer Paise, ACID Transactions, Idempotency)"]
        StateMachineService["Server State Machines<br/>(Audit Log + Event Notifications)"]
        WSGateway["WebSocket Gateway<br/>(Live Location & Status Broadcast)"]
    end

    subgraph AIService ["FastAPI AI Microservice (Python)"]
        AI_STT["/v1/stt (Whisper / IndicConformer)"]
        AI_TTS["/v1/tts (Indic Multi-Speaker TTS)"]
        AI_Extract["/v1/extract-entities (Slot Filling)"]
        AI_Match["/v1/match & /explain (Semantic Embeddings)"]
    end

    subgraph DataStorage ["Persistent Data & Cache Tier"]
        Postgres[("PostgreSQL 16 + PostGIS<br/>Spatial Worker Index & Spatial Radius")]
        RedisCache[("Redis 7 Cluster<br/>Dispatch Locks, Rate-Limiting, Live Worker Geohash")]
    end

    UI --> VM
    VM --> Domain
    Domain --> DataRepo
    DataRepo --> Providers
    Providers -->|REST / TLS 1.3| SecurityFilter
    Providers -->|WebSocket WSS| WSGateway
    
    SecurityFilter --> DispatchService
    SecurityFilter --> EscrowService
    SecurityFilter --> StateMachineService
    
    DispatchService -->|Geo Radius Query| Postgres
    DispatchService -->|20s Worker Lock| RedisCache
    EscrowService -->|Double-Entry Journal| Postgres
    StateMachineService -->|Audit Trail| Postgres
    
    DispatchService -.->|Semantic Rank| AI_Match
    SecurityFilter -.->|Voice Intent| AI_Extract
```

---

## 2. Data Flow Diagram: "Request Now" (On-Demand Local Dispatch)

```mermaid
sequenceDiagram
    autonumber
    actor Employer as Task-Poster
    participant App as Android Client
    participant API as Spring Boot API
    participant Redis as Redis (Dispatch Lock)
    participant PostGIS as PostgreSQL + PostGIS
    actor Worker as Worker Candidate

    Employer->>App: Tap "Request Electrician Now" (Category, Urgency, Address)
    App->>API: POST /v1/jobs/request-local (Idempotency-Key, Escrow Deposit in Paise)
    API->>API: Hold Escrow Amount in Double-Entry Ledger (HOLD)
    API->>PostGIS: ST_DWithin(worker_location, request_location, 2000m)
    PostGIS-->>API: List of eligible online & KYC-verified workers
    API->>API: Rank candidates (Distance + Trust Score + Rating + Fairness Rotation)
    
    loop Dispatch Cascade (20s Window)
        API->>Redis: SET job:dispatch:{jobId} {workerId} NX EX 20
        API->>Worker: Push Notification & WebSocket Event: Incoming Request (20s timer)
        Worker-->>App: Displays Incoming Request Card with countdown ring
        alt Worker Accepts Within 20s
            Worker->>API: POST /v1/jobs/{id}/accept
            API->>API: Transition State: REQUESTED -> MATCHED
            API->>App: Notify Employer: Worker Assigned (ETA, Name, Masked Phone)
        else 20s Timeout Expired
            Redis-->>API: Lock Expired
            API->>API: Expand Radius to 5km and dispatch to next ranked worker
        end
    end
```

---

## 3. Data Flow Diagram: "Escrow Release" & Financial Settlement

```mermaid
sequenceDiagram
    autonumber
    actor Worker as Worker
    actor Employer as Task-Poster
    participant App as Android Client
    participant API as Spring Boot API
    participant Ledger as Postgres Double-Entry Ledger
    participant Payout as Payment Gateway (RazorpayX)

    Worker->>App: Upload Completion Photo Proof & Tap "Finish Work"
    App->>API: POST /v1/jobs/{id}/deliver (Photo URL)
    API->>API: Transition State: IN_PROGRESS -> DELIVERED (Audit Logged)
    API->>Employer: Notification: Work Completed, Please Inspect & Release
    
    Employer->>App: Confirm Satisfaction & Submit Rating (5 Stars)
    App->>API: POST /v1/jobs/{id}/release-escrow (Idempotency-Key, Rating)
    
    critical Atomic Double-Entry Settlement
        API->>Ledger: DEBIT Escrow_Liability_Account (₹850.00 / 85000 paise)
        API->>Ledger: CREDIT Platform_Commission_Revenue (15% = ₹127.50 / 12750 paise)
        API->>Ledger: CREDIT Worker_Wallet_Payable (85% = ₹722.50 / 72250 paise)
    end

    API->>API: Transition State: DELIVERED -> COMPLETED
    API->>Payout: Trigger Instant UPI Payout to Worker VPA (if auto-withdrawal enabled)
    API->>Worker: WebSocket Push: "₹722.50 Credited to Wallet!"
    API->>Employer: PDF GST Invoice Generated & Download URL Issued
```

---

## 4. Database Schema (Entity Relationship Diagram)

```mermaid
erDiagram
    USERS ||--o{ JOBS : "posts or executes"
    USERS ||--o{ PROPOSALS : "submits"
    USERS ||--o{ REVIEWS : "gives or receives"
    USERS ||--o{ KYC_RECORDS : "submits"
    USERS ||--o{ LEDGER_ENTRIES : "has wallet balance"
    CATEGORIES ||--o{ JOBS : "categorizes"
    JOBS ||--o{ PROPOSALS : "receives"
    JOBS ||--o{ MILESTONES : "contains"
    JOBS ||--o{ LEDGER_ENTRIES : "triggers"
    JOBS ||--o{ DISPUTES : "may have"
    JOBS ||--o{ AUDIT_LOGS : "tracks lifecycle"

    USERS {
        uuid id PK
        varchar phone UK
        varchar full_name
        varchar role "WORKER, TASK_POSTER, ADMIN, FINANCE"
        int trust_score "0-100"
        numeric rating "1.00 to 5.00"
        int jobs_completed
        boolean is_kyc_verified
        bigint wallet_balance_paise
        geometry location_coords
        varchar preferred_language "en, hi, mr"
        timestamp created_at
    }

    CATEGORIES {
        varchar id PK
        varchar name_en
        varchar name_hi
        varchar name_mr
        varchar work_type "LOCAL_ON_DEMAND, PROJECT_SKILLED"
        bigint base_price_paise
        numeric commission_percentage "10.0 to 20.0"
        boolean is_surge_active
    }

    JOBS {
        uuid id PK
        varchar title
        varchar category_id FK
        uuid task_poster_id FK
        uuid worker_id FK
        varchar status "REQUESTED, MATCHED, EN_ROUTE, ARRIVED, IN_PROGRESS, COMPLETED, DISPUTED, CANCELLED"
        bigint budget_paise
        bigint escrow_held_paise
        varchar urgency
        geometry job_location
        varchar address_text
        text completion_photo_url
        timestamp created_at
    }

    MILESTONES {
        uuid id PK
        uuid job_id FK
        varchar title
        bigint amount_paise
        varchar status "PENDING, FUNDED, DELIVERED, RELEASED"
        timestamp completed_at
    }

    LEDGER_ENTRIES {
        uuid id PK
        uuid job_id FK
        varchar idempotency_key UK
        varchar account_type "ESCROW, REVENUE, WORKER_WALLET, TAX_GST"
        varchar entry_type "DEBIT, CREDIT"
        bigint amount_paise
        text description
        timestamp recorded_at
    }

    KYC_RECORDS {
        uuid id PK
        uuid user_id FK
        varchar doc_type "AADHAAR_MASKED, TRADE_LICENSE, DEGREE"
        varchar masked_reference
        varchar status "PENDING, VERIFIED, REJECTED"
        timestamp verified_at
        text verification_provider_id
    }

    DISPUTES {
        uuid id PK
        uuid job_id FK
        uuid raised_by_id FK
        text reason
        varchar status "OPEN, UNDER_INVESTIGATION, RESOLVED"
        bigint contested_amount_paise
        text resolution_notes
        timestamp created_at
    }

    AUDIT_LOGS {
        uuid id PK
        varchar entity_type
        uuid entity_id
        varchar action
        varchar performed_by
        jsonb metadata
        timestamp created_at
    }
```

---

## 5. Architectural Boundaries & Android Clean Layers

1. **Presentation Layer (`com.example.skillbridge.ui`):**
   - Pure UI declaration using Jetpack Compose with strict adherence to design system tokens.
   - All state exposed via `StateFlow` from ViewModels.
   - Zero business or pricing logic inside composables.
   
2. **Domain Layer (`com.example.skillbridge.domain`):**
   - Pure Kotlin (zero `android.*` imports).
   - Contains use-cases: `CalculateTrustScoreUseCase`, `ValidateStateTransitionUseCase`, `ExecuteEscrowLedgerUseCase`, `RankCandidateWorkersUseCase`.
   - Highest test coverage with automated unit tests for state machines and financial invariants.

3. **Data Layer (`com.example.skillbridge.data`):**
   - Implements repositories coordinating local caching (Room) and remote API communication.
   - Provider interface pattern allows switching between `Mock*` (for standalone development/testing) and `Remote*` / `Production*` implementations via configuration.

4. **Security & Invariant Rules:**
   - Client is strictly a presentation and input terminal.
   - Server holds exclusive authority over:
     1. Money balances, fees, and payouts.
     2. Job state transitions and dispute resolution.
     3. Trust scores and KYC validation.
     4. Dispatch algorithm and worker assignment.
