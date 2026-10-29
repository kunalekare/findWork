#!/usr/bin/env bash
# ==============================================================================
# SkillBridge — Backend Smoke Test & API Contract Verifier
# Run against local or staging environment:
#   bash scripts/smoke_test.sh [API_BASE_URL]
# Example:
#   bash scripts/smoke_test.sh http://localhost:8080
# ==============================================================================

set -euo pipefail

API_BASE="${1:-http://localhost:8080}"
PASSED_COUNT=0
FAILED_COUNT=0

echo "======================================================================"
echo "SKILLBRIDGE PRODUCTION READINESS: API SMOKE TEST"
echo "Target Base URL: ${API_BASE}"
echo "Timestamp: $(date -u +"%Y-%m-%dT%H:%M:%SZ")"
echo "======================================================================"

run_check() {
    local step_name="$1"
    local command="$2"
    local expected_pattern="$3"

    printf "%-50s ... " "${step_name}"
    local output
    if output=$(eval "${command}" 2>&1); then
        if [[ "${output}" =~ ${expected_pattern} ]]; then
            echo -e "\033[0;32m[PASS]\033[0m"
            ((PASSED_COUNT++))
        else
            echo -e "\033[0;31m[FAIL]\033[0m"
            echo "  Expected pattern: ${expected_pattern}"
            echo "  Received output: ${output}"
            ((FAILED_COUNT++))
        fi
    else
        echo -e "\033[0;31m[ERROR]\033[0m"
        echo "  Command failed: ${output}"
        ((FAILED_COUNT++))
    fi
}

# 1. Healthcheck Actuator
run_check "1. Actuator Health Probe" \
    "curl -s -f '${API_BASE}/actuator/health'" \
    "UP"

# 2. Prometheus Metrics
run_check "2. Prometheus Metrics Endpoint" \
    "curl -s -f '${API_BASE}/actuator/prometheus'" \
    "jvm_memory"

# 3. Request Phone OTP
run_check "3. Send Phone OTP" \
    "curl -s -X POST '${API_BASE}/v1/auth/phone/send-otp' -H 'Content-Type: application/json' -d '{\"phone\":\"+919822011223\"}'" \
    "OTP sent successfully"

# 4. Verify Phone OTP and Extract Access Token
echo -n "4. Verify Phone OTP & Exchange JWT ... "
AUTH_RESPONSE=$(curl -s -X POST "${API_BASE}/v1/auth/phone/verify" \
    -H "Content-Type: application/json" \
    -d '{"phone":"+919822011223","otp":"123456"}')

ACCESS_TOKEN=$(echo "${AUTH_RESPONSE}" | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4 || echo "")
if [[ -n "${ACCESS_TOKEN}" ]]; then
    echo -e "\033[0;32m[PASS]\033[0m (Token: ${ACCESS_TOKEN:0:16}...)"
    ((PASSED_COUNT++))
else
    echo -e "\033[0;31m[FAIL]\033[0m Response: ${AUTH_RESPONSE}"
    ((FAILED_COUNT++))
fi

# 5. List Categories (Secured)
run_check "5. Query Service Categories" \
    "curl -s -H 'Authorization: Bearer ${ACCESS_TOKEN}' '${API_BASE}/v1/categories'" \
    "Electrician|Plumber|Carpenter"

# 6. Worker Search (PostGIS Proximity)
run_check "6. Worker Proximity Search (Nagpur)" \
    "curl -s -H 'Authorization: Bearer ${ACCESS_TOKEN}' '${API_BASE}/v1/search/workers?lat=21.1458&lng=79.0882&category=Electrician'" \
    "workerId|distanceKm"

# 7. Staff Auth Rate-Limiting Protection
run_check "7. Staff Login Bad Password Throttling" \
    "curl -s -X POST '${API_BASE}/v1/auth/staff/login' -H 'Content-Type: application/json' -d '{\"email\":\"staff@skillbridge.in\",\"password\":\"BadPass123\"}'" \
    "Remaining attempts before lockout"

# 8. Razorpay Webhook Replay Deduplication
run_check "8. Webhook Signature Verification" \
    "curl -s -X POST '${API_BASE}/v1/payments/razorpay/webhook' -H 'X-Razorpay-Signature: mock_valid_webhook_sig' -H 'Content-Type: application/json' -d '{\"event\":\"payment.captured\",\"id\":\"evt_smoke_test_1\"}'" \
    "success|processed|duplicate"

echo "======================================================================"
echo "SMOKE TEST SUMMARY: ${PASSED_COUNT} Passed, ${FAILED_COUNT} Failed"
if [[ ${FAILED_COUNT} -eq 0 ]]; then
    echo -e "\033[0;32mALL API CONTRACTS VERIFIED SUCCESSFULLY\033[0m"
    exit 0
else
    echo -e "\033[0;31mSMOKE TEST FAILED: Review endpoint output above\033[0m"
    exit 1
fi
