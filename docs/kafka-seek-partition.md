# Kafka offset 이동하기

분산 환경에서 Kafka 를 사용하다 보면 특정 순간, 혹은 특정 offset 부터 다시 메시지를 재처리 하고 싶은 순간이 올 수 있습니다.

5분 전 메시지를 다시 처리하고 싶다던가, 100번 Offset 메시지부터 다시 처리하고 싶을 때 특정 토픽, 파티션의 메시지로 어떻게 offset 을 이동해서 다음 poll() 부터 처리가 가능할까요?

간단한 방법으로는 이런 방법이 있습니다

브로커는 그룹 ID 별로 offset 을 관리하므로 그룹명을 바꾸면 브로커 입장에서 완전히 새로운 컨슈머가 됩니다. offset 이력이 없으니 auto.offset.reset: earliest 설정에 따라 처음부터 읽기 시작합니다.

아니면 카프카 컨테이너가 파티션을 할당받는 순간 여기에 하드코딩으로 seek를 직접 박아두면 서비스가 뜰 때마다 지정한 위치부터 읽습니다

```kotlin
// 예시코드
override fun onPartitionsAssigned(
  assignments: Map<TopicPartition, Long>,
  callback: ConsumerSeekCallback
) {
  assignments.keys.forEach { tp ->
      callback.seekToTimestamp(tp.topic(), tp.partition(), 1779177600000L)
  }
}
```

하지만 이 방법의 문제점은 아래와 같습니다.

파티션이 3개일 때 그룹명을 변경할 경우 파티션 0, 2 도 처음부터 다시 읽습니다. timestamp 적용할 때도 동일하게 다른 파티션도 영향을 받습니다.

하지만 `AbstractConsumerSeekAware` 추상 클래스를 사용하면 파티션 1개만 골라서 seek 를 할 수 있습니다.

저는 `AbstractConsumerSeekAware` 를 사용해서 개발자가 직접 특정 토픽의 파티션의 offset 을 이동할 수 있도록 해보았습니다.

## AbstractConsumerSeekAware 내부 구현

우선 Kafka 에서 제공하는 `ConsumerSeekAware` 인터페이스를 살펴보면 인터페이스가 제공하는 콜백 메소드는 다음과 같습니다.

```kotlin
interface ConsumerSeekAware {

    // 파티션이 할당될 때 호출 — 여기서 ConsumerSeekCallback 을 저장하는 것이 핵심
    fun onPartitionsAssigned(assignments: Map<TopicPartition, Long>, callback: ConsumerSeekCallback)

    // 컨슈머가 poll 할 메시지가 없어 idle 상태일 때 호출
    fun onIdleContainer(assignments: Map<TopicPartition, Long>, callback: ConsumerSeekCallback)

    // 파티션이 회수될 때 호출 (리밸런싱 등)
    fun onPartitionsRevoked(partitions: Collection<TopicPartition>)
}
```
직접 구현해서 사용하려면 `TopicPartition → ConsumerSeekCallback` 매핑을 직접 관리해줘야 하지만 이미 `AbstractConsumerSeekAware` 에서 사용하기 좋게 다 구현을 해놓았습니다.

![ConsumerSeekAware 구현](/docs/image/ConsumerSeekAware.png)

## 실제 Callback 이 등록되고 seek 하는 내부 과정

이제 실제로 `KafkaMessageListenerContainer` 부터 어떤 과정을 통해서 동작을 하는지 전체적인 흐름을 따라가 보겠습니다.

다소 혼란스러울 수 있는 부분이 있지만 코드를 잘 따라가다 보면 동작이 이해됩니다.

카프카 컨테이너가 파티션을 할당 받으면 `seekPartitions()` 메소드가 실행됩니다
```java
private void seekPartitions(Collection<TopicPartition> partitions, boolean idle) {
    this.consumerSeekAwareListener.registerSeekCallback(this);
    Map<TopicPartition, Long> current = new HashMap<>();
    for (TopicPartition topicPartition : partitions) {
        current.put(topicPartition, ListenerConsumer.this.consumer.position(topicPartition));
    }
    if (idle) {
        this.consumerSeekAwareListener.onIdleContainer(current, this.seekCallback);
    }
    else {
        this.consumerSeekAwareListener.onPartitionsAssigned(current, this.seekCallback);
    }
}
```

여기서 `this` 는 무엇을 의미할까요? 여기서 `this` 는 `KafkaMessageListenerContainer` 내부 클래스인 `ListenerConsumer` 입니다.

로직을 차근차근 따라가다 보면 `this.consumerSeekAwareListener.onPartitionsAssigned(current, this.seekCallback);` 에서 `this.seekCallback` 이 무엇인지 궁금해집니다. 내부 구현을 보면

`this.seekCallback` 의 경우 `InitialOrIdleSeekCallback` 객체입니다.

`InitialOrIdleSeekCallback` 객체를 살펴보면 이미 내부적으로 저희가 사용할 메소드들이 다 구현되어있습니다

```java
@Override
public void seekToTimestamp(String topic, int partition, long timestamp) {
    Consumer<K, V> consumerToSeek = ListenerConsumer.this.consumer;
    Map<TopicPartition, OffsetAndTimestamp> offsetsForTimes = consumerToSeek.offsetsForTimes(
            Collections.singletonMap(new TopicPartition(topic, partition), timestamp));
    offsetsForTimes.forEach((tp, ot) -> {
        if (ot != null) {
            consumerToSeek.seek(tp, ot.offset());
        }
    });
}
```

언뜻 보면 즉시 컨슈머에 offset 을 변경해서 동작하는 것처럼 보입니다.

실제로는 다음 poll() 과정에서 지연되어 실행되어야 하는데 위 로직을 보면 그렇게 구현되어 있는 것 같지는 않습니다.

원인을 파악하기 위해 다시 분석해 보겠습니다.

다시 `seekPartitions()` 메소드를 살펴보면 `this.consumerSeekAwareListener.registerSeekCallback(this);` 가 제일 첫 줄입니다. 여기서 `this` 는 `ListenerConsumer` 입니다.

그리고 idle 값에 따라 `this.seekCallback` 을 넘깁니다. 이때 메소드를 따라가 보면 메소드를 오버라이드하고 있는 `AbstractConsumerSeekAware` 클래스를 확인할 수 있습니다.

오버라이드하고 있는 메소드를 살펴보면 아래와 같습니다.

```java
@Override
public void registerSeekCallback(ConsumerSeekCallback callback) {
    // Map 에 현재 스레드를 키로 callBack this 로 넘어온 ListenerConsumer 를 저장합니다 (ListenerConsumer는 ConsumerSeekCallback을 구현하고 있습니다)
    this.callbackForThread.put(Thread.currentThread(), callback);
}

@Override
// ConsumerSeekCallback 으로 InitialOrIdleSeekCallback 객체가 넘어옵니다
public void onPartitionsAssigned(Map<TopicPartition, Long> assignments, ConsumerSeekCallback callback) { 
    // 위 메소드에서 저장한 ListenerConsumer 을 꺼냅니다
    ConsumerSeekCallback threadCallback = this.callbackForThread.get(Thread.currentThread());
    if (threadCallback != null) {
        assignments.keySet().forEach(tp -> {
           // ListenerConsumer 의 callBack 동작들을 저장합니다
            this.topicToCallbacks.computeIfAbsent(tp, key -> new ArrayList<>()).add(threadCallback);
            this.callbackToTopics.computeIfAbsent(threadCallback, key -> new LinkedList<>()).add(tp);
        });
    }
}
```

위 동작으로 결국 `AbstractConsumerSeekAware` 를 상속하는 경우 `ListenerConsumer` 가 구현하고 있는 콜백 동작으로 동작하도록 되어있습니다.

그럼 `ListenerConsumer` 의 `seekToTimestamp` 는 어떻게 구현되어있을까요?

```java
@Override
public void seekToTimestamp(String topic, int partition, long timestamp) {
    // this.seek = private final BlockingQueue<TopicPartitionOffset> seeks = new LinkedBlockingQueue<>();
    this.seeks.add(new TopicPartitionOffset(topic, partition, timestamp, SeekPosition.TIMESTAMP));
}
```

이제 비로소 예상한 대로 큐에 토픽, 파티션, timestamp 를 넣습니다.

이제 카프카 run() 메소드에서 예상한 대로 seek → poll() 순서로 동작합니다.

사용자 코드를 마지막으로 살펴보겠습니다

```kotlin
@Component
class UserConsumer : AbstractConsumerSeekAware() {
    fun seekPartitionByTimeStamp(topic: String, partition: Int, timestamp: Long) {
        getSeekCallbacksFor(TopicPartition(topic, partition))
            ?.forEach { callback ->
                log.info("seek to timestamp — topic :{}, partition: {}, timestamp: {}", topic, partition, timestamp)
                callback.seekToTimestamp(topic, partition, timestamp)
            }
    }
}
```

`AbstractConsumerSeekAware` 에 구현된 대로 요청한 값이 큐에 저장됩니다.


## 아직 남은 문제점

실제로 사용자 RestApi 요청을 보내본 결과 지정한 timestamp 부터 정상적으로 메시지를 재처리하는 것을 확인할 수 있었습니다

하지만 `AbstractConsumerSeekAware` 을 사용하여 런타임에서도 사용자 요청으로 원하는 파티션의 메시지의 offset 조절이 가능하게 되었지만, 내부 구현만 봐도 이 인스턴스에 연결된 파티션만 가능합니다.

예를 들어, 스케일아웃 상황의 경우 인스턴스 1에는 0번 파티션이 인스턴스 2에는 1번 파티션이 있다고 했을 때 사용자 요청으로 인스턴스 1번에서 1번 파티션을 조절하고 싶어도 연결되어 있는 파티션이 아니기 때문에 불가능합니다.

이 문제를 해결하기 위한 방법은 여러 가지가 있지만, 여기서는 Spring Cloud Config Bus 를 사용한 방법을 알아보겠습니다. [spring-cloud-config 적용 문서](/docs/config/spring-cloud-config.md)

### Spring Cloud Config Bus 사용해서 분산환경에서 Partition Seek 하기

이미 ConfigServer 가 구성되어 있으므로, 설정 변경 이벤트를 수신할 리스너만 추가하면 됩니다

```kotlin
class UserConsumer : AbstractConsumerSeekAware() {
    companion object {
        private val SEEK_KEYS = setOf(
            "spring.kafka.seek.topic",
            "spring.kafka.seek.partition",
            "spring.kafka.seek.timestamp",
        )
    }
    
    @EventListener
    fun onSeekPartitionChange(event: EnvironmentChangeEvent) {
        if (event.keys.none { it in SEEK_KEYS}) return

        val seekPartition = environment.getProperty<Int>("spring.kafka.seek.partition") ?: return
        val seekTimestamp = environment.getProperty<Long>("spring.kafka.seek.timestamp") ?: return
        val seekTopic = environment.getProperty<String>("spring.kafka.seek.topic") ?: return

        seekPartitionByTimeStamp(seekTopic, seekPartition, seekTimestamp)
    }
}
```

위 코드를 통해 컨슈머가 여러 대인 환경에서도 외부 레포지토리의 설정 변경만으로 메시지의 offset 을 되돌릴 수 있습니다

이로써 REST API 와 Spring Cloud Config Bus 를 함께 활용하면 단일 인스턴스와 멀티 인스턴스 환경 모두에서 런타임에 offset 조절이 가능합니다.

## 정리하며

이로써 운영 환경에서 파티션의 offset 을 런타임에서 동적으로 변경하는 방법을 살펴보았습니다

단순 메시지 재처리 외에도, 개발자가 특정 구간의 메시지를 직접 조회하는 등 활용 방법에 따라 다양하게 사용할 수 있습니다

Spring Cloud Config Bus 를 사용하기 어려운 환경이라면 Redis Pub/Sub 이나 DB 폴링 등 현재 인프라 환경에 맞는 방법을 활용하면 좋을 것 같습니다.