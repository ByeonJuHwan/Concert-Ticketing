# 멀티플렉싱

gRPC 의 장점중 하나는 멀티플렉싱 기능입니다.

멀티플렉싱(Multiplexing)은 하나의 TCP 연결에서 여러 요청과 응답을 동시에 처리할 수 있는 HTTP/2의 핵심 기능입니다.

예를 들어, 3개의 서비스와 통신한다고 할 때 1번의 TCP 연결로 3개의 통신이 이 연결 한개를 공유하면서 사용할수 있습니다.

### HTTP 1.1 과 비교

HTTP 1.1 의 경우 각 요청마다 별도의 네트워크 연결을 시도합니다

이 과정에서 3way-핸드쉐이크 과정이 동반되고 각 요청마다 헤더 파싱이 발생합니다.

하지만 멀티플렉싱 기술을 사용하면 핸드쉐이크 과정이 1번으로 줄고 헤더는 요청마다 전송하지만 이미 전송한 헤더의 경우 인덱스를 통해서 헤더 크기 압축도 가능합니다.

이제 실제로 테스트 코드 결과를 통해서 실제 gRPC 의 경우 1번의 네트워크 연결로 3개의 API 가 해당 네트워크를 공유하며 사용하는지 확인해보겠습니다.

### HTTP/1.1 vs gRPC 연결 패턴 비교

#### 테스트 시나리오

Payment Service 에서 다음 3개 요청 수행:
- UserService -> 포인트 사용 (8081 포트)
- ConcertService -> 예약 확정 (8080 포트)
- ConcertService -> 좌석 확정 (8080 포트)

이 요청은 동기적으로 수행되며 각 요청이 끝난 다음 요청이 실행됩니다

#### HTTP 네트워크 테스트

여기서 제가 예상한 점은 http 의 연결의 경우 각 요청별로 새로운 연결이 생성되어 3개의 연결이 생성될것으로 예상했습니다.

아래는 실제 `Wireshark` 를 사용해서 네트워크 연결 패킷을 살펴본 결과입니다.

![결제 http 요청 패킷](https://velog.velcdn.com/images/asdcz11/post/60280e4a-e74f-442c-aaa6-853f34232512/image.png)
![결제 http 요청 패킷 정보](https://velog.velcdn.com/images/asdcz11/post/f2c3f366-fad5-4b91-bdac-5509677a618f/image.png)

처음 Payment Service 로 들어오는 POST 요청을 제외한 3개요청이 아닌 8080, 8081 딱 1번씩 총 2번 네트워크 연결이 되는것을 확인 할 수 있었습니다.

제 처음 예상과는 달라진 테스트 결과에 확인해본 결과 원인은 다음과 같았습니다.

HTTP 1.1 의 경우 기본 Connection 이 keep-alive 설정이 기본이었습니다.

HTTP/1.1의 Keep-Alive는 TCP 연결을 재사용하여 핸드셰이크 오버헤드를 줄입니다.

```text
1개 TCP 연결:
├─ 요청 1 → [2초] → 응답 1
├─ 요청 2 → [2초] → 응답 2
└─ 요청 3 → [2초] → 응답 3

총 소요 시간: 6초
핸드셰이크: 1번
```

따라서 3개의 요청이 모두 동일한 호스트(8080, 8081)로 향하기 때문에 각 호스트별로 1개의 연결만 생성된것이었습니다.

--- 

#### gRPC 네트워크 테스트

이제 gRPC 를 사용해서 테스트해본 결과입니다



---

### 동시요청 테스트

순차적으로 요청이 가는 현 상황에서는 HTTP/2 의 멀티 플랙싱 장점을 제대로 체감하기 어렵웠습니다.

순차 요청이 아닌 동시요청을 통해서 HTTP 1.1 과 gRPC 의 멀티플렉싱 차이를 비교해보겠습니다.

http 의 경우 Thread 를 직접 사용해서 2개의 요청을 동시에 보내도록 작성하였고, gRPC 의 경우 코루틴을 사용해서 2개의 요청을 동시에 보내도록 작성했습니다.

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

```


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

```

gRPC 의 경우 동시요청 속에서도 1개의 TPC 연결을 사용해서 2개의 요청이 동시에 처리되는것을 확인할 수 있었습니다.

---

## 마무리

블로그글로만 읽거나 이론으로만 접하고 넘어갔다면 제일 처음 확인해본 순차요청에서의 HTTP 1.1 의 `keep-alive` 기능 때문에 멀티플렉싱의 장점을 제대로 체감하지 못했을수도 있습니다.

실제 멀티플랙싱 기능이 동작하는걸 확인해보면서 더 네트워크에 대해서 깊게 알아가는 시간이었습니다.




