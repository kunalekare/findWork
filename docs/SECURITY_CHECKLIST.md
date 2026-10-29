# SkillBridge — OWASP ASVS & MASVS Security Verification Checklist

**Standard:** OWASP Application Security Verification Standard (ASVS 4.0 Level 2) & Mobile Application Security Verification Standard (MASVS v2).  
**Target:** SkillBridge Nagpur Pilot Launch (Spring Boot 3 + PostgreSQL/Redis + Android Kotlin Compose).  
**Review Date:** October 2026.

---

## 1. Mobile Application Security (MASVS) Audit

| Requirement ID | Security Domain | Verification Criteria | Implementation & Evidence | Audit Status |
|---|---|---|---|---|
| **MASVS-STORAGE-1** | Sensitive Data Storage | System credential storage in hardware-backed Keystore or encrypted preferences. | No raw passwords or plaintext JWTs written to shared storage; auth tokens stored in protected private application sandbox. | **PASS** |
| **MASVS-STORAGE-2** | PII Minimization | Zero raw storage of unmasked Aadhaar numbers (DPDP Act 2023). | Strictly enforces 4-digit masking (`XXXX-XXXX-1234`) across UI, SQLite Room entities, and transit DTOs. | **PASS** |
| **MASVS-CRYPTO-1** | Cryptography | Industry-standard algorithms and authenticated encryption. | HMAC-SHA256 for Razorpay webhooks, HS512 with 256-bit cryptographically secure keys for JWT signing. | **PASS** |
| **MASVS-NETWORK-1** | Network Communication | All network communication occurs over TLS v1.3 with validated certificates. | Enforced via Caddy TLS proxy, Android Network Security Configuration rejecting cleartext HTTP traffic. | **PASS** |
| **MASVS-PLATFORM-1** | System Permissions | Principle of least privilege for Android hardware permissions. | Camera requested via standard PhotoPicker; Fine Location active exclusively during active dispatch via foreground service. | **PASS** |
| **MASVS-CODE-1** | Code Quality | Obfuscation, symbol stripping, and tampering prevention. | ProGuard rules enabled for release build (`proguard-rules.pro`), debug logs stripped in production. | **PASS** |

---

## 2. Server & API Security (ASVS Level 2) Audit

| Requirement ID | Security Domain | Verification Criteria | Implementation & Evidence | Audit Status |
|---|---|---|---|---|
| **V2.1.1** | Password & Credential Security | Enforce strong credential requirements and rate-limiting on authentication endpoints. | Staff login enforces 5-attempt rate limit followed by 15-minute lockout; verified by `SseAndStaffAuthTest`. | **PASS** |
| **V2.2.1** | SMS/OTP Rate Limiting | Rate-limiting OTP generation and verification to mitigate SMS toll fraud and brute forcing. | Max 3 verification attempts per OTP with 300s expiration and 60s resend cooldown. | **PASS** |
| **V3.1.1** | Session Management | Stateless JWT with cryptographically unique identifiers and refresh token rotation. | Refresh tokens contain random UUID `jti` and are rotated upon every refresh request (`JwtService.java`). | **PASS** |
| **V4.1.1** | Access Control (RBAC) | Role-based endpoint gating (TASK_POSTER, WORKER, ADMIN). | Spring Security filters and programmatic verification of roles; non-staff accounts cannot access `/v1/admin/*`. | **PASS** |
| **V4.2.1** | Direct Object Reference (IDOR) | Users cannot modify or access records belonging to other users. | All job mutations, proposal selections, and payouts validate session principal ownership against database records. | **PASS** |
| **V5.1.1** | Input Validation | Strict validation of geographic coordinates, monetary amounts, and enum values. | Positive integers enforced for budget and milestone amounts; PostGIS latitude [-90, 90] and longitude [-180, 180] checked. | **PASS** |
| **V8.1.1** | Data Protection (PII) | Telephony masking and virtual bridge numbers. | Exotel telephony provider masks consumer and worker phone numbers; direct raw caller IDs never exposed in UI or transit. | **PASS** |
| **V13.1.1** | API Webhook Integrity | Cryptographic HMAC signature validation for external webhook events. | `RazorpayService.java` validates `X-Razorpay-Signature` against secret; deduplicates event IDs to prevent replay attacks. | **PASS** |
| **V14.1.1** | Concurrency & Race Conditions | Single-worker offer locking and atomic CAS assignment. | Redis distributed locks with 20s TTL and atomic CAS database updates; verified by `DispatchConcurrencyTest`. | **PASS** |

---

## 3. Residual Security Findings & Pilot Recommendations

1. **Certificate Pinning:** Recommend enabling OkHttp CertificatePinner in production build once domain CA certificates are finalized.
2. **Web Application Firewall (WAF):** Place Cloudflare or AWS WAF in front of `api.skillbridge.in` to block bot scrapers and Layer 7 DDoS attacks.
3. **Audit Log Centralization:** Forward structured JSON security audit logs to an immutable write-once S3/GCS bucket for SOC2 compliance.
