# SkillBridge — Data Protection & Privacy Policy (DPDP Act 2023 Compliance)
**Effective Date:** 2026-10-29  
**Applicability:** India Operations & Digital Personal Data Protection Act, 2023 (DPDP Act)

---

## 1. Principles of Data Minimization & Consent

SkillBridge operates under strict compliance with the Digital Personal Data Protection Act, 2023 (DPDP Act):

1. **Lawful & Purpose-Specific Processing:**
   - Personal data (phone number, geolocation, voice snippets, skill certificates) is collected strictly for matching, dispatching, verifying identity, and processing payments.
   - Separate, unbundled checkboxes are presented for:
     - Profile creation and service dispatch.
     - Aadhaar / DigiLocker verification.
     - Live location tracking during active job transit.

2. **No Storage of Raw Aadhaar Numbers:**
   - SkillBridge never stores 12-digit Aadhaar numbers or raw unredacted biometric identity cards.
   - Identity verification via DigiLocker / UIDAI returns an authenticated verification status, masked token (`XXXX-XXXX-4912`), and cryptographic signature.

3. **Masked Telephony & Contact Protection:**
   - Both workers and employers communicate using temporary virtual proxy phone numbers.
   - In-app chat strips or masks personal telephone numbers, UPI IDs, and external links prior to delivery to prevent out-of-platform scamming or harassment.

---

## 2. User Rights & Data Subject Access Requests (DSAR)

Under Chapter III of the DPDP Act 2023, every registered User (Data Principal) has the following self-service rights directly in the SkillBridge app:

1. **Right to Access Information:**
   - Users can download a full, machine-readable JSON archive of all profile data, completed jobs, ledger transactions, and rating history from `Profile ➔ Privacy ➔ Export My Data`.

2. **Right to Correction & Erasure ("Right to Be Forgotten"):**
   - Users can trigger one-tap account deletion via `Profile ➔ Privacy ➔ Delete Account`.
   - Upon deletion request:
     - Identifiable personal information is scrubbed within 7 business days.
     - Financial transaction records and tax-related double-entry journals are retained in immutable, pseudonymized form for 7 years to comply with statutory GST and banking audit requirements.

3. **Right of Grievance Redressal:**
   - Grievance Officer details are published in-app:
     - **Email:** `privacy@skillbridge.in` / `grievance-officer@skillbridge.in`
     - **Physical Address:** SkillBridge Technology Hub, Civil Lines, Nagpur, Maharashtra - 440001
   - All grievances are acknowledged within 24 hours and resolved within 72 hours.

---

## 3. Data Retention & Security Standards

| Data Category | Retention Period | Encryption Standard | Access Control |
|---|---|---|---|
| Phone & Auth Tokens | Active session + 90 days after deactivation | AES-256 at rest, TLS 1.3 in transit | Restricted to Auth Microservice |
| Voice Clips & Transcripts | 30 days (for dispute resolution, then scrubbed) | AES-256 | Internal safety review team only |
| Financial & Escrow Ledger | 7 years (Statutory GST requirement) | Immutable append-only PostgreSQL | Read-only finance audit role |
| Live GPS Telemetry | Active job duration + 48 hours buffer | Ephemeral in Redis | Worker & Employer during active transit |
| KYC Masked References | Duration of active worker profile | AES-256 with Hardware Security Module (HSM) | Admin / KYC verification queue |
