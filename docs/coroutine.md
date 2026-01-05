# 코루틴

현재까지 공부하고 개발했었던 프로젝트에서는 스레드 기반의 비동기 작업을 처리했었습니다. 하지만 코틀린에서는 코루틴이라는 경량 스레드를 제공하여 더 효율적인 비동기 프로그래밍이 가능하다고 하여
어떤 점이 더 기존 스레드기반보다 나은지 알아보았습니다.

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

기존 멀티 스레드 방식에서는 Thread → ExecutorService → Future → CompletableFuture 순으로 
문제점을 개선해왔지만, 여전히 **스레드 블로킹**이라는 근본적인 문제가 남아있습니다.

## 코루틴의 핵심: 스레드 블로킹 해결

코루틴은 **"대기 중에도 스레드를 블로킹하지 않는다"** 는 점에서 기존 방식과 다릅니다.

I/O 작업을 기다리는 동안 스레드를 반납하여 다른 작업에 사용할 수 있게 함으로써,
적은 수의 스레드로도 수만 개의 동시 작업을 효율적으로 처리할 수 있습니다.
```kotlin
// Future 방식 - 스레드 블로킹
fun futureExample(): String {
    val future = executorService.submit {
        Thread.sleep(2000L)  // ❌ 스레드 블로킹
        "결과"
    }
    return future.get()  // ❌ 호출 스레드도 블로킹
}

// 코루틴 방식 - 스레드 블로킹 없음
suspend fun coroutineExample(): String {
    return withContext(Dispatchers.IO) {
        delay(2000L)  // ✅ 코루틴만 일시정지, 스레드는 재사용 가능
        "결과"
    }
}
```

이러한 **스레드 재사용**으로 인해 동일한 작업을 훨씬 적은 스레드로 처리할 수 있으며,
시스템 자원을 효율적으로 사용할 수 있습니다.

## 꼭 코루틴 만이 블로킹 문제를 해결하는가?

Java 21부터 정식 도입된 **Virtual Thread** 역시 코루틴과 유사하게 스레드 블로킹 문제를 해결합니다.
순수 Java 프로젝트라면 Virtual Thread를 고려해보는 것도 좋은 선택이 될 수 있을 것같습니다.

## 정리

이번 프로젝트에 `gRPC` 통신을 적용하면서 `Kotlin gRPC`가 코루틴 기반으로 동작한다는 것을 알게 되었습니다.

코루틴의 핵심은 **스레드 블로킹 없이 대기**할 수 있다는 점입니다. 
이를 통해 적은 수의 스레드로도 수많은 동시 작업을 효율적으로 처리할 수 있으며,
다음 단계로 실제 프로젝트의 MSA 환경에서 여러 서비스를 조회하는 API에 코루틴을 적용해볼 예정입니다.


