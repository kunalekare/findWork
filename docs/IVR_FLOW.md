# SkillBridge — Phone Keypad / IVR Call Flow Architecture
**Toll-Free Helpline:** `1800-SKILL-IN` (`1800-754-5546`)  
**Telephony Engine:** Asterisk / FreeSWITCH / Exotel Voice API Gateway

---

## 1. Keypad Menu Tree & Multi-Lingual Decision Matrix

```
[Inbound Call from Citizen / Worker]
                 │
                 ▼
      [Step 1: Language Selection]
      "Namaste. Hindi ke liye 1 dabayein."
      "Marathi sathi 2 daba."
      "For English, press 3."
                 │
       ┌─────────┼─────────┐
     [1: HI]  [2: MR]   [3: EN]
       └─────────┬─────────┘
                 │
                 ▼
      [Step 2: Intent Selection]
      "Yadi aapko kaam chahiye toh 1 dabayein." (Worker Mode)
      "Yadi aapko worker chahiye toh 2 dabayein." (Employer Mode)
                 │
       ┌─────────┴─────────┐
   [1: Worker]        [2: Employer]
       │                   │
       ▼                   ▼
 [Worker Sub-Tree]   [Employer Sub-Tree]
 1. Electrician      1. Electrician
 2. Plumber          2. Plumber
 3. Cook / Chef      3. Cook / Chef
 4. Driver           4. Driver
 5. Other Skilled    5. Other Services
       │                   │
       └─────────┬─────────┘
                 │
                 ▼
      [Step 3: PIN Code Entry]
      "Apne shehar ya area ka 6-digit PIN code dial karein, ant me hash (#) dabayein."
      (Regex validation: ^[1-9][0-9]{5}$)
                 │
                 ▼
      [Step 4: Confirmation & SMS Dispatch]
      "Dhanyawad! Aapka order darj kar liya gaya hai."
      - Automatic reverse lookup of PIN code (e.g. 440010 -> Dharampeth, Nagpur).
      - Job created in SkillBridge dispatch engine with tag "SOURCE_IVR".
      - SMS dispatched to caller with tracking web link and telephone cancellation PIN.
```

---

## 2. Telephony Webhook Specification (VoiceXML / JSON)

```json
{
  "callSid": "CA_8920194820",
  "fromNumber": "+919822012345",
  "digitsPressed": "1",
  "step": "LANGUAGE_SELECTION",
  "nextAction": {
    "playAudioUrl": "https://assets.skillbridge.in/audio/ivr/hi_intent_prompt.mp3",
    "gatherDigits": {
      "numDigits": 1,
      "timeoutSeconds": 8,
      "actionUrl": "https://api.skillbridge.in/v1/telephony/ivr/gather-intent"
    }
  }
}
```
