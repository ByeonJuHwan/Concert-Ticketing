import http from 'k6/http';
import { check, sleep } from 'k6';
import { randomIntBetween } from 'https://jslib.k6.io/k6-utils/1.2.0/index.js';

export const options = {
    stages: [
        { duration: '30s', target: 100 },
        { duration: '4m', target: 4000 },
        { duration: '1m', target: 4000 },
        { duration: '1m', target: 0 },
    ],
    thresholds: {
        http_req_duration: ['p(95)<3000'],
        http_req_failed: ['rate<0.01'],
    },
};

const BASE_URL = 'http://localhost:8080';
const CONCERT_ID = 1;
const CONCERT_OPTION_ID = 1;

export default function () {
    // 1. 토큰 발급
    const userId = randomIntBetween(1, 6000);
    const tokenPayload = JSON.stringify({ userId: userId });
    const tokenRes = http.post('http://localhost:8080/queue/tokens', tokenPayload, {
        headers: { 'Content-Type': 'application/json' },
    });

    const tokenCheck = check(tokenRes, {
        '토큰 발급 성공': (r) => r.status === 200,
    });

    if (!tokenCheck) {
        console.error('토큰 발급 실패:', tokenRes.status, tokenRes.body);
        return;
    }

    const token = tokenRes.json().data.token;

    // 2. 토큰 상태 조회
    let isActive = false;
    let attempts = 0;
    const maxAttempts = 12; // 2분 동안 10초마다 체크

    while (!isActive && attempts < maxAttempts) {
        sleep(10);
        attempts++;

        const statusRes = http.get(`${BASE_URL}/queue/tokens/status/${userId}`, {
            headers: { 'Content-Type': 'application/json' },
        });

        if (statusRes.json().data.status === 'ACTIVE') {
            isActive = true;
        }
    }

    if (!isActive) {
        console.error('토큰 활성화 실패');
        return;
    }

    sleep(randomIntBetween(1, 5));

    // 3. 콘서트 목록 조회
    const concertListRes = http.get(`${BASE_URL}/concerts`, {
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json',
        },
    });

    check(concertListRes, {
        '콘서트 목록 조회 성공': (r) => r.status === 200,
    });

    sleep(randomIntBetween(1, 5));

    // 4. 콘서트 날짜 조회
    const concertDatesRes = http.get(`${BASE_URL}/concerts/${CONCERT_ID}/available-dates`, {
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json',
        },
    });

    check(concertDatesRes, {
        '콘서트 날짜 조회 성공': (r) => r.status === 200,
    });

    sleep(randomIntBetween(1, 5));

    // 5. 콘서트 좌석 조회
    const seatsRes = http.get(`${BASE_URL}/concerts/${CONCERT_OPTION_ID}/available-seats`, {
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json',
        },
    });

    check(seatsRes, {
        '좌석 조회 성공': (r) => r.status === 200,
    });

    sleep(randomIntBetween(1, 5));
}