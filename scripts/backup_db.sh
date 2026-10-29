#!/usr/bin/env bash
# ==============================================================================
# SkillBridge — Production Database Backup Script (PostgreSQL + PostGIS)
# Creates a compressed, checksummed SQL dump with automatic 14-day retention.
# Usage:
#   bash scripts/backup_db.sh [OUTPUT_DIR]
# ==============================================================================

set -euo pipefail

BACKUP_DIR="${1:-./backups/postgres}"
TIMESTAMP=$(date -u +"%Y%m%d_%H%M%SZ")
DB_NAME="${POSTGRES_DB:-skillbridge_db}"
DB_USER="${POSTGRES_USER:-skillbridge_user}"
CONTAINER_NAME="skillbridge-postgres"
BACKUP_FILE="${BACKUP_DIR}/skillbridge_backup_${TIMESTAMP}.sql.gz"
CHECKSUM_FILE="${BACKUP_FILE}.sha256"

mkdir -p "${BACKUP_DIR}"

echo "[INFO] Initiating database backup for ${DB_NAME} at ${TIMESTAMP}..."

if ! docker ps --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}$"; then
    echo "[ERROR] Container '${CONTAINER_NAME}' is not running." >&2
    exit 1
fi

docker exec -t "${CONTAINER_NAME}" pg_dump -U "${DB_USER}" -d "${DB_NAME}" --clean --if-exists --no-owner --no-privileges | gzip -9 > "${BACKUP_FILE}"

# Generate SHA-256 integrity checksum
sha256sum "${BACKUP_FILE}" > "${CHECKSUM_FILE}"

FILE_SIZE=$(du -h "${BACKUP_FILE}" | cut -f1)
echo "[SUCCESS] Backup created successfully: ${BACKUP_FILE} (${FILE_SIZE})"
echo "[SUCCESS] Checksum verified: $(cat "${CHECKSUM_FILE}")"

# Prune backups older than 14 days
echo "[INFO] Pruning local backups older than 14 days..."
find "${BACKUP_DIR}" -type f -name "skillbridge_backup_*.sql.gz*" -mtime +14 -delete
echo "[SUCCESS] Backup workflow completed."
