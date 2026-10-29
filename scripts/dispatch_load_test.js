import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '30s', target: 50 },  // Ramp-up to 50 users
    { duration: '1m', target: 250 },   // Sustained peak at 250 requests/sec
    { duration: '20s', target: 0 },    // Ramp-down
  ],
  thresholds: {
    http_req_duration: ['p(95)<200'],  // 95% of dispatch requests must complete under 200ms
    http_req_failed: ['rate<0.01'],    // Error rate must remain under 1%
  },
};

const BASE_URL = __ENV.API_BASE_URL || 'http://localhost:8080/v1';

export default function () {
  const payload = JSON.stringify({
    jobId: `load_test_${__VU}_${__ITER}`,
    categoryId: 'cat_elec',
    latitude: 21.1458 + (Math.random() - 0.5) * 0.02,
    longitude: 79.0882 + (Math.random() - 0.5) * 0.02,
    budget: 450,
  });

  const params = {
    headers: {
      'Content-Type': 'application/json',
      'Idempotency-Key': `idemp_${__VU}_${__ITER}_${Date.now()}`,
      'Authorization': 'Bearer test_jwt_token',
    },
  };

  const res = http.post(`${BASE_URL}/marketplace/request-now`, payload, params);

  check(res, {
    'status is 200 or 201': (r) => r.status === 200 || r.status === 201,
    'response under 200ms': (r) => r.timings.duration < 200,
  });

  sleep(1);
}
