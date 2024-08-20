import http from 'k6/http';
import { check, sleep } from 'k6';
import { randomIntBetween } from 'https://jslib.k6.io/k6-utils/1.2.0/index.js';

export const options = {
    stages: [
        { duration: '30s', target: 100 },
        { duration: '1m', target: 1000 },
        { duration: '1m', target: 2000 },
        { duration: '1m', target: 3000 },
        { duration: '1m', target: 4000 },
        { duration: '1m', target: 4000 },
        { duration: '1m', target: 100 },
    ],
    thresholds: {
        http_req_duration: ['p(95)<3000'],
        http_req_failed: ['rate<0.01'],
    },
};

const BASE_URL = 'http://localhost:8080';
const CONCERT_ID = 1;
const CONCERT_OPTION_ID = 1;
const TOTAL_SEATS = 100;

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

    sleep(randomIntBetween(1, 3));

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

    sleep(randomIntBetween(1, 3));

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

    sleep(randomIntBetween(1, 3));


    // 5. 콘서트 좌석 조회
    const seatsRes = http.get(`${BASE_URL}/concerts/${CONCERT_OPTION_ID}/available-seats`, {
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json',
        },
    });

    const seatsCheck = check(seatsRes, {
        '좌석 조회 성공': (r) => r.status === 200,
    });

    if (!seatsCheck) {
        console.error('좌석 조회 실패:', seatsRes.status, seatsRes.body);
        return;
    }

    const availableSeats = seatsRes.json().data.seats;

    if (availableSeats.length === 0) {
        console.log('예약 가능한 좌석 없음');
        return;
    }

    sleep(randomIntBetween(1, 3));

    // 6. 좌석 예약
    const randomSeat = availableSeats[Math.floor(Math.random() * availableSeats.length)];
    const reservationPayload = JSON.stringify({
        seatId: randomSeat.seatId,
        userId: userId
    });

    const reservationRes = http.post(`${BASE_URL}/concerts/reserve-seat`, reservationPayload, {
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json',
        },
    });

    check(reservationRes, {
        '예약 성공 또는 이미 예약됨': (r) => r.status === 200 || r.status === 409,
    });

    if (reservationRes.status === 200) {
        console.log(`좌석 예약 성공: 사용자 ${userId}, 좌석 ${randomSeat.seatNo}`);
    } else if (reservationRes.status === 409) {
        console.log(`좌석 이미 예약됨: 사용자 ${userId}, 좌석 ${randomSeat.seatNo}`);
    } else {
        console.error(`예약 실패: 상태 코드 ${reservationRes.status}, 사용자 ${userId}, 좌석 ${randomSeat.seatNo}`);
    }

    sleep(randomIntBetween(1, 3));
}