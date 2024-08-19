import http from 'k6/http';
import { check, sleep } from 'k6';
import { randomIntBetween } from 'https://jslib.k6.io/k6-utils/1.2.0/index.js';

export const options = {
    stages: [
        { duration: '30s', target: 100 },
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
    // Step 1: Generate Token
    const userId = randomIntBetween(1, 2000);
    const tokenPayload = JSON.stringify({ userId: userId });
    const tokenRes = http.post('http://localhost:8080/queue/tokens', tokenPayload, {
        headers: { 'Content-Type': 'application/json' },
    });

    const tokenCheck = check(tokenRes, {
        'token generation status is 200': (r) => r.status === 200,
        'token is present': (r) => {
            const body = r.json();
            return body && body.data && typeof body.data.token === 'string';
        },
    });

    if (!tokenCheck) {
        console.error('Token generation failed:', tokenRes.status, tokenRes.body);
        return;
    }

    const token = tokenRes.json().data.token;

    // Step 2: Check Token Status
    let isActive = false;
    let attempts = 0;
    const maxAttempts = 12; // Maximum number of attempts (1 minute total)

    while (!isActive && attempts < maxAttempts) {
        sleep(10); // Wait for 10 seconds before checking token status
        attempts++;

        const statusRes = http.get(`http://localhost:8080/queue/tokens/status/${userId}`, {
            headers: { 'Content-Type': 'application/json' },
        });

        const statusCheck = check(statusRes, {
            'status check is successful': (r) => r.status === 200,
            'token info is present': (r) => {
                const body = r.json();
                return body && body.data && body.data.status;
            },
        });

        if (statusCheck) {
            const tokenInfo = statusRes.json().data;
            if (tokenInfo.status === 'ACTIVE') {
                isActive = true;
            } else {
                console.log(`Token status: ${tokenInfo.status}, Queue Order: ${tokenInfo.queueOrder}, Remaining Time: ${tokenInfo.remainingTime}`);
            }
        }
    }

    if (!isActive) {
        console.error('Token did not become active within the time limit');
        return;
    }

    // Step 3: Retrieve Concert List
    const concertRes = http.get('http://localhost:8080/concerts', {
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json',
        },
    });

    check(concertRes, {
        'concert list status is 200': (r) => r.status === 200,
        'concert list has data': (r) => {
            const body = r.json();
            return body && body.data && Array.isArray(body.data.concerts);
        },
    });

    if (concertRes.status !== 200) {
        console.error('Concert list retrieval failed:', concertRes.status, concertRes.body);
    }

    sleep(randomIntBetween(5, 15));
}