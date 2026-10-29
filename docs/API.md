# SkillBridge — REST API Specification (OpenAPI 3.0 / v1)
**Base URL:** `https://api.skillbridge.in/v1`  
**Protocol:** HTTPS / TLS 1.3  
**Content-Type:** `application/json`

---

## 1. Uniform Response & Error Envelope

All API endpoints return a standardized envelope with a unique `correlationId` (UUID v4) for distributed tracing:

### Standard Success Envelope
```json
{
  "success": true,
  "correlationId": "c8b4112e-9d22-41e9-9184-e42bc511b849",
  "data": { ... },
  "meta": {
    "page": 1,
    "pageSize": 20,
    "totalCount": 85
  }
}
```

### Uniform Error Envelope
```json
{
  "success": false,
  "correlationId": "c8b4112e-9d22-41e9-9184-e42bc511b849",
  "error": {
    "code": "INVALID_STATE_TRANSITION",
    "message": "Cannot transition job status from IN_PROGRESS directly to REQUESTED",
    "details": [
      {
        "field": "targetStatus",
        "issue": "Valid next statuses are COMPLETED or DISPUTED"
      }
    ],
    "timestamp": "2026-10-29T06:40:00Z"
  }
}
```

---

## 2. Core Endpoints Catalogue

### 2.1 Authentication & Profile
- `POST /v1/auth/otp/send`
  - **Body:** `{ "phone": "+919822012345", "deviceId": "dev_xyz" }`
  - **Description:** Sends rate-limited 6-digit OTP (3 attempts max, 5-minute expiry).
- `POST /v1/auth/otp/verify`
  - **Body:** `{ "phone": "+919822012345", "otp": "491203" }`
  - **Response:** `{ "accessToken": "jwt_...", "refreshToken": "jwt_...", "user": { ... } }`
- `GET /v1/users/me`
  - Returns authenticated user profile, trust score, and role permissions.

### 2.2 Marketplace & Dispatch
- `GET /v1/categories`
  - Returns 14 blue-collar & white-collar categories with localized names (`name_hi`, `name_mr`), base rates, and surge multipliers.
- `POST /v1/jobs/request-local`
  - **Headers:** `Idempotency-Key: <UUID>`
  - **Body:**
    ```json
    {
      "categoryId": "cat_elec",
      "problemDescription": "MCB switch tripping on geyser load",
      "urgency": "IMMEDIATE",
      "address": "Plot 42, West High Court Rd, Dharampeth",
      "city": "Nagpur",
      "budgetPaise": 85000
    }
    ```
  - **Action:** Deposits ₹850.00 into platform escrow, searches 2km radius via PostGIS, and initiates 20-second cascade to top-ranked worker.
- `POST /v1/jobs/{id}/accept`
  - **Worker Action:** Claims candidate dispatch lock in Redis within 20-second window.
- `POST /v1/jobs/{id}/advance-status`
  - **Body:** `{ "targetStatus": "ARRIVED", "photoProofUrl": null }`
  - **Validation:** Enforces server state machine invariants; logs to `audit_log`.

### 2.3 Payments & Escrow Ledger
- `POST /v1/jobs/{id}/release-escrow`
  - **Headers:** `Idempotency-Key: <UUID>`
  - **Body:** `{ "rating": 5.0, "comment": "Excellent, safe work!" }`
  - **Action:** Atomically settles journal: debits Escrow Liability, credits 15% to Platform Revenue, and credits 85% to Worker Wallet. Issues GST invoice PDF.
- `POST /v1/wallet/withdraw-upi`
  - **Headers:** `Idempotency-Key: <UUID>`
  - **Body:** `{ "vpa": "ramesh@okaxis", "amountPaise": 150000 }`
  - **Action:** Triggers instant payout via payment provider; rejects if requested amount exceeds available ledger balance.

### 2.4 Safety & SOS
- `POST /v1/safety/sos-broadcast`
  - **Body:** `{ "jobId": "job_301", "latitude": 21.1458, "longitude": 79.0882, "reason": "EMERGENCY_ASSISTANCE" }`
  - **Action:** Alerts 112 emergency relay, sends automated SMS with live GPS link to trusted contacts, and flags active job for emergency response team.

### 2.5 AI & Voice Microservices
- `POST /v1/ai/extract-entities`
  - Extracts `{ "skill", "location", "urgency", "budget" }` from voice transcript.
- `POST /v1/ai/explain-match`
  - Generates transparent reason in user's preferred language (Hindi, Marathi, or English).
