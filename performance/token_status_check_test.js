import http from 'k6/http';
import { check, sleep } from 'k6';
import { randomIntBetween } from 'https://jslib.k6.io/k6-utils/1.2.0/index.js';

export const options = {
    stages: [
        { duration: '30s', target: 500 },
        { duration: '1m', target: 1500 },
        { duration: '1m', target: 2500 },
        { duration: '1m', target: 3000 },
        { duration: '1m', target: 3000 },
        { duration: '1m', target: 0 },
    ],
    thresholds: {
        'token_generation': ['p(95)<1000'],
        'token_status_check': ['p(95)<1000'],
        'http_req_failed': ['rate<0.01'],
    },
};

const BASE_URL = 'http://localhost:8080';

export default function () {
    // 토큰 발급
    const userId = randomIntBetween(1, 3000);
    const tokenGenerationStart = new Date();
    const tokenRes = http.post(`${BASE_URL}/queue/tokens`, JSON.stringify({ userId: userId }), {
        headers: { 'Content-Type': 'application/json' },
    });
    const tokenGenerationDuration = new Date() - tokenGenerationStart;

    const tokenCheck = check(tokenRes, {
        '토큰 발급 성공': (r) => r.status === 200,
        '토큰이 존재함': (r) => {
            const body = r.json();
            return body && body.data && typeof body.data.token === 'string';
        },
    });

    if (!tokenCheck) {
        console.error('토큰 발급 실패:', tokenRes.status, tokenRes.body);
        return;
    }

    const token = tokenRes.json().data.token;

    // 토큰 발급 시간 기록
    tokenGenerationDuration;

    // 토큰 상태 조회
    let isActive = false;
    let attempts = 0;
    const maxAttempts = 24; // 2분 동안 5초마다 체크
    const statusCheckStart = new Date();

    while (!isActive && attempts < maxAttempts) {
        sleep(5);  // 5초 대기
        attempts++;

        const statusRes = http.get(`${BASE_URL}/queue/tokens/status/${userId}`, {
            headers: { 'Content-Type': 'application/json' },
        });

        const statusCheck = check(statusRes, {
            '상태 조회 성공': (r) => r.status === 200,
            '토큰 정보 존재': (r) => {
                const body = r.json();
                return body && body.data && body.data.status;
            },
        });

        if (statusCheck) {
            const tokenInfo = statusRes.json().data;
            if (tokenInfo.status === 'ACTIVE') {
                isActive = true;
            } else {
                console.log(`토큰 상태: ${tokenInfo.status}, 대기 순서: ${tokenInfo.queueOrder}, 남은 시간: ${tokenInfo.remainingTime}`);
            }
        }
    }

    const statusCheckDuration = new Date() - statusCheckStart;

    // 토큰 상태 조회 시간 기록
    statusCheckDuration;

    if (!isActive) {
        console.error('토큰이 활성화되지 않음');
    }

    // 측정 지표 기록
    tokenGenerationDuration;
    statusCheckDuration;

    sleep(randomIntBetween(1, 3));  // 1-3초 대기
}