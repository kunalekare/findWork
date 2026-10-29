# SkillBridge — Privacy, Legal & Regulatory Compliance Checklist

**Jurisdiction:** Republic of India  
**Target Scope:** Nagpur Controlled Pilot (State of Maharashtra)  
**Applicable Statutes:**
1. Digital Personal Data Protection Act, 2023 (DPDP Act)
2. Information Technology (Reasonable Security Practices and Procedures and Sensitive Personal Data or Information) Rules, 2011 (SPDI Rules)
3. Aadhaar (Targeted Delivery of Financial and other Subsidies, Benefits and Services) Act, 2016 (Section 29)
4. Reserve Bank of India (RBI) Guidelines on Regulation of Payment Aggregators and Payment Gateways (DPSS.CO.PD.No.1810/02.14.008/2019-20)
5. Code on Social Security, 2020 (Chapter IX: Social Security for Gig Workers and Platform Workers)

> **LEGAL NOTICE:** This document constitutes an engineering and architectural compliance audit. It must be formally reviewed and approved by a qualified Indian legal counsel and chartered accountant prior to public commercial launch.

---

## 1. Compliance Audit & Control Matrix

| Regulatory Requirement | Statute / Guidelines | Engineering Implementation in SkillBridge | Status |
|---|---|---|---|
| **Notice & Multilingual Consent** | DPDP Act 2023, Section 5 | Itemized, granular consent presented in English, Hindi, and Marathi during onboarding. Users explicitly opt into location tracking and contact sharing. | **Implemented** |
| **Right to Erasure & Withdrawal** | DPDP Act 2023, Section 12 | In-app "Data Erasure & Account Deletion" workflow in `ProfileScreen.kt` and `POST /v1/users/delete-account` with 30-day purge grace period. | **Implemented** |
| **Aadhaar Number Protection** | Aadhaar Act, Sec 29 & DPDP Sec 6 | Zero raw Aadhaar storage rule. Only the last 4 digits are persisted (`XXXX-XXXX-1234`). Verification performed via DigiLocker / API Setu. | **Implemented** |
| **Escrow & Nodal Account Handling** | RBI Guidelines on Payment Aggregators | All escrow funds collected through Razorpay Route / Nodal escrow account with scheduled T+1 / T+2 settlement. SkillBridge does not hold commingled user funds in operating current accounts. | **Pending Live Nodal MoU** |
| **GST Invoicing & SAC Classification** | Central Goods & Services Tax Act, 2017 | Invoicing engine calculates 18% GST (9% CGST + 9% SGST). In production, generation is blocked until valid corporate GSTIN is configured. | **Guarded with Pilot Warning** |
| **Gig Worker Voluntary Engagement** | Code on Social Security, 2020 | Workers set their own hourly rates, choose service radii, and retain the right to toggle Online/Offline at will without automated deactivation penalties. | **Compliant** |

---

## 2. Draft Legal Policies (English / हिंदी / मराठी)

### A. Terms of Service Summary
- **EN:** SkillBridge operates as an intermediary marketplace connecting service seekers with independent verified professionals. SkillBridge does not act as an employer. Escrow payments are held securely until task completion or dispute resolution.
- **HI:** स्किलब्रिज एक मध्यस्थ मंच है जो स्वतंत्र कुशल कारीगरों को ग्राहकों से जोड़ता है। स्किलब्रिज कोई नियोक्ता नहीं है। जब तक काम पूरा नहीं होता, पैसा सुरक्षित एस्क्रो में रहता है।
- **MR:** स्किलब्रिज हे स्वतंत्र कुशल कामगारांना ग्राहकांशी जोडणारे डिजिटल व्यासपीठ आहे. काम पूर्ण होईपर्यंत किंवा वाद मिटेपर्यंत रक्कम सुरक्षित एस्क्रो खात्यात ठेवली जाते.

### B. Privacy Policy Summary
- **EN:** We collect location telemetry strictly while you are online or during an active job dispatch to calculate accurate ETAs and ensure safety. Telemetry stops immediately when going offline. Phone numbers are protected using masked calling.
- **HI:** जब आप ऑनलाइन होते हैं या किसी काम पर जा रहे होते हैं, तभी आपकी लोकेशन ली जाती है। ऑफलाइन होने पर ट्रैकिंग बंद हो जाती है। फोन नंबर मास्क कॉल द्वारा सुरक्षित रहते हैं।
- **MR:** आपण ऑनलाइन असताना किंवा कामावर जातानाच लोकेशन घेतले जाते. ऑफलाइन झाल्यावर ट्रॅकिंग लगेच थांबते. कॉलिंगसाठी फोन नंबर सुरक्षितपणे लपवला जातो.

### C. Cancellation & Dispute Resolution Policy
- **EN:** Free cancellation is permitted before worker arrival. If cancelled after arrival, a nominal transit fee (₹50) is disbursed to the worker. Disputes are reviewed within 48 hours by the Nagpur resolution desk.
- **HI:** कारीगर के पहुंचने से पहले रद्दीकरण निःशुल्क है। पहुंचने के बाद रद्द करने पर ₹50 का यात्रा शुल्क कारीगर को दिया जाएगा। विवादों का समाधान 48 घंटों में किया जाता है।
- **MR:** कामगार येण्यापूर्वी रद्द करणे विनामूल्य आहे. कामगार आल्यानंतर रद्द केल्यास ₹50 प्रवास शुल्क कामगाराला दिले जाईल. तक्रारींचे निवारण 48 तासांत केले जाते.

---

## 3. Google Play Store Data Safety Declaration

When submitting the SkillBridge APK to Google Play Console:
1. **Location:**
   - *Precise Location:* Collected in foreground while online/en-route (App functionality, safety, fraud prevention).
   - *Ephemeral Tracking:* Coordinates are NOT shared with third-party advertising brokers.
2. **Personal Info:**
   - *Name, Phone, Email:* Collected for account management and authentication.
   - *Government ID:* Masked identity verification via DigiLocker.
3. **Financial Info:**
   - *Payment Info & UPI ID:* Processed via PCI-DSS Level 1 certified payment gateway (Razorpay).
4. **Security Practices:**
   - Data encrypted in transit using TLS 1.3.
   - User account deletion request mechanism provided in-app.
