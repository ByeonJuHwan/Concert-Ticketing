# 코루틴 전환

프로젝트에서는 스레드 기반의 비동기 작업을 처리하고 있습니다. 하지만 코틀린에서는 코루틴이라는 경량 스레드를 제공하여 더 효율적인 비동기 프로그래밍이 가능합니다.

이 문서에서는 코루틴 전환의 필요성과 실제 전환 과정에서 얻은 이점들을 정리해보겠습니다.

## 기존 멀티 스레드의 문제점

### 1. Thread 객체 직접 사용
```kotlin
fun processData() {
    Thread {
        println("긴 작업 시작")
        Thread.sleep(2000L)
        println("긴 작업 완료")
    }.start()
    
    println("다른 작업 진행")
}
```

**문제점:**
- Thread 생성 비용이 크고 재사용 불가능
- 스레드 생명주기를 개발자가 직접 관리해야 함
- 스레드 수를 제어하기 어려워 리소스 고갈 위험

### 2. ExecutorService 사용
```kotlin
private val executorService = Executors.newFixedThreadPool(10)

fun processData() {
    executorService.submit {
        println("긴 작업 시작")
        Thread.sleep(2000L)
        println("긴 작업 완료")
    }
    
    println("다른 작업 진행")
}
```

**개선점:**
- 스레드풀을 통한 스레드 재사용 가능
- 스레드 생명주기 관리 자동화

**여전히 남은 문제:**
- 반환값이 필요한 경우 블로킹 발생

### 3. Future를 이용한 결과 반환
```kotlin
fun processData(): String {
    val future: Future<String> = executorService.submit<String> {
        Thread.sleep(2000L)
        "처리 완료"
    }
    
    // 다른 작업들...
    
    // 결과가 필요한 시점에 블로킹 발생
    return future.get() // 여기서 스레드가 블로킹됨
}
```

**문제점:**
- `future.get()` 호출 시 결과를 받을 때까지 **스레드가 블로킹**됨
- 블로킹된 스레드는 다른 작업을 처리할 수 없어 리소스 낭비 발생

### 4. CompletableFuture를 이용한 비동기 콜백

`CompletableFuture`는 콜백을 통해 블로킹 문제를 해결하려 했지만, 새로운 문제들이 발생합니다.
```kotlin
fun processDataAsync() {
    CompletableFuture.supplyAsync({
        Thread.sleep(1000L)
        "사용자 정보"
    }, executorService)
    .thenApplyAsync({ userInfo ->
        Thread.sleep(1000L)
        "주문 정보: $userInfo"
    }, executorService)
    .thenApplyAsync({ orderInfo ->
        Thread.sleep(1000L)
        "결제 정보: $orderInfo"
    }, executorService)
    .thenAccept { result ->
        println("최종 결과: $result")
    }
    .exceptionally { e ->
        println("에러 발생: ${e.message}")
        null
    }
}
```

**문제점:**
- **콜백 체이닝으로 인한 가독성 저하** (콜백 지옥)
- 예외 처리가 복잡하고 누락되기 쉬움
- 디버깅이 어려움 (스택 트레이스 추적 곤란)
- 순차적 코드처럼 보이지 않아 로직 파악이 어려움

기존 멀티 스레드 방식에서는 Thread -> ExecutorService -> Future -> CompletableFuture 순으로 기존의 문제점을 해결하려고 있지만
여전히 아쉬운 부분이 존재합니다

하지만 Java 21부터 정식으로 도입된 `Virtual Thread`를 사용하면 블로킹 문제를 해결할수 있기 때문에 `Java` 진영에서 I/O 블로킹이 주된 문제일 경우 `Virtual Thread`
를 고민해 볼 수 있을것 같습니다.



