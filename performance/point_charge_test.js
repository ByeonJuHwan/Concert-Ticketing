import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    stages: [
        { duration: '30s', target: 50 },
        { duration: '1m', target: 1000 },
        { duration: '3m', target: 1000 },
        { duration: '1m', target: 200 },
    ],
    thresholds: {
        http_req_duration: ['p(95)<3000', 'p(99)<5000', 'avg<1000'],
        http_req_failed: ['rate<0.001'],
    },
};

export default function () {
    const userId = Math.floor(Math.random() * 1000) + 1;
    const amount = Math.floor(Math.random() * 290000) + 10000;

    const payload = JSON.stringify({
        userId: userId,
        amount: amount,
    });

    const params = {
        headers: {
            'Content-Type': 'application/json',
        },
    };

    const res = http.put('http://localhost:8080/points/charge', payload, params);

    check(res, {
        'is status 200': (r) => r.status === 200,
        'transaction time OK': (r) => r.timings.duration < 1000,
    });

    sleep(Math.random() * 0.5);
}