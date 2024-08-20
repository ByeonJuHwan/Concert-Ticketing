# 부하테스트

부하 테스트는 시스템이 예상되는 사용자 부하를 처리할 수 있는지 확인하는 성능 테스트 입니다. 
콘서트 좌석 예약 프로젝트의 각 API 에 대한 부하 테스트 시나리오를 작성하고, 그에 따른 테스트 결과를 분석해 보겠습니다.

**테스트 대상 API**

- 포인트 충전 API
- 포인트 조회 API
- 토큰 발급 API
- 토큰 상태 조회 API
- 콘서트 목록 조회 API
- 콘서트 예약가능 날짜 조회 API
- 콘서트 예약 가능 좌석 조회 API
- 예약 API
- 결제 API

**테스트 환경**

- 맥북 M1 Air
- 스프링부트 3.3.1
- Mariadb:latest 이미지

**테스트 툴** 
 - k6
 - 프로메테우스
 - 그라파나

## 각 테스트 시나리오 및 테스트 결과

콘서트 예약이라는 조건에 맞게 평상시에는 API 요청건이 낮다가 콘서트 예약 오픈 직전에 급격하게 사용자가 몰리는 상황을 가정하여 테스트 하겠습니다.

### 포인트 충전 API

#### 가정
- 충전 금액 : 10,000원 ~ 300,000원 사이로 다양한 금액으로 요청

#### 시나리오

1. 준비단계 (30초) :
   - 일반적인 사용 상황 동시 사용자 50명으로 시작
2. 급격한 부하 증가 (1분) :
    - 티켓 오픈 직전 상황이며 많은 사용자들이 포인트 충전을 시도합니다
    - 동시 사용자가 50명 -> 1000명으로 급격히 증가합니다.
3. 피크 부하 (3분) :
    - 티켓 오픈 중 최대 부하 상황입니다
    - 1000명의 동시 사용자가 지속적으로 포인트를 충전합니다.
4. 부하 감소(1분) :
    - 티켓 오픈 후 포인트충전을 마친 사용자들이 점점 줄어듭니다.
    - 동시 사용자가 1000명 -> 200명으로 점진적으로 감소합니다.
   
#### 성공기준 및 목표
- 평균 응답 시간 : 1초
- 95퍼센타일 응답 시간 : 3초이하
- 99퍼센타일 응답 시간 : 5초이하
- 오류율 0.1 % 미만
- 300 TPS 이상
- 

#### 테스트 스크립트
```js
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
        http_req_duration: ['p(95)<3000', 'p(99)<5000', 'avg<1000'],  // 응답 시간 임계값
        http_req_failed: ['rate<0.001'],  // 오류율 임계값
    },
};

export default function () {
    const userId = Math.floor(Math.random() * 1000) + 1;  // 더 많은 사용자 ID 범위
    const amount = Math.floor(Math.random() * 290000) + 10000;  // 10000 ~ 300000 원

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

    // 더 짧은 sleep 시간으로 더 높은 부하 생성
    sleep(Math.random() * 0.5);
}
```

#### 테스트 결과 분석

![](https://velog.velcdn.com/images/asdcz11/post/5a63f9cb-f2b1-4f7a-96ec-d0ff5578c4e5/image.png)
![](https://velog.velcdn.com/images/asdcz11/post/cb30be61-b64f-4134-9222-8f79ab0ccd1e/image.webp)

- 응답시간
  - 평균 응답 시간 : 925.68ms
  - 중간값 응답 시간 : 1.04ms
  - 90퍼 센타일 응답시간 : 1.58ms
  - 95퍼 센타일 응답시간 : 1.74ms
  - 최대 응답시간 4.44ms

- 처리량
  - 약 620 TPS
  - 총 처리된 요청 수 : 354,183

- CPU 사용률
  - 최대 82.9%
  - 평균 43.2%

#### 정리

평균 응답시간은 목표시간인 1초 이내를 달성하였고, TPS 도 목표치인 300 TPS 보다 높은 수치를 달성했습니다.

CPU 사용량 안정적으로 유지도고 메모리 사용량은 저전반적으로 낮게 유지된걸로 보아 약 1000명의 유저가 포인트 충전 API 를 사용하는데 무리 없이 사용할 수 있습니다.

---

### 포인트 조회 API

#### 시나리오

#### 테스트 스크립트

```js

```

#### 정리하며...

---

### 대기열 토큰 발급 API

#### 가정

- 콘서트 예약이 오픈하면 1000명이 동시에 토큰발급을 요청합니다.
- 이후 1분마다 1000명씩 증가하여 10뒤에는 1만명의 토큰발급 동시요청이 들어오게 합니다.
- 1만명이된 시점부터는 1분간 유지하며 애플리케이션 응답을 관찰합니다

#### 시나리오

1. 단계적 부하 증가 (총 10분)
    - 매 1분마다 1,000명씩 사용자 수 증가
    - 1분: 1,000명 → 10분: 10,000명
2. 최대 부하 유지 (1분)
    - 10,000명의 가상 사용자가 지속적으로 토큰 발급 시도
3. 부하 감소 (1분)
    - 부하를 점진적으로 0으로 감소

#### 성공기준

- 95 퍼 센타일 응답시간 1초 이내
- 오류율 1% 미만

#### 테스트 스크립트
```js
import http from 'k6/http';
import { check, sleep } from 'k6';
import { randomIntBetween } from 'https://jslib.k6.io/k6-utils/1.2.0/index.js';

export const options = {
  stages: [
    { duration: '1m', target: 1000 },  
    { duration: '1m', target: 2000 },  
    { duration: '1m', target: 3000 },  
    { duration: '1m', target: 4000 },  
    { duration: '1m', target: 5000 },  
    { duration: '1m', target: 6000 },  
    { duration: '1m', target: 7000 },  
    { duration: '1m', target: 8000 },  
    { duration: '1m', target: 9000 },  
    { duration: '1m', target: 10000 }, 
    { duration: '1m', target: 10000 }, 
    { duration: '1m', target: 0 }, 
  ],
  thresholds: {
    http_req_duration: ['p(95)<1000'], 
    http_req_failed: ['rate<0.01'],
  },
};

export default function () {
  const userId = randomIntBetween(1, 10000);
  const payload = JSON.stringify({ userId: userId });
  
  const res = http.post('http://localhost:8080/queue/tokens', payload, {
    headers: { 'Content-Type': 'application/json' },
  });

  check(res, {
    'status is 200': (r) => r.status === 200,
    'response has valid structure': (r) => {
      try {
        const body = r.json();
        return body && typeof body === 'object';
      } catch (e) {
        console.error('Failed to parse response body:', e);
        return false;
      }
    },
    'data field is present': (r) => {
      const body = r.json();
      return body && body.hasOwnProperty('data');
    },
    'token is present and valid': (r) => {
      const body = r.json();
      return body && 
             body.data && 
             typeof body.data.token === 'string' && 
             body.data.token.length > 0;
    },
  });

  // 실패시 ERROR 로그 출력
  if (res.status !== 200) {
    console.error(`Request failed with status ${res.status}:`, res.body);
  }

  sleep(randomIntBetween(0.1, 0.5));  // 0.1-0.5 초 사이로 랜덤하게 쓰레드 정지
}
```

#### 테스트 결과 분석 (1 만명)

실제 테스트 해본결과 6분정도 지난시점 즉, 동시접속자 6000~7000명이 토큰 발행 API 요청을 보낼 시
서버가 이를 처리하지 못하고 전부 Error 를 반환했습니다.

- 평균 응답시간 248.64ms
- 최대 응답 시간 12.04s (응답시간이 매우 높습니다)
- 테스트 중단 (예정된 12분을 채우지못하고 중단되었습니다)

**그라파나 대시보드 분석**
![](https://velog.velcdn.com/images/asdcz11/post/e1762414-ca2a-4aac-b2b4-818f283c1908/image.webp)

- CPU 사용량 최대 99.4% 까지 증가
- 프로세스 오픈파일 최대치 도달

현재 시스템이 10,000명의 부하를 견디지 못하고 있습니다. 
그러면 현재 서버가 몇 명까지의 사용자 요청을 받아 낼 수 있는지 
6,000명의 사용자가 동시에 요청을 보내는 테스트를 진행해 보도록 하겠습니다.

기존의 테스트 스크립트에서 6000 명부터 점점 사용자수를 줄여가면서 적절한 CPU 사용량을 찾아보면서 적정 인원을 확인해 보겠습니다.

- 6000 명 CPU 사용량 95 % -> 테스트 실패
- 5000 명 CPU 사용량 90 %
- 3000 명 ~ 4000명 CPU 사용량 70 ~ 80 %
- 1800 명 55%

**1800명 부하테스트 결과**

![](https://velog.velcdn.com/images/asdcz11/post/4484d76a-16a6-4b45-bb2d-3b612197f3be/image.png)
![](https://velog.velcdn.com/images/asdcz11/post/b1531bd9-3613-403c-9790-e94997e2b95d/image.png)
- 평균응답 : 4.04ms 
- 95% 센타일 12.68ms
- 오류율 0%
- CPU 사용율 1800명 동시요청시 60% 대

위 결과로 보아 약 3000명 이상의 요청이 들어와도 서버로 들어와도 cpu 사용량이 80프로정도로 유지 가능하지만,
어디까지나 예측값이기 때문에 2000명 이상의 요청이 들어오면 스케일 아웃을 고려해 볼 수 있을 것 같습니다.

### 토큰 조회 API

**가정**

- 최대 동시 사용자수 : 3,000명

**테스트 시나리오**

1. 준비 단계 (30s)
   - 0명에서 500명의 사용자로 점진적 증가
2. 부하 증가 (3m)
    - 500 -> 3000명 으로 1분당 1000명씩 증가
3. 최대 부가 (1m)
    - 3000 명의 사용자가 지속적으로 토큰 발급 및 상태 조회
4. 부하 감소 (1m)
    - 3000명 -> 0명으로 사용자수 감소

#### 테스트 스크립트
```js
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
        'token_generation': ['p(95)<1000'],  // 토큰 발급 95% 1초 이내
        'token_status_check': ['p(95)<1000'], // 토큰 상태 조회 95% 1초 이내
        'http_req_failed': ['rate<0.01'],    // 전체 오류율 1% 미만
    },
};

const BASE_URL = 'http://localhost:8080';

export default function () {
    // 토큰 발급
    const userId = randomIntBetween(1, 1000000);
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
```

#### 테스트 결과 분석

---

### 콘서트 목록 조회 API

예약가능한 콘서트 목록이 100개정도 있다고 가정하고 테스트를 진행해 보겠습니다.

**가정**
- 100명의 유저부터 콘서트 목록 조회를 시작해서 점차 증가합니다
- 1분안에 1000명으로 증가하며 콘서트 조회를 합니다
- 2분안에 2000명으로 사용자가 2배 증가하면 콘서트 조회를 합니다.

**시나리오**
1. 준비단계 (30s)
    - 평소과 같은 트래픽으로 100명의 유저가 콘서트 조회를 합니다
2. 점진적 부하(2m)
    - 1분당 1000명씩 부하가 들어옵니다 총 2분 -> 2000명이 동시에 토큰발급, 콘서트 조회를 합니다
3. 정리단계 (1m)
    - 1분간 2000명 ->0명으로 사용자가 줄어들며 부하가 점차 감소합니다


**성공기준**
- 평균 응답 시간 : 500ms 이하
- 95% 센타일 응답 시간 : 1초 이하
- 오류율 1% 미만
- CPU 사용률 80 % 이하

#### 테스트 스크립트
```js
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
    // 토큰 발급 API
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
        console.error('토큰 생성 실패:', tokenRes.status, tokenRes.body);
        return;
    }

    const token = tokenRes.json().data.token;

    // 토큰 상태 조회 API
    let isActive = false;
    let attempts = 0;
    const maxAttempts = 12;

    while (!isActive && attempts < maxAttempts) {
        sleep(10);
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
        console.error('WatingToken -> ActiveToken 변환 오류 (ActiveToken 상태 변경 오류)');
        return;
    }

    // 콘서트 목록 조회 API
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
        console.error('콘서트 목록 조회 실패:', concertRes.status, concertRes.body);
    }

    sleep(randomIntBetween(5, 15));
}
```

#### 2000명 테스트 결과 분석

![](https://velog.velcdn.com/images/asdcz11/post/1169e519-4f2d-4bbf-b4c2-b1a2f02a4980/image.png)
![](https://velog.velcdn.com/images/asdcz11/post/85e02423-8b5d-4fe3-a5ba-c18fd724abad/image.png)

- CPU 사용률: 평균 24.1%, 최대 34.4%
- 평균 응답 시간 : 3.47ms
- 95퍼센타일 응답 시간 : 6.18ms
- 초당 132.72개의 요청을 처리

2000명의 사용자가 동시접속해 콘서트 목록조회 테스트 결과 여유있게 트래픽 처리가 가능했습니다.

이번엔 6000명 동시 접속시로 테스트해 보겠습니다.

**테스트 결과**

- CPU 사용률 최대 약 70%
- 평균 응답 시간 : 3.77ms
- 95퍼센타일 : 6.64ms
- 초당 처리량 : 420.08 개

cpu 사용량을 70~80% 사이로 안정정적으로 유지하려면 6,000명 ~ 7,000명 사이의 트래픽은 안정적적으로 처리가능합니다.

---


### 콘서트 예약 가능 날짜 조회 API

콘서트 목록조회에서 2000명 트래픽은 여유롭게 처리했으므로 6,000명 부하테스트로 테스트 해 보겠습니다.

**가정**
- 100명의 유저부터 콘서트 목록 조회를 시작해서 점차 증가합니다
- 1분당 3000명씩 증가하며 예약가능 콘서트 날짜를 조회 합니다
- 6000명 까지 증가하며 1분간 0명으로 부하가 줄어듭니다

**시나리오**
1. 준비단계 (30s) 
    - 평소과 같은 트래픽으로 100명의 유저가 콘서트 조회를 합니다
2. 점진적 부하(3m)
    - 1분당 3000명씩 부하가 들어옵니다
3. 정리단계 (1m)
    - 1분간 6000명 ->0명으로 사용자가 줄어들며 부하가 점차 감소합니다

 **성공기준**
- 평균 응답 시간 : 500ms 이하
- 95% 센타일 응답 시간 : 1초 이하
- 오류율 1% 미만
- CPU 사용률 80 % 이하

#### 테스트 스크립트

```js
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
    const userId = randomIntBetween(1, 5000);
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
```

#### 테스트 분석 결과

![](https://velog.velcdn.com/images/asdcz11/post/69aa66ee-3a24-4f50-aae3-dec16a1f94da/image.png)
![](https://velog.velcdn.com/images/asdcz11/post/12b1cbe7-811f-44dc-a05a-7e0b20cb1d2c/image.png)

- 평균 응답 : 14.75 ms
- 95 센티넬 : 78.66ms
- 최대 cpu 사용률 90%

콘서트 예약 가능한 날짜조회 API 도 콘서트 목록조회 API 와 마찬가지로 인덱스 및 캐싱전략이 되어있어서 많은 트래픽이 몰려도
빠른 응답을 할 수 있습니다.

하지만 CPU 사용률이 90 % 까지 올라가며 모든 기존 10개 -> 20개로 늘린 쓰레드풀도 전부 사용하고 있습니다.

이때에는 그라파나에서 알림을 받아서 쓰레드 풀을 더 증가시키는 방법, 혹은 스케일 아웃 / 업 도 생각해 볼 수 있습니다.

### 콘서트 예약가능 좌석 조회 API

이번에는 캐싱이 적용되기전 후 부하테스트 결과를 비교해 보겠습니다.

현재 좌석조회 API 에는 인덱스는 적용되어있지만, 캐싱이 적용되어 있지않습니다.

이때 기존 6,000명보다 조금 낮은 4,000명의 사용자료 부하테스트 시 그 차이를 확인해 보겠습니다.

**가정**
- 최대 동시 사웅자수 : 4000명
- 테스트 대상 콘서트 ID : 1
- 테스트 대상 콘서트 옵션 ID : 1
- 좌석 개수 : 100개

**시나리오**

1. 준비 단계 (30s)
   - 0에서 100명으로 사용자 수 증가
   - 각 사용자는 전체 프로세스 수행
2. 점진적 부하 (4m)
   - 1분당 1000명씩 증가
   - 4000명까지 점진적 증가
3. 최대 부하 유지(1m)
   - 4000명 의 사용자가 지속적으로 예약 시도
4. 점진적 부하 감소 (1m)
    - 4000명 -> 0명 점진적으로 사용자수 감소

#### 테스트 스크립트

```js
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
```

#### 테스트 결과 분석 (캐싱 전후 비교)

**콘서트 좌석 조회 캐싱 전** 

![](https://velog.velcdn.com/images/asdcz11/post/889fd35d-4f49-4202-a15b-242cfc3847f9/image.png)
![](https://velog.velcdn.com/images/asdcz11/post/11c11827-9e9c-427c-9f9e-8d55a880ea71/image.webp)

- 초당 요청 처리량 : 491 개
- 평균 응답 시간 : 10.05ms
- 95 % 센타일 시간 : 29.3ms
- CPU 최대 사용률 : 78%

전체적으로 빠른 요청 처리가 가능하지만 위 두 테스트에서는 6,000 명 부하테스트에서 CPU 사용량이 70~80% 였던 반면,

현재 좌석조회 쿼리는 DB 에 지속적으로 요청을 보내다 보니 CPU 사용량이 낮은 부하수에도 급격히 상승하고 있습니다.

**콘서트 좌석 조회 캐싱 후**

![](https://velog.velcdn.com/images/asdcz11/post/e1818401-515b-4e06-a7ba-836427b969d0/image.png)
![](https://velog.velcdn.com/images/asdcz11/post/21467b8f-3989-4ebc-af6d-f148d0493e2e/image.png)

- 초당 요청 처리량 : 487.4 개
- 평균 응답시간 : 4.7 ms (53.2% 개선)
- 95 % 센타일 시간 : 6,76ms (76.9% 개선)
- cpu 최대 사용률 : 70%

**위 테스트 결과로 대용량 트래픽 발생시 병목이 조회쿼리에 병목이 발생한다면 캐싱 전략을 통해서 cpu 사용률을 낮추고, 응답시간의 개선을 가져갈 수 있습니다.**

---

### 콘서트 예약 API

**가정**
- 총 좌석 수 : 100개
- 최대 동시 사용자 수 : 4000명
- 부하를 높이기위해 좌석을 적게, 사용자수를 많게 설정

**시나리오**
1. 준비 단계 (30s)
   - 100명의 사용자가 동시에 랜덤한 좌석 예약 시도
2. 부하 증가 단계 (4m)
    - 100명 -> 1분당 1000명씩 점진적 증가
    - 각 사용자는 랜덤한 좌석 번호로 예약 시도
3. 최대 부하 단계 (1m)
    - 4000명의 사용자가 지속적으로 예약 시도
    - 이미 예약된 좌석에 대한 재예약 시도 포함
4. 부가 감소 단계 (1m)
    - 4000명 -> 100명으로 점진적 감소

**성공기준**

- 평균 응답 시간 : 1초 이하
- 95% 센타일 응답시간 : 3초 이하
- 오류율 1% 미만

#### 테스트 스크립트

```js
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
```

#### 테스트 결과 분석

![](https://velog.velcdn.com/images/asdcz11/post/617cbed8-92b6-4a43-8d97-6f9a0619fa76/image.png)

![](https://velog.velcdn.com/images/asdcz11/post/e2e18e32-2e9e-4660-8128-5353f5112df2/image.png)

- 평균 응답 시간 : 4.49ms
- 95% 센타일 응답 시간 : 10.1ms
- 초당 요청 처리량 645.2 요청
- 최대 CPU 사용량 : 75%

좌석 예약의 경우 비관적 락이 적용되어 있고 로그 확인 결과 Error 없이 정상적으로 100개의 좌석이 예약되고, 이후 예약 요청은 409 응답코드를 반환한걸로보아
정상적으로 동시성 이슈에 대해서 처리하고 있습니다.

---

### 결제 API

#### 가정

#### 시나리오

---

## 정리하며...

콘서트 좌석 예약 프로젝트를 하면서 각 API 를 테스트 해보면서 몇명의 사용자 즉 트래픽을 받아낼 수 있는지 테스트해 보았습니다.

- CPU 사용률 80 % 아래
- 응답률 1초 이하
- 오류율 1% 이하

공통된 기준을 위와 같이 잡고 테스트를 해볼 수 있어서 현재 내 서버 애플리케이션이 얼마정도의 트래픽을 받아낼수 있는지
확인해 볼수 있는 기회가 되었습니다.