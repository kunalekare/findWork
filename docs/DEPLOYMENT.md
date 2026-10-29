# SkillBridge — Production Deployment Guide (Nagpur Pilot)

This document provides exact operational procedures for deploying SkillBridge to staging and production pilot infrastructure.

---

## 1. Pre-Deployment Infrastructure Prerequisites

1. **Host Environment:**
   - Linux (Ubuntu 22.04 LTS or Debian 12 recommended)
   - Docker Engine v24.0+ and Docker Compose v2.20+
   - Minimum Specs: 2 vCPU, 4GB RAM, 40GB SSD
2. **DNS & Networking:**
   - Public IP address with ports `80` (HTTP) and `443` (HTTPS) open
   - DNS A record pointing `api.skillbridge.in` to host IP
3. **Credentials & Secrets:**
   - `.env.backend` configured using `.env.backend.example`
   - Real Razorpay Test/Live API keys
   - Google Maps Platform API key restricted to server IP / Android SHA-1

---

## 2. Configuration Validation (Fast-Fail Check)

Before launching services, execute the environment validator:

```bash
# Verify all required environment variables are present and non-empty
bash -c '
required=(POSTGRES_DB POSTGRES_USER POSTGRES_PASSWORD DATABASE_URL REDIS_PASSWORD JWT_SECRET)
for var in "${required[@]}"; do
  if [ -z "${!var}" ]; then
    echo "[FAIL] Missing required environment variable: $var" >&2
    exit 1
  fi
done
echo "[PASS] All required variables present."
'
```

---

## 3. Production Deployment Execution

### Step A: Pull & Build Containers
```bash
# Export production environment
export COMPOSE_FILE=docker-compose.prod.yml

# Build backend and pull dependencies
docker compose -f docker-compose.prod.yml --env-file .env.backend build
```

### Step B: Launch Database & Cache
```bash
# Bring up PostgreSQL PostGIS and Redis first
docker compose -f docker-compose.prod.yml --env-file .env.backend up -d postgres redis

# Verify health status
docker compose -f docker-compose.prod.yml ps
```

### Step C: Run Automated Flyway Migrations & Launch Backend
```bash
# Launch Spring Boot backend and TLS proxy
docker compose -f docker-compose.prod.yml --env-file .env.backend up -d backend reverse-proxy
```

### Step D: Execute Live Smoke Test
```bash
# Run contract verification against localhost or domain
bash scripts/smoke_test.sh https://api.skillbridge.in
```

---

## 4. Post-Deployment Verification

1. **Actuator Health Probe:**
   ```bash
   curl -f https://api.skillbridge.in/actuator/health
   # Expected: {"status":"UP","components":{"db":{"status":"UP"},"redis":{"status":"UP"}}}
   ```
2. **Prometheus Metrics:**
   ```bash
   curl -s https://api.skillbridge.in/actuator/prometheus | grep "jvm_memory"
   ```
3. **Database Migration State:**
   ```bash
   docker exec -it skillbridge-postgres psql -U skillbridge_user -d skillbridge_db -c "SELECT version, description, installed_on, success FROM flyway_schema_history;"
   ```

---

## 5. Maintenance & Backup Automation

Add automated nightly database backup to root crontab:
```bash
# Nightly backup at 02:00 IST with 14-day retention
0 2 * * * /bin/bash /opt/skillbridge/scripts/backup_db.sh /var/backups/skillbridge >> /var/log/skillbridge_backup.log 2>&1
```
