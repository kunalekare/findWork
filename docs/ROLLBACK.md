# SkillBridge — Production Rollback Runbook (Zero-Downtime / Emergency)

This runbook defines the emergency rollback procedure for SkillBridge backend services and database state during an incident or failed deployment.

---

## 1. Rollback Trigger Criteria

Initiate immediate rollback if ANY of the following occur within 30 minutes of deployment:
1. **Actuator Health Status:** `/actuator/health` returns `DOWN` or HTTP `503`.
2. **Crash Loop:** `docker ps` reports frequent backend container restarts.
3. **Elevated Error Rate:** 5xx response rate crosses 1% over a 5-minute rolling window.
4. **Data Corruption / Ledger Inbalance:** Any discrepancy detected in double-entry escrow sum.
5. **Real-time SSE Gateway Failure:** Dropped persistent streams or inability to replay events.

---

## 2. Emergency Rollback Execution

### Step 1: Revert Container Image to Previous Stable Tag
```bash
# Set target previous stable release
PREV_TAG="1.0.0-pilot-stable"

# Pull or tag previous build
docker compose -f docker-compose.prod.yml down backend

# Update docker-compose image tag and restart
sed -i "s/1.0.0-pilot/1.0.0-pilot-stable/g" docker-compose.prod.yml
docker compose -f docker-compose.prod.yml up -d backend
```

### Step 2: Database State Restoration (If Schema Mutation Failed)
If a Flyway migration failed or corrupted table state:
```bash
# 1. Stop backend traffic
docker compose -f docker-compose.prod.yml stop backend

# 2. Restore database from pre-deployment snapshot
bash scripts/restore_db.sh /var/backups/skillbridge/skillbridge_pre_deploy.sql.gz

# 3. Restart backend
docker compose -f docker-compose.prod.yml start backend
```

### Step 3: Clear Redis Ephemeral State & Active Locks
If stale locks or invalid dispatch keys persist:
```bash
# Flush dispatch keys safely without dropping system auth
docker exec -it skillbridge-redis redis-cli -a "${REDIS_PASSWORD}" --scan --pattern "dispatch:lock:*" | xargs -r docker exec -i skillbridge-redis redis-cli -a "${REDIS_PASSWORD}" del
```

### Step 4: Verification
```bash
# Run smoke test to verify recovered state
bash scripts/smoke_test.sh https://api.skillbridge.in
```

---

## 3. Post-Incident Review Protocol

1. Preserve container logs:
   ```bash
   docker logs skillbridge-backend > /var/log/incident_$(date +%s).log
   ```
2. Post incident status report to SRE channel within 60 minutes.
3. Conduct blameless retrospective and publish Root Cause Analysis (RCA) within 24 hours.
