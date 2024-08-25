import http from 'k6/http';
import { check, sleep } from 'k6';
import { randomIntBetween } from 'https://jslib.k6.io/k6-utils/1.2.0/index.js';

export const options = {
    stages: [
        { duration: '30s', target: 100 },
        { duration: '1m', target: 1000 },
        { duration: '3m', target: 6000 },
        { duration: '30s', target: 0 },
    ],
    thresholds: {
        http_req_duration: ['p(95)<1000'],
        http_req_failed: ['rate<0.01'],
    },
};

export default function () {
    const userId = randomIntBetween(1, 6000);  // Assume up to 1 million unique users

    const res = http.get(`http://localhost:8080/points/current/${userId}`);

    check(res, {
        'status is 200': (r) => r.status === 200,
        'currentPoints is present': (r) => r.json('data.currentPoints') !== null,
    });

    sleep(randomIntBetween(1, 5));  // Random sleep between 1-5 seconds
}