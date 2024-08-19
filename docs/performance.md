# 부하테스트

부하 테스트는 시스템이 예상되는 사용자 부하를 처리할 수 있는지 확인하는 성능 테스트 입니다. 
콘서트 좌석 예약 프로젝트의 각 API 에 대한 부하 테스트 시나리오를 작성하고, 그에 따른 테스트 결과를 분석해 보겠습니다.

**테스트 대상 API**

- 포인트 충전 API
- 포인트 조회 API
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
- 평상시 초당 50건의 포인트 충전 요청
- 콘서트 티켓 오픈 직전 초당 1,000건까지 급격히 증가 가능
- 충전 금액은 10,000원에서 300,000원 사이로 다양함

#### 시나리오

1. 준비단계 (30초) :
   - 일반적인 사용 상황 동시 사용자 50명으로 시작
2. 급격한 부하 증가 (1분) :
    - 티켓 오픈 직전 상황이며 많은 사용자들이 포인트 충전을 시도합니다
    - 동시 사용자가 50명 -> 500명으로 급격히 증가합니다.
3. 피크 부하 (3분) :
    - 티켓 오픈 중 최대 부하 상황입니다
    - 500명의 동시 사용자가 지속적으로 포인트를 충전합니다.
4. 부하 감소(1분) :
    - 티켓 오픈 후 포인트충전을 마친 사용자들이 점점 줄어듭니다.
    - 동시 사용자가 500명 -> 100명으로 점진적으로 감소합니다.
5. 정리 단계 (30초) :
    - 콘서트 티켓팅이 끝나고 1번단계와 같이 평소 사용자로 돌아옵니다
    - 100명의 동시 사용자로 안정화됩니다.

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
        { duration: '30s', target: 50 },    // 준비 단계
        { duration: '1m', target: 1000 },   // 급격한 부하 증가
        { duration: '5m', target: 1000 },   // 피크 부하
        { duration: '2m', target: 200 },    // 부하 감소
        { duration: '1m', target: 200 },    // 정리 단계
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



**테스트 시나리오**

#### 테스트 스크립트
```js

```

#### 테스트 결과 분석



### 콘서트 목록 조회 API

예약가능한 콘서트 목록이 100개정도 있다고 가정하고 테스트를 진행해 보겠습니다.

**가정 및 시나리오**
- 100명의 유저부터 콘서트 목록 조회를 시작해서 점차 증가합니다
- 1분안에 1000명으로 증가하며 콘서트 조회를 합니다
- 2분안에 2000명으로 사용자가 2배 증가하면 콘서트 조회를 합니다.

**성공기준**
- 평균 응답 시간 : 500ms 이하
- 95% 센타일 응답 시간 : 1초 이하
- 오류율 1% 미만
- CPU 사용률 80 % 이하

#### 테스트 스크립트
```js

```

#### 테스트 결과 분석

![](https://velog.velcdn.com/images/asdcz11/post/60a0aee3-8401-4676-80c3-9c217acd73cb/image.png)

### 콘서트 예약 가능 날짜 조회 API

### 콘서트 예약가능 좌석 조회 API

### 콘서트 예약 API

### 콘서트 결재 API