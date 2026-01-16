# 멀티플렉싱

gRPC 의 장점중 하나는 멀티플렉싱 기능입니다.

gRPC의 주요 장점 중 하나로 멀티플렉싱이 언급되지만, 실제로 어떻게 동작하는지 직접 확인한 자료는 많지 않습니다.

이번 기회에 `Wireshark`를 사용하여 HTTP/1.1과 gRPC의 네트워크 연결 패턴을 직접 비교하고, 멀티플렉싱의 실제 이점을 확인해보겠습니다.

## 멀티플렉싱이란?

멀티플렉싱(Multiplexing)은 하나의 TCP 연결에서 여러 요청과 응답을 동시에 처리할 수 있는 HTTP/2의 핵심 기능입니다.

예를 들어, Concert Service에 3개의 API를 호출할 때:
- **HTTP/1.1**: 3개 요청 → 3개 TCP 연결 (또는 순차 처리)
- **gRPC (HTTP/2)**: 3개 요청 → 1개 TCP 연결 (동시 처리)

### HTTP 1.1 과 비교

HTTP/1.1의 경우 각 요청마다 별도의 네트워크 연결을 시도합니다.

이 과정에서 3-way 핸드셰이크 과정이 동반되고 각 요청마다 헤더 파싱이 발생합니다.

하지만 멀티플렉싱 기술을 사용하면 핸드셰이크 과정이 1번으로 줄고, 
헤더는 요청마다 전송하지만 이미 전송한 헤더의 경우 인덱스를 통해서 헤더 크기 압축도 가능합니다.

이제 실제로 테스트를 통해 gRPC가 정말 1개의 네트워크 연결로 여러 API 호출을 
처리하는지 확인해보겠습니다.

### HTTP/1.1 vs gRPC 연결 패턴 비교

#### 테스트 시나리오

Payment Service에서 다음 3개 요청을 수행합니다:
- User Service → 포인트 사용 (8081 포트)
- Concert Service → 예약 확정 (8080 포트)
- Concert Service → 좌석 확정 (8080 포트)

이 요청은 순차적으로 수행되며 각 요청이 끝난 후 다음 요청이 실행됩니다.

#### HTTP 네트워크 테스트

먼저 HTTP/1.1의 연결 패턴을 확인해보겠습니다.

#### 예상

HTTP/1.1은 각 요청마다 새로운 연결을 생성한다고 알려져 있습니다. 따라서 3개 요청이므로 **3개의 TCP 연결**이 생성될 것으로 예상했습니다.

아래는 실제 `Wireshark` 를 사용해서 네트워크 연결 패킷을 살펴본 결과입니다.

![결제 http 요청 패킷](https://velog.velcdn.com/images/asdcz11/post/60280e4a-e74f-442c-aaa6-853f34232512/image.png)
![결제 http 요청 패킷 정보](https://velog.velcdn.com/images/asdcz11/post/f2c3f366-fad5-4b91-bdac-5509677a618f/image.png)

처음 Payment Service로 들어오는 POST 요청을 제외하면, 
3개 요청이 아닌 8080 포트와 8081 포트로 각각 1번씩 총 **2개의 TCP 연결**만 
생성된 것을 확인할 수 있었습니다.

#### 원인 분석: HTTP/1.1의 Keep-Alive

제 처음 예상과 달라진 테스트 결과를 분석한 결과, 원인은 다음과 같았습니다.

HTTP/1.1의 경우 기본 Connection 헤더가 **`Keep-Alive`** 설정으로 되어 있었습니다.

**Keep-Alive의 동작**

HTTP/1.1의 Keep-Alive는 TCP 연결을 재사용하여 핸드셰이크 오버헤드를 줄이는 기능입니다.
```console
8080 포트 (Concert Service):
├─ [TCP 연결 수립]
├─ 요청 1: 예약 확정 → [2초] → 응답
├─ 요청 2: 좌석 확정 → [2초] → 응답
└─ [연결 유지]

8081 포트 (User Service):
├─ [TCP 연결 수립]
└─ 요청: 포인트 사용 → [2초] → 응답
```

따라서 3개의 요청이 모두 동일한 호스트(8080, 8081)로 향하기 때문에 
각 호스트별로 1개의 연결만 생성된 것이었습니다.

--- 

#### gRPC 네트워크 테스트

이제 gRPC를 사용해서 테스트한 결과입니다.

![결제 http 요청 패킷 정보](https://velog.velcdn.com/images/asdcz11/post/2c4bcda4-d005-406c-bbf6-357e4f076170/image.png)


gRPC의 경우 9091 포트와 9093 포트로 HTTP와 동일하게 
각각 1번씩 연결이 생성되는 것을 확인할 수 있었습니다.


#### 중간 결론

HTTP와 gRPC 모두 동일한 결과가 나와서 멀티플렉싱의 이점을 제대로 확인할 수 없었습니다.

순차 요청에서는 HTTP/1.1의 Keep-Alive 덕분에 gRPC와 비슷한 결과가 나왔습니다. 
둘 다 같은 서버로의 요청을 하나의 연결로 처리했기 때문입니다.

---

## 진짜 차이는 동시 요청에서 나타난다

순차 요청에서는 HTTP/1.1 Keep-Alive와 gRPC의 차이를 체감하기 어려웠습니다. 
그렇다면 멀티플렉싱의 진짜 장점은 무엇일까요?

### Keep-Alive의 한계

HTTP/1.1 Keep-Alive는 연결을 재사용하지만, 
**한 번에 하나의 요청만 처리**할 수 있습니다 (Head-of-Line Blocking).
```console
시나리오: 예약 확정(2초) + 좌석 확정(0.1초)

HTTP/1.1 Keep-Alive (순차):
├─ 예약 확정 → [2초 대기]
└─ 좌석 확정 → [0.1초 대기]
총 2.1초

gRPC 멀티플렉싱 (동시):
├─ 예약 확정 → [2초 대기] ┐
└─ 좌석 확정 → [0.1초 대기] ┘ 동시 실행
총 2초 (느린 작업 기준)
```

HTTP/1.1이 동시 처리를 하려면 **여러 개의 TCP 연결**이 필요합니다.
반면 gRPC는 **하나의 연결에서 멀티플렉싱**으로 동시 처리가 가능합니다.

이 차이를 확인하기 위해 **동시 요청 테스트**를 진행했습니다.

---

### 동시요청 테스트

HTTP의 경우 Thread를 직접 사용해서 2개의 요청을 동시에 보내도록 작성했고, 
gRPC의 경우 코루틴을 사용해서 2개의 요청을 동시에 보내도록 작성했습니다.

#### HTTP 동시 요청 테스트 결과


```kotlin
@GetMapping("/http/multi/{reservationId}")
fun testJavaMulti(@PathVariable reservationId: Long) {
    val start = System.currentTimeMillis()

    println("[${System.currentTimeMillis() - start}ms] HTTP 동시 요청 시작")

    val latch = CountDownLatch(2)

    thread {
        println("[${System.currentTimeMillis() - start}ms] - 예약 확정 요청 전송")
        concertApiClient.changeReservationPaid(reservationId)
        println("[${System.currentTimeMillis() - start}ms] - 예약 확정 응답 받음")
        latch.countDown()
    }

    thread {
        println("[${System.currentTimeMillis() - start}ms] - 좌석 확정 요청 전송")
        concertApiClient.changeSeatReserved(reservationId)
        println("[${System.currentTimeMillis() - start}ms] - 좌석 확정 응답 받음")
        latch.countDown()
    }

    latch.await()
    println("[${System.currentTimeMillis() - start}ms] HTTP 동시 요청 완료")
}
```

```text
[0ms] HTTP 동시 요청 시작
[2ms] - 예약 확정 요청 전송
[3ms] - 좌석 확정 요청 전송
[91ms] - 예약 확정 응답 받음
[91ms] - 좌석 확정 응답 받음
[91ms] HTTP 동시 요청 완료
```

두 요청이 거의 동시에 전송되고(2ms, 3ms), 응답도 동시에 받았습니다(91ms).


![동시요청-http](https://velog.velcdn.com/images/asdcz11/post/2127450e-39da-4832-9891-94325b83bec2/image.png)


서로 다른 스레드로 멀티스레드 환경에서 동시 요청을 보낸 결과, 
의도한 대로 **2개의 TCP 연결**이 생성되었습니다.

**분석:**
- Concert Service(8080)로 2개 요청 → 2개 TCP 연결 생성
- Keep-Alive는 있지만, 동시 실행을 위해 별도 연결 필요
- 각 연결마다 핸드셰이크 발생

---

#### gRPC 동시 요청 테스트

```kotlin
@GetMapping("/grpc/multi/{reservationId}")
suspend fun testKotlinMulti(@PathVariable reservationId: Long) {
    val start = System.currentTimeMillis()

    runBlocking {
        println("[${System.currentTimeMillis() - start}ms] gRPC 동시 요청 시작")

        val jobs = listOf(
            async {
                println("[${System.currentTimeMillis() - start}ms] - 예약 확정 요청 전송")
                concertGrpcClient.changeReservationPaid(reservationId).also {
                    println("[${System.currentTimeMillis() - start}ms] - 예약 확정 응답 받음")
                }
            },
            async {
                println("[${System.currentTimeMillis() - start}ms] - 좌석 확정 요청 전송")
                concertGrpcClient.changeSeatReserved(reservationId).also {
                    println("[${System.currentTimeMillis() - start}ms] - 좌석 확정 응답 받음")
                }
            }
        )

        jobs.awaitAll()
        println("[${System.currentTimeMillis() - start}ms] gRPC 동시 요청 완료")
    }
}
```

```text
[1ms] gRPC 동시 요청 시작
[7ms] - 예약 확정 요청 전송
[112ms] - 좌석 확정 요청 전송
INFO 20337 --- [nio-8083-exec-2] o.k.p.a.o.a.g.c.ConcertGrpcAdapter       : grpc 좌석 예약 확정 호출: reservationId = 64
INFO 20337 --- [nio-8083-exec-2] o.k.p.a.o.a.g.c.ConcertGrpcAdapter       : 좌석 상태 예약 변경 성공
[219ms] - 좌석 확정 응답 받음
INFO 20337 --- [nio-8083-exec-2] o.k.p.a.o.a.g.c.ConcertGrpcAdapter       : 예약 결제 상태가 성공적으로 변경되었습니다
[219ms] - 예약 확정 응답 받음
[219ms] gRPC 동시 요청 완료
```

요청 전송 시점은 7ms, 112ms로 차이가 있지만, 
**응답은 219ms에 동시에 완료**되었습니다.

이는 두 요청이 같은 TCP 연결을 통해 
**독립적으로 처리**되었다는 증거입니다.

만약 순차 처리였다면 다음과 같았을 것입니다:
```console
[7ms] 예약 확정 전송 → [107ms 응답]
[107ms] 좌석 확정 전송 → [207ms 응답]  ← 100ms 후
```

![gRPC-동시요청](https://velog.velcdn.com/images/asdcz11/post/e1e350e7-ca07-4e30-b8a0-e5b39d5940f7/image.png)


gRPC의 경우 동시 요청 상황에서도 **1개의 TCP 연결**을 사용해서 
2개의 요청이 동시에 처리되는 것을 확인할 수 있었습니다.

**분석:**
- Concert Service(9093)로 2개 요청 → 1개 TCP 연결 사용
- HTTP/2 멀티플렉싱으로 동시 처리
- 각 요청은 독립적인 HTTP/2 Stream으로 구분

### 결과

HTTP/1.1은 동시 처리를 위해 **2개의 TCP 연결**이 필요했지만,
gRPC는 **1개의 TCP 연결**로 동일한 작업을 수행했습니다.

---

## 마무리

블로그 글로만 읽거나 이론으로만 접하고 넘어갔다면 
처음 확인해본 순차 요청에서의 HTTP/1.1 Keep-Alive 기능 때문에 
멀티플렉싱의 장점을 제대로 체감하지 못했을 수도 있습니다.

실제 멀티플렉싱 기능이 동작하는 것을 확인하면서 
네트워크에 대해 더 깊게 알아가는 시간이었습니다.