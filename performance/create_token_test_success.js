import http from 'k6/http';
import { check, sleep } from 'k6';
import { randomIntBetween } from 'https://jslib.k6.io/k6-utils/1.2.0/index.js';

export const options = {
    stages: [
        { duration: '30s', target: 100 },
        { duration: '1m', target: 500 },
        { duration: '1m', target: 1000 },
        { duration: '1m', target: 1800 },
        { duration: '30s', target: 900 },
        { duration: '30s', target: 0 },
    ],
    thresholds: {
        http_req_duration: ['p(95)<1000'],
        http_req_failed: ['rate<0.01'],
    },
};

export default function () {
    const userId = randomIntBetween(1, 1800);

    const payload = JSON.stringify({ userId: userId });

    const res = http.post('http://localhost:8080/queue/tokens', payload, {
        headers: { 'Content-Type': 'application/json' },
    });

    check(res, {
        'status is 200': (r) => r.status === 200,
        'token is present': (r) => {
            const body = r.json();
            return body && body.data && typeof body.data.token === 'string' && body.data.token.length > 0;
        },
    });

    sleep(randomIntBetween(1, 3));
}