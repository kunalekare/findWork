# SkillBridge — Two-Device End-to-End Verification Checklist (Phase 4)

This checklist verifies the complete marketplace loop across two devices/emulators communicating through the backend server:
- **Device A (Task-Poster):** Phone running `in.skillbridge.app` in Poster Mode.
- **Device B (Worker):** Phone running `in.skillbridge.app` in Worker Mode.

> **Environment Note:** Since the cloud container does not host an Android emulator or ADB bridge, this procedure and test scripts are marked **Written, not executed here**. Run the commands below on your local machine with two Android Virtual Devices (AVDs).

---

## 1. Local Environment & Emulator Setup

### A. Start Backend Infrastructure
```bash
# 1. Start PostgreSQL with PostGIS and Redis
docker compose up -d

# 2. Verify containers are healthy
docker compose ps
curl http://localhost:8080/actuator/health
```

### B. Launch Two Android Emulators
```bash
# Terminal 1: Launch Poster Device (Port 5554)
emulator -avd Pixel_8_Poster -port 5554 &

# Terminal 2: Launch Worker Device (Port 5556)
emulator -avd Pixel_8_Worker -port 5556 &
```

### C. Build & Install SkillBridge on Both Devices
```bash
gradle :app:assembleDebug
adb -s emulator-5554 install app/build/outputs/apk/debug/app-debug.apk
adb -s emulator-5556 install app/build/outputs/apk/debug/app-debug.apk
```

---

## 2. GPS Simulation Helper Script

Use `/scripts/simulate_worker_route.sh` to stream progressive GPS waypoints to the worker device:

```bash
# Simulates driving from Dharampeth to Ramdaspeth, Nagpur (approx 2.4 km)
bash scripts/simulate_worker_route.sh emulator-5556
```

---

## 3. End-to-End Happy Path Run

| Step # | Actor | Action | Expected Result | Pass / Fail |
|---|---|---|---|---|
| **HP-01** | Device A (Poster) | Log in via OTP (`+91 94221 67890`, OTP: `123456`) | Poster home dashboard displays with Nagpur location and 14 categories. | [ ] |
| **HP-02** | Device B (Worker) | Log in via OTP (`+91 98220 12345`, OTP: `123456`) | Worker home dashboard displays; toggle switch set to **Online** (green badge). | [ ] |
| **HP-03** | Device B (Worker) | Background worker app | `WorkerLocationForegroundService` notification appears: *"Online: Sharing availability location with nearby Task-Posters"*. | [ ] |
| **HP-04** | Device A (Poster) | Tap **Electrician** ➔ Tap **Request Now (Immediate)** | Escrow payment modal opens with ₹400 estimate and 15% platform fee breakdown. | [ ] |
| **HP-05** | Device A (Poster) | Confirm Escrow Deposit (Razorpay Test Mode) | Escrow hold entry recorded; Live Radar screen opens with 2km search circle and *"3 Workers Nearby"* pill. | [ ] |
| **HP-06** | Device B (Worker) | Observe incoming request | Heads-up push notification & full-screen 20-second countdown ring appear with ₹340 estimated payout. | [ ] |
| **HP-07** | Device B (Worker) | Tap **Accept Request** within 20 seconds | Atomic CAS succeeds on server; worker offer lock transitions to MATCHED. | [ ] |
| **HP-08** | Device A (Poster) | Observe Radar screen transition | Radar automatically updates to **MATCHED**; worker card (Ramesh Sharma, 94% Trust) and route appear. | [ ] |
| **HP-09** | Device B (Worker) | Tap **I Am En Route** | Status advances to `EN_ROUTE`; foreground service switches to 5s adaptive GPS update interval. | [ ] |
| **HP-10** | Device B (Worker) | Tap **Open in Google Maps** | Google Maps app opens with turn-by-turn navigation intent to destination coordinates. | [ ] |
| **HP-11** | Device A (Poster) | Observe live route tracking | Worker marker moves smoothly along orange polyline; ETA updates live (~12 mins ➔ ~4 mins). | [ ] |
| **HP-12** | Device B (Worker) | Tap **I Have Arrived at Location** | Status advances to `ARRIVED`; Poster receives notification: *"Worker has arrived at site"*. | [ ] |
| **HP-13** | Device B (Worker) | Tap **Start Work & Diagnostic** | Status advances to `IN_PROGRESS`; live session timer starts. | [ ] |
| **HP-14** | Device B (Worker) | Take/select proof photo ➔ Tap **Mark Job Complete** | Completion proof uploaded; status advances to `COMPLETED`; Escrow release dialog prompts Poster. | [ ] |
| **HP-15** | Device A (Poster) | Review proof & Tap **Confirm & Release Payment** | Escrow releases ₹340 to worker wallet; platform retains ₹60 (15%); ledger balanced to 0 integer paise. | [ ] |
| **HP-16** | Both Devices | Submit 5-star ratings with feedback | Trust score recalculated on server; GST-compliant tax invoice generated with SAC `998713`. | [ ] |

---

## 4. Negative & Resilience Paths

| Scenario | Action | Expected System Behavior | Pass / Fail |
|---|---|---|---|
| **NEG-01: 20s Cascade Ignore** | Worker 1 ignores incoming 20s ring | On 20s expiration, server automatically unlocks Worker 1 and cascades offer to Worker 2. | [ ] |
| **NEG-02: Explicit Rejection** | Worker 1 taps Reject | Server releases Worker 1 lock immediately and cascades offer to Worker 2 without waiting for 20s. | [ ] |
| **NEG-03: Radius Expansion** | No workers found within 2km | Radar expands from 2km ➔ 5km ➔ 10km. If still no candidates, transitions cleanly to `NO_WORKER_FOUND`. | [ ] |
| **NEG-04: Poster Cancellation** | Poster cancels while searching | Active cascade cancelled; worker offer lock released immediately; escrow hold returned to poster wallet. | [ ] |
| **NEG-05: Worker Cancellation** | Worker cancels after match | Server frees poster for re-assignment without charging a second escrow deposit. | [ ] |
| **NEG-06: Escrow Dispute Freeze** | Poster raises dispute during service | Escrow ledger entry flagged as `DISPUTED`; funds frozen from auto-release; routed to Admin Console. | [ ] |
| **NEG-07: Network Drop & Recovery** | Worker phone goes offline in transit | Actions enqueued in Room `OfflineActionQueue`; SSE stream reconnects with `Last-Event-ID` upon signal restore. | [ ] |
