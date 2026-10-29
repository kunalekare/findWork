# SkillBridge — Production Runbook & Operations Guide (Phase 3)

---

## 1. Quick Start with Docker Compose

To start the complete Phase 3 backend infrastructure (Spring Boot 3, PostgreSQL 16 + PostGIS, Redis 7):

```bash
# 1. Copy backend environment configuration
cp .env.backend.example .env.backend

# 2. Spin up containers in daemon mode
docker compose up -d
```

### Containers & Port Bindings
| Service | Image | Internal Port | Host Port | Purpose |
|---|---|---|---|---|
| `postgres` | `postgis/postgis:16-3.4` | 5432 | `5432` | PostgreSQL + PostGIS spatial engine, 18 tables + seed data |
| `redis` | `redis:7-alpine` | 6379 | `6379` | Dispatch cascade timers (20s TTL), OTP rate limiting, JWT blocklist |
| `backend` | `eclipse-temurin:21-jdk-jammy` | 8080 | `8080` | Spring Boot 3 API (`/v1/*`), OpenAPI Swagger UI, Webhooks |

To view container logs:
```bash
docker compose logs -f backend
```

To stop containers:
```bash
docker compose down
```

---

## 2. Environment Variables & Secret Configuration

Create `.env.backend` from `/.env.backend.example`:

```properties
# Database Configuration
POSTGRES_DB=skillbridge
POSTGRES_USER=skillbridge
POSTGRES_PASSWORD=skillbridge_secure_pass_2026
DATABASE_URL=jdbc:postgresql://postgres:5432/skillbridge

# Redis Configuration
REDIS_HOST=redis
REDIS_PORT=6379

# Security & JWT Tokens
JWT_SECRET=4c9a7218d6e3f5b081a293c4d7e8f1a0b3c5d7e9f2a4b6c8d0e2f4a6b8c0d2e4
JWT_ACCESS_EXPIRATION_MS=900000        # 15 minutes
JWT_REFRESH_EXPIRATION_MS=604800000    # 7 days

# GST Compliance Configuration (Phase 3 Test Mode)
GST_GSTIN=27AABCS1423Q1ZP             # Config placeholder - pending registration
GST_SAC_CODE=998713                    # Service Accounting Code (Placement / Supply Services)
GST_LEGAL_ENTITY_NAME=SkillBridge Technologies India Pvt Ltd

# Payment Gateway (Razorpay TEST Mode)
RAZORPAY_KEY_ID=rzp_test_placeholder_key
RAZORPAY_KEY_SECRET=rzp_test_placeholder_secret
RAZORPAY_WEBHOOK_SECRET=rzp_webhook_secret_key

# Third-Party Provider Credentials
BHASHINI_API_KEY=bhashini_indic_api_key_test
BHASHINI_USER_ID=bhashini_user_id_test
API_SETU_CLIENT_ID=api_setu_digilocker_client_id
API_SETU_CLIENT_SECRET=api_setu_digilocker_client_secret
EXOTEL_ACCOUNT_SID=exotel_sid_test
EXOTEL_API_KEY=exotel_key_test
EXOTEL_API_TOKEN=exotel_token_test
EXOTEL_CALLER_ID=08047123456
FCM_SERVER_KEY=firebase_fcm_server_key_test
```

---

## 3. Obtaining Real Third-Party API Test Keys

### A. Razorpay (Test Mode)
1. Sign up or log into [Razorpay Dashboard](https://dashboard.razorpay.com).
2. Toggle the switch at the top-left from **Live** to **Test Mode**.
3. Navigate to **Settings > API Keys** and click **Generate Key**.
4. Copy the `Key ID` (`rzp_test_...`) and `Key Secret`.
5. Under **Settings > Webhooks**, add a webhook with target URL:
   `https://<your-domain-or-ngrok>/v1/payments/webhook`
6. Select events: `order.paid`, `payment.failed`, `refund.processed`.
7. Set a Webhook Secret and save it to `RAZORPAY_WEBHOOK_SECRET`.

### B. Firebase Cloud Messaging (FCM)
1. Open the [Firebase Console](https://console.firebase.google.com).
2. Create or select the `SkillBridge` project.
3. Add an Android app with package `com.example`.
4. Download `google-services.json` and place it in `/app/`.
5. Under **Project Settings > Cloud Messaging**, generate a Private Key for the Firebase Admin SDK / Server Key and update `FCM_SERVER_KEY`.

### C. Bhashini / ULCA (Indic Speech Pipeline)
1. Register on the [Bhashini API Portal](https://bhashini.gov.in) or [ULCA](https://ulca.bhashini.gov.in).
2. Request an API key for ASR (Speech-to-Text) and TTS (Text-to-Speech) pipelines supporting Hindi, Marathi, Tamil, Telugu, and English.
3. Save the resulting API key to `BHASHINI_API_KEY` and inference endpoint URL.

### D. API Setu / DigiLocker (Aadhaar & Driving Licence eKYC)
1. Register an organization account on [API Setu](https://apisetu.gov.in).
2. Subscribe to the DigiLocker Aadhaar & Driving Licence verification APIs.
3. Create an API client to obtain `API_SETU_CLIENT_ID` and `API_SETU_CLIENT_SECRET`.
4. *Compliance Rule:* SkillBridge strictly adheres to the DPDP Act 2023. Raw Aadhaar numbers are never transmitted or stored in the database. Only the masked format `XXXX-XXXX-1234` and verified cryptographic hashes are retained.

### E. Exotel / Kaleyra (Virtual Masked Calling & IVR)
1. Register on [Exotel](https://exotel.com).
2. Provision a virtual pilot number for India (`EXOTEL_CALLER_ID`).
3. Under API Credentials, generate `EXOTEL_ACCOUNT_SID`, `API_KEY`, and `API_TOKEN`.
4. Configure the IVR applet webhook pointing to `/v1/ivr/webhook` per `docs/IVR_FLOW.md`.

### F. Google Maps Platform (Android & Server Keys)
1. Open the [Google Cloud Console](https://console.cloud.google.com/google/maps-apis/overview) with a billing-enabled project.
2. Enable the 4 required APIs:
   - **Maps SDK for Android**
   - **Places API (New)**
   - **Routes API**
   - **Geocoding API**
3. **Android Client API Key (`MAPS_API_KEY`):**
   - Go to **Credentials > Create Credentials > API Key**.
   - Under **Set application restrictions**, choose **Android apps**.
   - Click **Add an item**:
     - Package name: `in.skillbridge.app`
     - SHA-1 certificate fingerprint:
       Run `keytool -list -v -keystore debug.keystore -alias androiddebugkey -storepass android -keypass android` to obtain your SHA-1.
   - Under **API restrictions**, select **Maps SDK for Android** and **Places API (New)**.
   - Copy key into `.env` (or `local.properties`): `MAPS_API_KEY=AIzaSy...`
4. **Backend Server API Key (`SERVER_GOOGLE_ROUTES_KEY`):**
   - Create a second API Key.
   - Under **Application restrictions**, choose **IP addresses** and enter your backend server/VPC static IP.
   - Under **API restrictions**, select **Routes API** and **Geocoding API**.
   - Copy key into `.env.backend`: `SERVER_GOOGLE_ROUTES_KEY=AIzaSy...`


---

## 4. Android App Environment Configuration

The Android client switches between Mock, Staging, and Production environments via `/app/src/main/java/com/example/skillbridge/config/AppConfig.kt`:

```kotlin
// In AppConfig.kt
var currentEnvironment: Environment = Environment.MOCK // or STAGING, PRODUCTION
```

- **MOCK (`Environment.MOCK`):** Runs on local device without server dependency. Uses deterministic mock providers for testing UI and offline workflows.
- **STAGING (`Environment.STAGING`):** Points to local Docker container (`http://10.0.2.2:8080/v1/` on emulator, or local LAN IP on physical device) with Test Mode Razorpay and DigiLocker sandboxes.
- **PRODUCTION (`Environment.PRODUCTION`):** Points to live secure HTTPS endpoints with real signed payloads.

---

## 5. Verification & Automated Testing

### A. Backend Unit & Shared Vector Tests
Verify backend compilation, JPA repositories, PostGIS spatial queries, and mathematical test vectors:
```bash
cd backend
./gradlew test
```
*Key test suites:*
- `SharedTestVectorsTest`: Guarantees 100% parity between Kotlin and Java implementations of `TrustScoreEngine` (55 baseline score) and `EscrowLedgerEngine` (15% commission, 10% Pro discount).
- `JobStateMachineTest`: Enforces strict forward state transitions (`REQUESTED` -> `ASSIGNED` -> `IN_TRANSIT` -> `IN_PROGRESS` -> `COMPLETED`) and rejects illegal bypasses.
- `LedgerAndPaymentTest`: Validates double-entry accounting balanced to 0 integer paise, HMAC-SHA256 Razorpay signature validation, and idempotent webhook replay protection.

### B. Android Unit & Robolectric Tests
Verify Android Compose UI states, Room database migrations, `OfflineActionQueue`, and repository logic:
```bash
gradle :app:testDebugUnitTest
```
Ensure all 33 tasks execute successfully and 100% of unit tests pass.

---

## 6. Health & Observability Endpoints

| Service | Endpoint | Purpose | Healthy Response |
|---|---|---|---|
| Spring Boot API | `GET http://localhost:8080/actuator/health` | Liveness & Readiness | `{"status":"UP","components":{"db":{"status":"UP"},"redis":{"status":"UP"}}}` |
| Spring Boot Metrics | `GET http://localhost:8080/actuator/metrics` | JVM, HTTP latency, Dispatch stats | Micrometer / Prometheus metrics format |
| OpenAPI 3.0 Documentation | `GET http://localhost:8080/swagger-ui.html` | Interactive API documentation | Swagger UI Explorer |
| Raw OpenAPI Spec | `GET http://localhost:8080/v3/api-docs` | OpenAPI 3.0 JSON specification | OpenAPI Schema |

---

## 7. Disaster Recovery & Incident Management

1. **Redis Cache Crash / Failure:**
   - The Spring Boot dispatch service automatically falls back to direct database lock records (`SELECT ... FOR UPDATE`) if the Redis connection fails.
2. **Payment Gateway Outage:**
   - Razorpay webhook failures trigger automated reconciliation jobs running every 15 minutes.
   - Escrow remains locked until cryptographic signature confirmation is obtained.
3. **Emergency Hotline SOS Alert:**
   - On receiving an SOS alert, support agents are alerted on the ops dashboard, and an automated SMS dispatch is sent to the worker's emergency contact with current GPS coordinates.
4. **Data Erasure (DPDP Act 2023):**
   - User requests through the Data Erasure Dialog are processed by scrubbing personal identifiers (name, phone, avatar) and replacing KYC records with anonymized hashes while maintaining double-entry ledger integrity for tax auditing.
