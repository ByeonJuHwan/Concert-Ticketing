# Circuit Breaker 도입

## 문제 상황

결제 Coordinator는 여러 외부 서비스와 통신합니다:
- UserService: 포인트 차감
- ConcertService: 예약 확정, 좌석 확정

만약 ConcertService가 장애 상황에 빠진다면:
- 결제 요청마다 타임아웃까지 대기 (예: 10초)
- 쓰레드 풀이 고갈되어 다른 정상 요청도 처리 불가
- 장애가 전파되어 전체 시스템 마비

실제로 테스트 중 ConcertService 응답이 지연되자, 
결제 서비스의 응답 시간이 평균 200ms에서 8초로 증가했습니다.

---

## Circuit Breaker란?

Circuit Breaker는 장애 전파를 차단하는 안전장치입니다. 마치 전기 회로의 차단기처럼, 문제가 감지되면 자동으로 연결을 끊어 시스템 전체를 보호합니다.

### 동작 원리 (3가지 상태)

**1. CLOSED (정상)**
- 모든 요청이 정상적으로 외부 서비스로 전달됨
- 실패율과 응답시간을 지속적으로 모니터링
- 임계값을 초과하면 OPEN 상태로 전환

**2. OPEN (차단)**
- 모든 요청을 즉시 실패 처리 (Fast Fail)
- 외부 서비스 호출 없이 즉시 응답하여 쓰레드 풀 보호
- 일정 시간 후 자동으로 HALF-OPEN 상태로 전환

**3. HALF-OPEN (테스트)**
- 제한된 수의 요청으로 서비스 복구 테스트
- 테스트 성공 시 CLOSED로 복귀
- 테스트 실패 시 다시 OPEN으로 전환

---

## 적용 결과

결제 Coordinator의 동작 흐름:
1. 포인트 사용 → UserService 통신
2. 예약 확정 → ConcertService 통신
3. 좌석 확정 → ConcertService 통신
4. 결제 저장

```yaml
resilience4j:
  circuitbreaker:
    instances:
      concertService:
        failure-rate-threshold: 50
        # 50% 이상 실패 시 차단
        
        slow-call-rate-threshold: 50
        slow-call-duration-threshold: 10s
        # 10초 이상 걸리는 호출이 50% 넘으면 차단
        # (평균 200ms 대비 명백한 장애)
        
        wait-duration-in-open-state: 10s
        # OPEN 후 10초 대기 후 복구 시도
        
        permitted-number-of-calls-in-half-open-state: 3
        # 복구 테스트는 3번만 시도
        
        sliding-window-size: 10
        # 최근 10개 호출 기준으로 판단
```

이제 강제로 테스트를위해 ConcertService의 실패를 유발했을 때, 결제서비스에서 바로 모든 요청을 차단하는지 확인해보겠습니다.

```kotlin
@PostMapping("/reservations/seat/reserved")
fun changeSeatStatus(
    @RequestBody request: ChangeSeatReservedRequest
) {
    throw RuntimeException("서킷브레이커 테스트")
    changeSeatReservedUseCase.changeSeatStatusReserved(request.toCommand())
}
```

### 테스트 결과

| 요청 번호 | Circuit 상태 | 결과 | 비고 |
|----------|-------------|------|------|
| 1-10번 | CLOSED | 실패 (예외 발생) | 실패 카운트 누적 중 |
| 11번 | CLOSED → OPEN | 즉시 실패 | 실패율 50% 초과로 OPEN 전환 |
| 12-20번 | OPEN | 즉시 실패 (Fast Fail) | 외부 호출 없음 |
| 21번 (10초 후) | HALF-OPEN | 실패 | 복구 테스트 1/3 |
| 22-23번 | HALF-OPEN | 실패 | 복구 테스트 2/3, 3/3 |
| 24번 | HALF-OPEN → OPEN | 즉시 실패 | 테스트 실패로 다시 OPEN |

장애가 발생하는 서비스에 대한 호출이 빠르게 실패 처리되어, 서비스 안정성을 높일수 있었습니다.


### 결론

Circuit Breaker 도입으로 아래와 같은 효과가 있었습니다.

- 장애 서비스 호출을 자동 차단하여 **시스템 전체 안정성 확보**
- 쓰레드 풀 고갈 방지로 **정상 트래픽 처리 유지**

