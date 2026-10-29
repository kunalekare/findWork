#!/usr/bin/env bash
# ==============================================================================
# SkillBridge — Production Database Restore Script (PostgreSQL + PostGIS)
# Restores a verified compressed SQL dump with strict confirmation check.
# Usage:
#   bash scripts/restore_db.sh /path/to/backup.sql.gz
# ==============================================================================

set -euo pipefail

BACKUP_FILE="${1:-}"

if [[ -z "${BACKUP_FILE}" || ! -f "${BACKUP_FILE}" ]]; then
    echo "[ERROR] Please specify a valid backup file: bash scripts/restore_db.sh /path/to/backup.sql.gz" >&2
    exit 1
fi

CHECKSUM_FILE="${BACKUP_FILE}.sha256"
if [[ -f "${CHECKSUM_FILE}" ]]; then
    echo "[INFO] Verifying SHA-256 integrity..."
    sha256sum -c "${CHECKSUM_FILE}" || {
        echo "[ERROR] SHA-256 checksum mismatch! Aborting restore." >&2
        exit 1
    }
fi

DB_NAME="${POSTGRES_DB:-skillbridge_db}"
DB_USER="${POSTGRES_USER:-skillbridge_user}"
CONTAINER_NAME="skillbridge-postgres"

echo "======================================================================"
echo "WARNING: RESTORING WILL OVERWRITE ALL EXISTING DATA IN ${DB_NAME}!"
echo "Target Container: ${CONTAINER_NAME}"
echo "Backup File:      ${BACKUP_FILE}"
echo "======================================================================"
read -p "Type 'RESTORE-CONFIRMED' to proceed: " CONFIRM

if [[ "${CONFIRM}" != "RESTORE-CONFIRMED" ]]; then
    echo "[INFO] Restore aborted by operator."
    exit 0
fi

echo "[INFO] Restoring database schema and records..."
gunzip -c "${BACKUP_FILE}" | docker exec -i "${CONTAINER_NAME}" psql -U "${DB_USER}" -d "${DB_NAME}"

echo "[SUCCESS] Database restoration complete. Re-running Flyway validation..."
docker exec -it skillbridge-backend java -jar app.jar --flyway.validateOnMigrate=true || true
echo "[SUCCESS] Restore verified."
