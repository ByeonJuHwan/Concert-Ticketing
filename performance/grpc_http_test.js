// http-load-test.js
import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend, Counter } from 'k6/metrics';

// 커스텀 메트릭 정의
const errorRate = new Rate('error_rate');
const successRate = new Rate('success_rate');
const responseTrend = new Trend('response_time_custom');
const requestCounter = new Counter('total_requests');

export const options = {
  stages: [
    { duration: '30s', target: 10 },  // 워밍업
    { duration: '1m', target: 50 },   // 램프업
    { duration: '2m', target: 50 },   // 유지
    { duration: '30s', target: 100 }, // 스파이크
    { duration: '1m', target: 100 },  // 유지
    { duration: '30s', target: 0 },   // 램프다운
  ],
  thresholds: {
    'success_rate': ['rate>0.95'],
    'error_rate': ['rate<0.05'],
    'http_req_duration': ['p(95)<500', 'p(99)<1000'],
    'http_req_failed': ['rate<0.01'],
  },
};

export default function () {
  const userId = Math.floor(Math.random() * 1000) + 1;
  const url = `http://localhost:8081/api/v2/user/${userId}/payment-detail-history`;

  const params = {
    headers: {
      'Content-Type': 'application/json',
    },
    tags: {
      name: 'HTTPPaymentHistory',
    },
  };

  const startTime = new Date();
  const res = http.get(url, params);
  const duration = new Date() - startTime;

  // 커스텀 메트릭 기록
  requestCounter.add(1);
  responseTrend.add(duration);

  // 응답 검증
  const checkResult = check(res, {
    'status is 200': (r) => r.status === 200,
    'response time < 500ms': (r) => r.timings.duration < 500,
    'response time < 1000ms': (r) => r.timings.duration < 1000,
    'response has data': (r) => r.body && r.body.length > 0,
    'no server errors': (r) => r.status < 500,
  });

  // 성공/실패율 기록
  if (checkResult) {
    successRate.add(1);
    errorRate.add(0);
  } else {
    successRate.add(0);
    errorRate.add(1);
  }

  sleep(1);
}