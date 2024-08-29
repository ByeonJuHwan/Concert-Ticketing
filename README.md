# 콘서트 예약 시스템

## 요구사항

* 유저 토큰 발급 API
  * 예약 가능 날짜 / 좌석 API
  * 좌석 예약 요청 API
  * 잔액 충전 / 조회 API
  * 결제 API
* 각 기능 및 제약사항에 대해 단위 테스트를 반드시 하나 이상 작성하도록 합니다.
* 다수의 인스턴스로 어플리케이션이 동작하더라도 기능에 문제가 없도록 작성하도록 합니다.
* 동시성 이슈를 고려하여 구현합니다.
* 대기열 개념을 고려해 구현합니다.

---

## 마일스톤

* 1주차 : [콘서트 티켓팅 예약 시스템 설계](https://github.com/users/ByeonJuHwan/projects/7/views/4?pane=issue&itemId=69218480) (API 명세, 시퀀스 다이어그램, ERD, 마일스톤 작성, Mock API 작성)
* 2주차 : [포인트 충전 / 조회 기능 구현](https://github.com/users/ByeonJuHwan/projects/7/views/4?pane=issue&itemId=69218504), [대기열 시스템 구현](https://github.com/users/ByeonJuHwan/projects/7/views/4?pane=issue&itemId=69218492)
* 3주차 : [콘서트 좌석 예약](https://github.com/users/ByeonJuHwan/projects/7/views/4?pane=issue&itemId=69218503), [결재 구현](https://github.com/users/ByeonJuHwan/projects/7/views/4?pane=issue&itemId=69218505)


---
## 시퀀스 다이어그램

### 토큰 발급 API
```mermaid
sequenceDiagram
    participant Client
    participant TokenController
    participant TokenService
    participant TokenRepository

    Client->>+TokenController: [POST] /queue/tokens
    TokenController->>+TokenService: 토큰 발급 요청(userId)
    TokenService->>+TokenRepository: 토큰 저장(TokenEntity)
    TokenRepository-->>-TokenService: 토큰
    TokenService-->>-TokenController: 토큰
    TokenController-->>-Client: 토큰
```

### 토큰 조회 API

```mermaid
sequenceDiagram
    participant Client
    participant TokenFilter
    participant TokenController
    participant TokenService
    participant TokenRepository
    
    
    alt token is valid
	    Client->>+TokenFilter: [GET] /queue/tokens/{token}/status
      TokenFilter->>+TokenService: 토큰 유효성 검사
      TokenService-->>-TokenFilter:토큰 유효성 결과(Bolean)
		else token is not valid
			TokenFilter-->>-Client: TOKEN_NOT_FOUND
		end
		
    TokenFilter->>+TokenController: 토큰
	  TokenController->>+TokenService: 토큰 정보 조회 요청(Token)
	  TokenService->>+TokenRepository: 토큰 정보 조회 요청(Token)
	  TokenRepository-->>-TokenService: 토큰정보(대기열,만료시간..)
	  TokenService-->>-TokenController: 토큰정보(대기열,만료시간..)
    TokenController-->>-Client: 토큰정보(대기열,만료시간..)
```

### 콘서트 목록 조회 API

```mermaid
sequenceDiagram
    participant Client
    participant TokenFilter
    participant TokenController
    participant TokenService
    participant ConcertController
    participant ConcertService
    participant ConcertRepository
    
    alt token is valid
	    Client->>+TokenFilter: [GET] /concerts
      TokenFilter->>+TokenService: 토큰 유효성 검사
      TokenService-->>-TokenFilter:토큰 유효성 결과(Bolean)
		else token is not valid
			TokenFilter-->>-Client: TOKEN_NOT_FOUND
		end

    TokenFilter->>+ConcertController: 콘서트 목록 조회 요청
    ConcertController->>+ConcertService: 콘서트 목록 조회
    ConcertService->>+ConcertRepository: 콘서트 목록 조회
    ConcertRepository-->>-ConcertService: 콘서트 목록 리스트
    ConcertService-->>-ConcertController: 콘서트 목록 리스트
    ConcertController-->>-Client: 콘서트 목록 리스트
```

### 예약 가능한 날짜 조회 API

```mermaid
sequenceDiagram
    participant Client
    participant TokenFilter
    participant TokenController
    participant TokenService
    participant ConcertController
    participant ConcertOptionService
    participant ConcertOptionRepository

    alt token is valid
	    Client->>+TokenFilter: [GET] /concerts/{concertId}/dates/available
      TokenFilter->>+TokenService: 토큰 유효성 검사
      TokenService-->>-TokenFilter:토큰 유효성 결과(Bolean)
		else token is not valid
			TokenFilter-->>-Client:401 Unauthorized
		end

    TokenFilter->>+ConcertController: 콘서트 정보
    ConcertController->>+ConcertOptionService: 예약가능 날짜 조회 요청
    ConcertOptionService->>+ConcertOptionRepository: 예약가능 날짜 조회
    ConcertOptionRepository-->>-ConcertOptionService: 예약가능 날짜 리스트
    ConcertOptionService-->>-ConcertController: 예약가능 날짜 리스트
    ConcertController-->>-Client: 예약가능 날짜 리스트
```

### 예약 가능한 좌석 조회 API

```mermaid
sequenceDiagram
    participant Client
    participant TokenFilter
    participant TokenService
    participant ConcertController
    participant SeatService
    participant SeatRepository

		alt token is valid
	   Client->>+TokenFilter: GET /concert/{concertOptionId}/seats/available
     TokenFilter->>+TokenService: 토큰 유효성 검사
     TokenService-->>-TokenFilter:토큰 유효성 결과(Bolean)
		else token is not valid
			TokenFilter-->>-Client:401 Unauthorized
		end

    TokenFilter->>+ConcertController: 콘서트 날짜 정보
    ConcertController->>+SeatService: 콘서트 좌석 조회(optionId)
    SeatService->>+SeatRepository: 콘서트 좌석 조회(optionId)
    SeatRepository-->>-SeatService: 좌석 리스트
    SeatService-->>-ConcertController: 좌석 리스트
    ConcertController-->>-Client: 좌석 리스트
```

### 콘서트 좌석 예약 API

```mermaid
sequenceDiagram
    participant Client
    participant TokenFilter
    participant TokenService
    participant ConcertController
    participant SeatService
    participant SeatRepository
    
    
    alt token is valid
	   Client->>+TokenFilter: [POST] /concerts/reserve-seat
     TokenFilter->>+TokenService: 토큰 유효성 검사
     TokenService-->>-TokenFilter:토큰 유효성 결과(Bolean)
		else token is not valid
			TokenFilter-->>-Client:401 Unauthorized
		end
    
    
    TokenFilter->>+ConcertController: 콘서트날짜 + 좌석 정보
	  ConcertController->>+SeatService: 콘서트날짜 + 좌석 정보
	  SeatService->>+SeatRepository: 좌성 상태 확인
	  SeatRepository-->>-SeatService: 좌성 상태 정보
	  SeatService->>+SeatRepository: 좌성 상태 임시 배정 변경
	  SeatRepository-->>-SeatService: 좌석 임시 배정 결과
	  SeatService-->>-ConcertController: 좌석 예약 결과
	  ConcertController-->>-Client: 좌석 예약 결과
    
   
```


### 결재 API

```mermaid
sequenceDiagram
    participant Client
    participant TokenFilter
    participant TokenService
    participant ConcertController
    participant ReservationService
    participant ReservationRepository
    participant PointService
    participant PointRepository
    
    alt token is valid
	   Client->>+TokenFilter: [POST] /concerts/payments
     TokenFilter->>+TokenService: 토큰 유효성 검사
     TokenService-->>-TokenFilter:토큰 유효성 결과(Bolean)
		else token is not valid
			TokenFilter-->>-Client:401 Unauthorized
		end
		
		 TokenFilter->>+ConcertController: 유저 정보(userId)
	   ConcertController->>+ReservationService: 유저 정보(userId)
	   ReservationService->>+ReservationService: 예약 유효성 확인(userId)
	   ReservationService->>+ReservationRepository : 예약 정보 요청
	   ReservationRepository-->>-ReservationService : 예약 정보
	   ReservationService->>+PointService : 결재 요청(예약 정보)
	   alt 정상결재
       PointService->>+PointService : 포인트 유효성 검사(포인트 잔액)
       PointService->>+PointRepository: 포인트 차감 요청
       PointRepository-->>-PointService: 잔여 포인트
		 else 포인트 부족
			 PointService->>+PointService : 포인트 유효성 검사(포인트 잔액)
			 PointService->>+PointRepository: 포인트 차감 요청
			 PointRepository-->>-PointService : NOT_ENOUGH_POINTS ERROR
		 end
		 
		 PointService-->>-ReservationService: 잔여포인트
		 ReservationService-->>-ConcertController:결재완료
		 ConcertController-->>-Client: 결재완료
```

### 포인트 충전 API

```mermaid
sequenceDiagram
    participant Client
    participant PointController
    participant PointService
    participant PointRepository

    Client->>+PointController: POST /points/charge (userId, amount in body)
    PointController->>+PointService: 충전 포인트 정보(userId, amount)
    PointService->>+PointService: 포인트 유효성 검사(amount)
    PointService->>+PointRepository: 잔액 충전 요청(userId)
    PointRepository-->>-PointService: 충전된 잔액
    PointService-->>-PointController: 충전된 잔액
    PointController-->>-Client: 충전된 잔액
```

### 포인트 조회 API

```mermaid
sequenceDiagram
    participant Client
    participant PointController
    participant PointService
    participant PointRepository

    Client->>+PointController: GET /points/current/{userId}
    PointController->>+PointService: 현재 잔액 조회 요청(userId)
    PointService->>+PointRepository: 현재 잔액 조회 요청(userId)
    PointRepository-->>-PointService: Point
    PointService-->>-PointController: Point
    PointController-->>-Client: Point
```

---

## API 명세서

### 1. 유저 대기열 토큰 발급

| Method | URI | Description         |
|-----|-----|---------------------|
| POST |/queue/tokens| 대기열 등록| 

**Request**

```https
  curl -X POST https://{SERVER_URL}/queue/tokens \
  -H "Content-Type: application/json" \
  -d '{ \
        "userId": "{userId}", \
      }'
```
**Response**

```json
{
    "result": {
        "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9"
    }
}
```

**Error**
```json
{
    "status": "TOKEN_GENERATION_FAILED",
    "message": "토큰 생성에 실패했습니다. 다시 시도해주세요."
}
```

---
### 2. 유저 토큰 정보 조회 API

| Method | URI                           | Description         |
|-----|-------------------------------|---------------------|
| GET | /queue/tokens/status/{userId} | 대기열 정보 확인| 

**Request**

```https
curl -X GET https://{SERVER_URL}/queue/tokens/status/{userId}
```

**Response**

```json
{
    "result": {
        "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9",
        "status": "ACTIVE",
        "queueOrder": 10,
        "remainingTime": 360
    }
}
```

**Error**
```json
{
  "code": "TOKEN_NOT_FOUND",
  "message": "토큰 정보를 찾을 수 없습니다"
}
```

---

### 3. 콘서트 목록 조회 API

| Method | URI | Description         |
|-----|-----|---------------------|
| GET |/concerts|콘서트 목록 조회|

**Request**

```https
curl -X GET https://{SERVER_URL}/concerts\
```

**Response**

```json

{
    "result": {
        "concerts": [
            {
                "concertName": "아이유 콘서트",
                "singer": "아이유",
                "startDate": "2024-02-01",
                "endDate": "2024-02-12",
                "reserveStartDate": "2024-01-01",
                "reserveEndDate": "2024-01-31",
                "id": 1
            },
            {
                "concertName": "에스파 콘서트",
                "singer": "에스파",
                "startDate": "2024-02-01",
                "endDate": "2024-02-12",
                "reserveStartDate": "2024-01-01",
                "reserveEndDate": "2024-01-31",
                "id": 2
            }
        ]
    }
}

```
---

### 4. 예약 가능한 날짜 조회 API

| Method | URI | Description         |
|-----|-----|---------------------|
| GET |/concerts/{concertId}/available-dates |예약 가능한 날짜 조회 | 

**Request**

```https
curl -X GET https://{SERVER_URL}/concerts/{concertId}/dates/available \
  -H "Authorization: Bearer {token}"
```

**Response**

```json
{
    "result": {
        "concerts": [
            {
                "concertId": 1,
                "title": "에스파 콘서트",
                "concertDate": "024-07-15",
                "concertTime": "13:00",
                "concertVenue": "잠실 종합 운동장",
                "availableSeats": 50
            },
            {
                "concertId": 1,
                "title": "에스파 콘서트",
                "concertDate": "024-07-16",
                "concertTime": "18:00",
                "concertVenue": "도쿄돔",
                "availableSeats": 50
            }
        ]
    }
}
```
**Error**

```json
{
  "code": "TOKEN_EXPIRED",
  "message": "토큰이 만료되었습니다"
}
```

---

### 5.예악 가능 좌석 API

| Method | URI | Description         |
|-----|-----|---------------------|
| GET |/concerts/{concertOptionId}/seats/available|예약 가능한 좌석 조회 | 


**Request**

```https
curl -X GET https://{SERVER_URL}/concerts/{concertOptionId}/seats/available \
  -H "Authorization: Bearer {token}"
```

**Response**

```json
{
    "result": {
        "concertOptionId": 1,
        "seats": [
            {
                "seatId": 1,
                "seatNumber": 1,
                "price": 5000
            },
            {
                "seatId": 23,
                "seatNumber": 16,
                "price": 5000
            },
            {
                "seatId": 29,
                "seatNumber": 17,
                "price": 10000
            }
        ]
    }
}
```
**Error**

```json
{
  "code": "TOKEN_EXPIRED",
  "message": "토큰이 만료되었습니다"
}
```

```json
{
  "code": "NO_SEATS_AVAILABLE",
  "message": "예약 가능한 좌석이 없습니다."
}
```

---

### 6. 콘서트 좌석 예약 API

| Method | URI | Description         |
|-----|-----|---------------------|
| POST |/concerts/reserve-seat|콘서트 좌석 예약 | 


**Request**

```https
  curl -X POST https://{SERVER_URL}/concerts/reserve-seat \
  -H "Authorization: Bearer {token}" \
  -d '{ \
        "concertOptionId": {concertOptionId}, \
        "seatNo": {seatNo} 
      }'
```
**Response**

```json
{
    "result": {
        "status": "PENDING",
        "expiresAt": "2024-07-03T23:40:25.775651"
    }
}
```

**Error**

```json
{
  "code": "TOKEN_EXPIRED",
  "message": "토큰이 만료되었습니다"
}
```
```json
{
  "code": "SEAT_NOT_FOUND",
  "message": "존재하지 않는 좌석입니다. 다른 좌석을 선택해주세요"
}
```
```json
{
  "code": "SEAT_ALREADY_RESERVED",
  "message": "이미 예약된 좌석입니다. 다른 좌석을 선택해주세요"
} 
```
---

### 7. 콘서트 좌석 결제 API

| Method | URI          | Description         |
|-----|--------------|---------------------|
| POST | /payment/pay |콘서트 좌석 결재 | 


**Request**

```https
  curl -X POST https://{SERVER_URL}/concerts/payments \
  -H "Authorization: Bearer {token}" \
  -d '{ \
        "reservationId": {reservationId} \
      }'
```
**Response**

```json
{
    "reservationId": "1",
    "seatNo": "1",
    "concertVenue" : "서울 잠실 종합운동장",
    "concertDate" : "2025-01-01",
    "concertTime" : "13:00"
}
```
**Error**

```json
{
  "code": "TOKEN_EXPIRED",
  "message": "토큰이 만료되었습니다"
}
```
```json
{
    "status": "PAYMENT_FAILED",
    "message": "결제에 실패했습니다. 다시 시도해주세요."
}
```
```json
{
   "status": "SEAT_ALLOCATION_EXPIRED",
   "message": "좌석 임시 배정 시간이 만료되었습니다. 다시 시도해주세요."
} 
```
```json
{
  "code": "INSUFFICIENT_POINTS",
  "insufficient_point": 3000,
  "point": 2000,
  "message": "포인트 부족"
} 
```

---

### 8. 포인트 충전 API

| Method | URI | Description         |
|-----|-----|---------------------|
| PUT |/points/charge|포인트 충전 | 

**Request**

```https
  curl -X POST https://{SERVER_URL}/points/charge \
  -H "Content-Type: application/json" \
  -d '{ \
        "userId": {userId}, \
        "amount": {amount} \
      }'
```

**Response**

```json
{
    "result": {
        "currentPoints": 5000
    }
}
```
**Error**
```json
{
    "status": "CHARGE_FAILED",
    "message": "포인트 충전에 실패했습니다. 다시 시도해주세요."
}
```

---

### 9. 포인트 조회 API

| Method | URI | Description         |
|-----|-----|---------------------|
| GET |/points/current/{userId} |포인트 조회| 

**Request**

```https
  curl -X GET https://{SERVER_URL}/points/current/{userId}
  -H "Content-Type: application/json"
```

**Response**

```json
{
    "result": {
        "currentPoints": 5000
    }
}
```

---

## ERD

```mermaid
erDiagram
    USER {
        Long id
        String name
        LocalDateTime createdAt
        LocalDateTime updatedBy
    }

    CONCERT{
        Long id
        String name
        String singer
        String StartDate
        String EndDate
        String reserveStartDate
        String reserveEndDate
    }

    CONCERT_OPTION {
        Long id
        Long concertId
        String concertDate
        String concertTime
        String concertVenue
        LocalDateTime createdAt
        LocalDateTime updatedBy
    }

    POINT {
        Long id
        Long userId
        Long point
    }

    POINT_HISTORY {
        Long id
        Long userId
        Long amount
        PointTransactionType type
        LocalDateTime createdAt
        LocalDateTime updatedBy
    }

    QUEUE_TOKEN{
        Long id
        Long userId
        String token
        Int queueOrder
	LocalDateTime expiresAt 
        QueueTokenStatus status
        LocalDateTime createdAt
        LocalDateTime updatedBy
    }

    RESERVATION {
        Long id
        Long userId
        Int seatNo
        String concertName
        Long price
        expiresAt LocalDateTime
        status String
        String concertVenue
        String concertDate
        String concertTime
        String paymentStatus
        LocalDateTime createdAt
        LocalDateTime updatedBy
    }

    SEAT {
        Long id
        Long concertOptionId
        Int seatNo
        Long price
        SeatStatus seatStatus
    }

    USER ||--o{ POINT : "userId"
    CONCERT ||--o{ CONCERT_OPTION : "concertId"
    CONCERT_OPTION ||--o{ SEAT: "concertOptionId"
```

---

# 동시성 이슈

콘서트 좌서 예약시 동일한 좌석에 여려명이 예약을 시도하는 과정에서 동시성 이슈가 발생합니다.

어떻게 동시성 이슈를 해결하는지 그리고 락의 범위를 줄이기위해 트랜잭션의 범위를 줄이면 어떻게 되는지에 대해서
작성한 문서입니다.

**[콘서트 좌석 예약의 동시성 이슈 해결 방안](docs/synchronicity.md)**

---

# 인덱스와 캐싱

대용량 트래픽 발생시 데이터의 쓰기 작업보다는 읽기 작업에서 성능 저하가 발생합니다.
읽기 작업을 개선하기 위해서는 아래와 같은 2 가지 방법이 대표적입니다.

- 인덱스
- 캐싱

데이터 베이스를 풀스캔 하지 않고 `Index` 를 활용해서 쿼리 조회 속도를 줄이고, 조회 API 에서 `Index`를 적용하여 어떻게 성능 개선이 되었는지 정리했습니다.

**[콘서트 좌석 예약의 Index 활용 방법 및 성능개선](docs/index.md)**

캐싱의 경우 데이터베이스에서 조회 쿼리를 하지 않기 때문에 빠른 응답이 가능하지만 변동이 잦은 데이터의 경우 데이터 정합성의 문제점이 있습니다.
따라서 어떤 조회 API 에 캐싱을 사용해야 하는지 분석하고, 캐싱 적용 전 후 의 성능 비교를 작성한 문서입니다.

**[콘서트 좌석 예약의 캐싱 활용 방법 및 성능개선](docs/caching.md)**

---

# 이벤트 드리븐 방식 전환 / 아웃박스 패턴

서비스의 규모가 커졌을 경우 각 서비스를 쪼개는 MSA 방식을 사용합니다. 

하나의 서비스에서 이벤트를 발행하고 이벤트를 수신받은
서비스에서 다음 동작을 실행하는 이벤트 드리븐 방식을 적용해보고, `ApplicationEventPublisher` 를 사용해본 뒤 `Kafka` 로 변경하며 겪은 내용을 문서로 정리해 봤습니다.

**[콘서트 좌석 예약의 이벤트 드리븐 방식 적용](docs/event.md)**

이벤트 드리븐 방식을 사용하다보면 네트웨크 문제 혹은 MQ 가 문제가 생겨 이벤트가 정상적으로 발행이 안되는 경우가 생길수 있습니다.

이 문제점을 해결하기 위해 아웃박스 패턴을 사용해서 미발행된 이벤트들을 모아놓고 배치, 스케줄러로 이벤트 발행을 보장해 주는 패턴을 적용해봤습니다.

**[콘서트 좌석 예약의 아웃박스 패턴 적용](docs/outbox.md)**

---

# 부하테스트 / 장애대응

콘서트 좌석 예약이라는 특성상 특정 시간에 급격하게 트래픽이 증가하는 구조를가지고 있습니다.

이를 대비하여 대기열 시스템을 만들었지만, 운영 배포 환경에서는 실제로 몇명의 사용자를 서버가 받을수 있을지 확인이 필요합니다.

이를 위해 부하테스트를 진행하였고 실제로 각 API 가 대량 몇 명의 동시 사용자를 받아 낼 수 있는지 테스트 해봤습니다.

**[콘서트 좌석 예약의 부하테스트](docs/performance.md)**

**[콘서트 좌석 예약의 장애대응문서](docs/response.md)**