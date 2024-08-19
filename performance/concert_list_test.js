import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    stages: [
        { duration: '1m', target: 100 },
        { duration: '1m', target: 1000 },
        { duration: '1m', target: 2000 },
        { duration: '1m', target: 0 },
    ],
    thresholds: {
        http_req_duration: ['p(95)<1000'],
        http_req_failed: ['rate<0.01'],
    },
};

export default function () {
    const params = {
        headers: {
            'Content-Type': 'application/json',
        },
    };

    const res = http.get('http://localhost:8080/concerts', params);

    check(res, {
        'status is 200': (r) => r.status === 200,
        'response has concerts data': (r) => {
            try {
                const body = r.json();
                return body && body.data && Array.isArray(body.data.concerts);
            } catch (e) {
                console.error('Failed to parse response:', e);
                return false;
            }
        },
    });

    if (res.status !== 200) {
        console.error(`Request failed with status ${res.status}:`, res.body);
    }

    sleep(Math.random() * 4 + 1);
}