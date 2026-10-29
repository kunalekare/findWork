# SkillBridge — Google Maps Platform Architecture & Cost Controls (Phase 4)

---

## 1. Required Google Cloud APIs

To power the two-sided work marketplace radar, real-time dispatch, routing, and address autocomplete, enable the following four APIs in the [Google Cloud Console](https://console.cloud.google.com/google/maps-apis/overview):

| API | Runtime Environment | Purpose | Cost Optimization Strategy |
|---|---|---|---|
| **Maps SDK for Android** | Android Client | Native map rendering in Jetpack Compose, dynamic radar circle, worker/poster markers | Vector tiles cached locally on device; rendered only when key is configured |
| **Places API (New)** | Android Client | Address search & autocomplete for service requests and job postings | Session Tokens used per search session; autocomplete triggered only after min 3 characters typed |
| **Routes API** | Spring Boot Backend | Compute driving distance, turn-by-turn route polyline, and live ETA | Called **only** for Top-$N$ ranked candidates ($N=3$); never queried for all pool workers |
| **Geocoding API** | Spring Boot Backend | Reverse geocoding of worker/job GPS coordinates into Indian landmark addresses | Results cached in Redis / in-memory with a 7-day TTL |

> **Google Terms of Service Constraint:**
> Under Google Maps Platform Terms of Service, route polylines and directions computed via the Google Routes API must strictly be displayed on a Google Map, never mixed with third-party map tiles (such as MapLibre/OSRM). SkillBridge strictly isolates Google routing data to `GoogleMapsProvider`.

---

## 2. API Key Restrictions & Secret Management

SkillBridge enforces a strict **Zero-Secret Client Policy** with segregated Android and Server keys:

### A. Android Client Key (`MAPS_API_KEY`)
- **Key Restrictions:**
  - **Application Restriction:** Android apps only.
  - **Package Name:** `in.skillbridge.app`
  - **SHA-1 Fingerprint:** Added for both debug keystore and release signing keystore.
  - **API Restriction:** Restrict strictly to **Maps SDK for Android** and **Places API (New)**.
- **In-App Handling:** Stored in `.env` (or `local.properties`) and injected via the Secrets Gradle Plugin into `BuildConfig.MAPS_API_KEY` and `AndroidManifest.xml` (`com.google.android.geo.API_KEY`).
- **Fail-Safe Startup Check:** If `MAPS_API_KEY` is missing or placeholder, the app displays a non-blocking in-app warning banner and gracefully falls back to the high-fidelity Vector Radar Canvas.

### B. Backend Server Key (`SERVER_GOOGLE_ROUTES_KEY`)
- **Key Restrictions:**
  - **Application Restriction:** IP addresses (restricted strictly to backend server / VPC static IPs).
  - **API Restriction:** Restrict strictly to **Routes API** and **Geocoding API**.
- **In-App Handling:** Injected exclusively into backend environment variables (`.env.backend`); never exposed to mobile APKs or repository source code.

---

## 3. Cost & Reliability Controls (Section B.6)

1. **PostGIS Pre-filtering (Zero Cost):**
   Nearby online workers within the active search radius (2 km ➔ 5 km ➔ 10 km max) are queried using PostGIS straight-line spatial distance (`ST_DWithin` / Haversine). No Google APIs are invoked during initial spatial indexing.

2. **Top-N Candidate Routing ($N=3$, Configurable):**
   Google Routes API requests are executed exclusively for the top $N$ pre-ranked workers ($N=3$ by default). For a pool of 50 nearby workers, exactly 3 route calculations are made.

3. **Throttled Live ETA Recomputation (100m / 30s):**
   During transit (`JobStatus.EN_ROUTE`), live ETA and polyline updates are recomputed only if:
   - The worker moves **more than 100 meters**, OR
   - **More than 30 seconds** have elapsed since the last computation.
   If neither condition is met, the backend reuses the cached route snapshot.

4. **Daily Usage Budget, Alerting & Circuit Breaker (Kill Switch):**
   - **Daily Budget:** Configured in `GoogleMapsUsageTracker.java` (default 5,000 requests/day).
   - **80% Warning Alert:** An automated log warning is emitted when usage hits 80% of daily quota.
   - **100% Circuit Breaker (Kill Switch):** When daily quota hits 100%, `GoogleMapsUsageTracker` trips `isKillSwitchActive() = true`. All subsequent routing requests instantly fall back to zero-cost Euclidean straight-line distance calculations (assuming 20 km/h average Indian urban travel speed). The matching loop never crashes or halts.

---

## 4. Location Privacy & Foreground Service Telemetry

- **Foreground Service (`WorkerLocationForegroundService`):**
  - Starts only when a worker toggles **Online** or is **On an active job**.
  - Dynamic update interval: **5 seconds** while en route; **15 seconds** while idle online.
  - Automatically terminates (`stopForeground` and `stopSelf`) immediately upon toggling **Offline** or upon job completion.
- **Location Freshness:** GPS telemetry older than **60 seconds** is flagged as stale and excluded from dispatch eligibility.
- **Data Minimization:** Live worker coordinates are shared exclusively with the matched Task-Poster during active service delivery.
