import http from 'k6/http';
import { check, sleep } from 'k6';

// 618명의 유저 ID
const USER_IDS = Array.from({ length: 3080 }, (_, i) => i + 1);

export const options = {
    scenarios: {
        ticketing_open: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                { duration: '5s', target: 3080 },
                { duration: '30s', target: 3080 },
                { duration: '10s', target: 0 },
            ],
        },
    },
};

export default function () {
    // 랜덤 유저 선택
    const userId = USER_IDS[Math.floor(Math.random() * USER_IDS.length)];

    const payload = JSON.stringify({
        userId: userId,
    });

    const params = {
        headers: {
            'Content-Type': 'application/json',
        },
    };

    // 토큰 발급 요청
    const response = http.post('http://localhost:8082/api/v1/queue/tokens', payload, params);

    check(response, {
        'status is 201': (r) => r.status === 201,
        'has data': (r) => {
            try {
                const body = JSON.parse(r.body);
                return body.data !== undefined;
            } catch {
                return false;
            }
        },
        'response time < 1s': (r) => r.timings.duration < 1000,
    });

    // 1~2초 랜덤 대기 (실제 유저처럼)
    sleep(Math.random() + 1);
}