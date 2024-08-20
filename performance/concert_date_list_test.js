import http from 'k6/http';
import { check, sleep } from 'k6';
import { randomIntBetween } from 'https://jslib.k6.io/k6-utils/1.2.0/index.js';

export const options = {
    stages: [
        { duration: '30s', target: 100 },
        { duration: '1m', target: 1000 },
        { duration: '1m', target: 3000 },
        { duration: '1m', target: 6000 },
        { duration: '1m', target: 0 },
    ],
    thresholds: {
        http_req_duration: ['p(95)<1000'],
        http_req_failed: ['rate<0.01'],
    },
};

export default function () {
    // 1단계: 토큰 생성
    const userId = randomIntBetween(1, 6000);
    const tokenPayload = JSON.stringify({ userId: userId });
    const tokenRes = http.post('http://localhost:8080/queue/tokens', tokenPayload, {
        headers: { 'Content-Type': 'application/json' },
    });

    const tokenCheck = check(tokenRes, {
        '토큰 생성 상태가 200입니다': (r) => r.status === 200,
        '토큰이 존재합니다': (r) => {
            const body = r.json();
            return body && body.data && typeof body.data.token === 'string';
        },
    });

    if (!tokenCheck) {
        console.error('토큰 생성 실패:', tokenRes.status, tokenRes.body);
        return;
    }

    const token = tokenRes.json().data.token;

    // 2단계: 토큰 상태 확인
    let isActive = false;
    let attempts = 0;
    const maxAttempts = 12; // 최대 시도 횟수 (총 2분)

    while (!isActive && attempts < maxAttempts) {
        sleep(10); // 토큰 상태 확인 전 10초 대기
        attempts++;

        const statusRes = http.get(`http://localhost:8080/queue/tokens/status/${userId}`, {
            headers: { 'Content-Type': 'application/json' },
        });

        const statusCheck = check(statusRes, {
            '상태 확인이 성공했습니다': (r) => r.status === 200,
            '토큰 정보가 존재합니다': (r) => {
                const body = r.json();
                return body && body.data && body.data.status;
            },
        });

        if (statusCheck) {
            const tokenInfo = statusRes.json().data;
            if (tokenInfo.status === 'ACTIVE') {
                isActive = true;
            } else {
                console.log(`토큰 상태: ${tokenInfo.status}, 대기열 순서: ${tokenInfo.queueOrder}, 남은 시간: ${tokenInfo.remainingTime}`);
            }
        }
    }

    if (!isActive) {
        console.error('제한 시간 내에 토큰이 활성화되지 않았습니다');
        return;
    }

    // 3단계: 콘서트 목록 조회
    const concertListRes = http.get('http://localhost:8080/concerts', {
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json',
        },
    });

    const concertListCheck = check(concertListRes, {
        '콘서트 목록 상태가 200입니다': (r) => r.status === 200,
        '콘서트 목록 데이터가 존재합니다': (r) => {
            const body = r.json();
            return body && body.data && Array.isArray(body.data.concerts) && body.data.concerts.length > 0;
        },
    });

    if (!concertListCheck) {
        console.error('콘서트 목록 조회 실패:', concertListRes.status, concertListRes.body);
        return;
    }

    const concerts = concertListRes.json().data.concerts;
    const firstConcertId = concerts[0].id;

    // 4단계: 첫 번째 콘서트의 예약 가능한 날짜 조회
    const availableDatesRes = http.get(`http://localhost:8080/concerts/${firstConcertId}/available-dates`, {
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json',
        },
    });

    check(availableDatesRes, {
        '예약 가능한 날짜 조회 상태가 200입니다': (r) => r.status === 200,
        '예약 가능한 날짜 데이터가 존재합니다': (r) => {
            const body = r.json();
            return body && body.data && Array.isArray(body.data.concerts);
        },
    });

    if (availableDatesRes.status !== 200) {
        console.error('가능한 날짜 조회 실패:', availableDatesRes.status, availableDatesRes.body);
    }

    sleep(randomIntBetween(1, 5));  // 사용자 행동을 시뮬레이션하기 위한 1~5초 사이의 랜덤 대기
}