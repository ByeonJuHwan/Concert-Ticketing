import http from 'k6/http';
import { check, sleep } from 'k6';
import { randomIntBetween } from 'https://jslib.k6.io/k6-utils/1.2.0/index.js';

export const options = {
    stages: [
        { duration: '1m', target: 1000 },  // 0 to 1000 in 1m
        { duration: '1m', target: 2000 },  // 1000 to 2000 in 1m
        { duration: '1m', target: 3000 },  // 2000 to 3000 in 1m
        { duration: '1m', target: 4000 },  // 3000 to 4000 in 1m
        { duration: '1m', target: 5000 },  // 4000 to 5000 in 1m
        { duration: '1m', target: 6000 },  // 5000 to 6000 in 1m
        { duration: '1m', target: 7000 },  // 6000 to 7000 in 1m
        { duration: '1m', target: 8000 },  // 7000 to 8000 in 1m
        { duration: '1m', target: 9000 },  // 8000 to 9000 in 1m
        { duration: '1m', target: 10000 }, // 9000 to 10000 in 1m
        { duration: '1m', target: 10000 }, // Stay at 10000 for 1m
        { duration: '1m', target: 0 },     // Scale down to 0 in 1m
    ],
    thresholds: {
        http_req_duration: ['p(95)<1000'], // 95% of requests should be below 1s
        http_req_failed: ['rate<0.01'],    // Less than 1% can fail
    },
};

export default function () {
    const userId = randomIntBetween(1, 1000000);  // Assume up to 1 million unique users

    const payload = JSON.stringify({ userId: userId });

    const res = http.post('http://localhost:8080/points/charge', payload, {
        headers: { 'Content-Type': 'application/json' },
    });

    check(res, {
        'status is 200': (r) => r.status === 200,
        'token is present': (r) => r.json('data.token') !== null,
    });

    sleep(randomIntBetween(0.5, 2));  // Random sleep between 0.5-2 seconds
}