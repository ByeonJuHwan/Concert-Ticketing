# Spring Cloud Config 를 통한 배압조절 도입


어느 날 갑자기 서버에 폭발적인 요청이 들어왔을 때, 컨슈머에서 처리하는 DB CPU가 트래픽으로 인해 80~90%까지 치솟고 있을 때 어떻게 처리할까요?

위 질문을 듣고 '서버 증설? 스케일업? 어떻게 해야 하지?' 라는 여러 생각이 들었고, 결국 컨슈머를 늘린다는 결정을 했을 때,
그러면 `concurrency` 를 올려야겠다고 판단했을 때 다시 이런 질문을 마주했습니다.

런타임 환경에서 설정을 변경하고 싶을 때 어떻게 해야 할까요? 라는 질문을 받고 실제로 고민을 해보았을 때, 어떻게 하면 다시 배포하지 않고 설정 변경이 가능한 건지?

이 고민에서부터 시작했습니다. 처음에는 아예 방법이 떠오르지 않아서 '배포 말고 어떻게 런타임에서 설정을 변경하지?' 라는 생각만 했습니다.

하지만 1가지 간단한 예시를 듣고 나서 Spring Cloud Config 를 도입해보게 되었습니다.

1가지 예시는 바로 설정값들을 DB 에 저장해 놓는 것입니다. 설정값을 외부에서 관리하고 주입해 줄 수 있고, 외부 저장소의 변경으로 내부 환경을 변화시킬 수 있다면
런타임에서 설정을 바꿀 수 있을 것입니다.

그러면 Spring 진영에서는 이 설정에 대한 외부 저장소를 어떻게 처리할까 찾아보니 `Spring Cloud Config` 가 있었습니다.

## Spring Cloud Config 란?

분산 시스템에서 설정값을 외부화해서 한 곳에서 관리하기 위한 서버/클라이언트 구조를 말합니다.

즉, MSA 같은 환경에서 각 서비스의 application.yml 을 일일이 빌드/배포로 바꾸는 게 아니라 Config-Server 라는 중앙 저장소가 들고 있고
각 서비스는 클라이언트로부터 그 설정을 받아오는 구조입니다.

Spring 의 Environment / PropertySource 추상화 위에 그대로 매핑되기 때문에, 클라이언트 입장에서는 그냥 `@Value`, `@ConfigurationProperties` 그대로 사용하면 됩니다.

실제로 이번에 `Config-Service` 라고 설정을 서빙해 주는 서버를 만들어 봤는데 정말 간단하게 어노테이션과 yml 설정으로 끝났습니다.

이후 실제 설정들을 어디에 저장할지에 대한 내용은 공식 문서를 확인해 본 결과 아래와 같았습니다.

- Git (기본값)
- AWS (Secrets Manager, S3...)
- DB (JDBC, Mongo..)

이제 실제로 이 설정들을 어떤 방법으로 서빙할지에 대해서도 공식문서에서는

1. 가장 단순한 방법으로 Pull 방식. 클라이언트가 시작할 때 Config Server 에 붙어서 가져오는데, 시작 시점에서만 가져오기 때문에 런타임 중 설정을 바꿔도 반영이 안 됩니다.
2. 개별 인스턴스 갱신 - `/actuator/refresh` 호출로 `@RefreshScope` 빈을 갱신. 다만 이건 그 인스턴스 하나만 갱신되기 때문에 MSA 환경에서 10개면 10번 호출해야 합니다.
3. Spring Cloud Bus - 한 번에 전체 브로드캐스트 하는 방법으로 메시지 브로커 위에서 `/actuator/busrefresh` 를 한 번 호출하면 토픽을 구독하는 모든 클라이언트가 갱신됩니다.

저는 3번 방법을 사용해서 Kafka 설정을 Runtime 환경에서 조절할 수 있도록 해보겠습니다.


## Spring Config Server

이 서버는 결국 설정을 서빙해 주는 서버입니다. 실제 구현은 아래와 같이 진행했습니다.

```kotlin
@EnableConfigServer // 어노테이션 하나로 ConfigServer 등록 완료
@SpringBootApplication
class ConfigServiceApplication

fun main(args: Array<String>) {
    runApplication<ConfigServiceApplication>(*args)
}
```

이후 기본적인 설정은 공식문서를 참고해서 Yml 파일을 설정했습니다
```yaml
spring:
  application:
    name: Config-Server
  cloud:
    config:
      server:
        git:
          # 어떤 GitHub Repository 에서 설정을 가져올건지 Path 설정
          uri: https://github.com/ByeonJuHwan/config-repo.git
          default-label: main
        # OS 프로세스나 무언가가 로컬 디렉토리를 건드리먼 "dirty" 상태가됨 이때는 Pull이 실패함
        # 이 설정은 dirty 상태여도 강제로 reset --hard 후 Pull
        force-pull: true
        # 기본은 lazy clone. 첫 요청시 클론을 시도해서 git uri 오타등이 런타입환경에서 발견됨 -> 또 수정해서 배포해야함
        # 이를 막기위해서 부팅시 바로 시도 -> 빠르게 실패해서 배포 파이프라인에 잡힘
        clone-on-start: true
        # git이 느려지거나 장애상황에서 무한대기하면 모든 스레드가 다묶이는걸 방지하기 위해 timeout 설정 (기본값 5)
        timeout: 5
    bus:
      enabled: true
    stream:
      binder:
        brokers: localhost:9092

  kafka:
    bootstrap-servers: localhost:9092

management:
  endpoints:
    web:
      exposure:
        include: busrefresh, health, info

server:
  port: 8888
```

이제 수동으로 `/actuator/busrefresh` API 호출을 통해서 SpringCloudBus 토픽에 메시지가 발행되는지 확인해보겠습니다.

![스프링버스이벤트](/docs/image/spring-bus-event.png)

확인해보면 `RefreshRemoteApplicationEvent` 를 통해서 refresh 하라는 이벤트가 발행되었고, 해당 이벤트에 대한 `AckRemoteApplicationEvent` ack 이벤트도 잘 쌓이고 있습니다.


## Spring Config Client 내부 동작

위 테스트로 RefreshRemoteApplicationEvent 가 발행되면 설정이 변경되었다는 이벤트를 내부적으로 처리해야 합니다.

```java
// RefreshListener.java
public class RefreshListener implements ApplicationListener<RefreshRemoteApplicationEvent> {
  /*윗 내용 생략*/
  @Override
  public void onApplicationEvent(RefreshRemoteApplicationEvent event) {
    log.info("Received remote refresh request.");
    if (serviceMatcher.isForSelf(event)) {
      Set<String> keys = this.contextRefresher.refresh();
      log.info("Keys refreshed " + keys);
    }
    else {
      log.info("Refresh not performed, the event was targeting " + event.getDestinationService());
    }
  }
}
```

spring-bus 에서는 내부적으로 `RefreshListener` 가 `RefreshRemoteApplicationEvent` 를 받은 뒤에 `ContextRefresher` 클래스의 `refresh()` 메소드를 실행합니다.

```java
// ContextRefresher.java
public synchronized Set<String> refresh() {
    Set<String> keys = refreshEnvironment();
    this.scope.refreshAll();
    return keys;
}

public synchronized Set<String> refreshEnvironment() {
    Map<String, Object> before = getCurrentEnvironmentProperties(); // 옛날 버전의 설정
    updateEnvironment(); // 새로운 설정으로 update
    Set<String> keys = changes(before, getCurrentEnvironmentProperties()).keySet();
    this.context.publishEvent(new EnvironmentChangeEvent(this.context, keys)); // EnvironmentChangeEvent 에 변경된 키값등을 넣어서 발행
    return keys;
}
```

refresh() 메소드 내부에서는 새로운 설정으로 Environment 를 업데이트하고 `EnvironmentChangeEvent` 를 발행합니다.

그러면 이제 저희는 `EnvironmentChangeEvent` 이벤트를 수신하고 변경된 설정을 적용만 해 주면 됩니다.

실제로 테스트해 보기 위해서 외부에 별도로 있는 config-repo 의 yml 파일을 아래와 같이 넣어서 변경한 뒤 Push 했습니다.

```yaml
test:
  message: "Hello World!!"

spring:
  kafka:
    listener:
      concurrency: 2
```
```kotlin
@EventListener
fun onConcurrencyChange(event: EnvironmentChangeEvent) {
    event.keys
        .stream()
        .forEach { log.info("카프카 설정 변경 Key : {}", it) }
}
```
```text
-/actuator/busrefresh 를 호출한 다음 콘솔창

INFO 88569 --- [concert-service] [container-0-C-1] o.k.c.a.in.consumer.ConfigConsumer       : 카프카 설정 변경 Key : config.client.version
INFO 88569 --- [concert-service] [container-0-C-1] o.k.c.a.in.consumer.ConfigConsumer       : 카프카 설정 변경 Key : test.message
INFO 88569 --- [concert-service] [container-0-C-1] o.k.c.a.in.consumer.ConfigConsumer       : 카프카 설정 변경 Key : spring.kafka.listener.concurrency
```
변경된 Key 값은 알고 있으니 이제 이 Key 값으로 새롭게 업데이트된 `Environment` 에서 key 를 가지고 value 를 가져오면 됩니다.

```kotlin
@EventListener
fun onConcurrencyChange(event: EnvironmentChangeEvent) {
    event.keys
        .stream()
        .forEach { log.info("Kafka 변경 내용들 : {}", environment.getProperty(it)) }
}
```
```text
INFO 30286 --- [concert-service] [container-0-C-1] o.k.c.a.in.consumer.ConfigConsumer       : Kafka 변경 내용들 : 95de196288acdbf3735ff20c94012b893fd5d3a7
INFO 30286 --- [concert-service] [container-0-C-1] o.k.c.a.in.consumer.ConfigConsumer       : Kafka 변경 내용들 : Hello World!!
INFO 30286 --- [concert-service] [container-0-C-1] o.k.c.a.in.consumer.ConfigConsumer       : Kafka 변경 내용들 : 2
```

## KafkaListener 설정 런타임에 변경하기

변경된 설정으로 Kafka 설정을 적용하려면 내부 동작에 대해서 먼저 알아야 합니다.

핵심 내용으로는 `ConcurrentMessageListenerContainer` 내부에서 설정한 concurrency 만큼 `KafkaMessageListenerContainer` 를 생성하고 있고,
내부적으로 pause(), start() 를 하는 것으로 보아 개발자가 임의로 해당 빈을 stop(), start() 할 수 있다는 것을 알 수 있습니다.

```java
// ConcurrentMessageListenerContainer.java
@Override
protected void doStart() {
    /*위 메소드 생략*/
    for (int i = 0; i < this.concurrency; i++) {
        KafkaMessageListenerContainer<K, V> container =
                constructContainer(containerProperties, topicPartitions, i);
        configureChildContainer(i, container);
        if (isPauseRequested()) {
            container.pause();
        }
        container.start();
        this.containers.add(container);
    }
}
```

결국 변경하고자 하는 `ConcurrentMessageListenerContainer` 를 가져와서 값을 변경하고 stop(), start()만 하면 변경 내용이 런타임에 반영됩니다.

```kotlin
@EventListener
fun onConcurrencyChange(event: EnvironmentChangeEvent) {
    if ("spring.kafka.listener.concurrency" !in event.keys) return

    val newConcurrency = environment.getProperty<Int>(
        "spring.kafka.listener.concurrency", 1
    )

    val container = kafkaListenerEndpointRegistry
        .getListenerContainer("user-created-listener") // 설정을 변경하고자 하는 KafkaListener
            as? ConcurrentMessageListenerContainer<*, *>
        ?: return

    container.stop()
    container.concurrency = newConcurrency
    container.start()
    log.info("Kafka concurrency 변경 완료: {}", newConcurrency)
}
```

변경 후 Kafka UI 를 통해서 확인해본 결과 해당 토픽에 붙어 있는 consumer 의 값이 동적으로 변경된 것을 확인할 수 있었습니다.

**설정 변경 전**

![](/docs/image/consumer1.png)

**설정 변경 후**

![](/docs/image/consumer3.png)

## 정리하며

런타임 환경, 즉 운영 중인 서버에서 급하게 이슈 대응을 해야 할 경우 코드 수정 후 배포까지는 너무 많은 시간이 걸립니다.

이때 손쉽게 설정 변경을 통해서 배압 조절을 할 수 있는 환경을 만들어 놓는 경험을 해 볼 수 있었습니다.