package dev.concert.interfaces.event

import dev.concert.infrastructure.kafka.reservation.KafkaProducer
import dev.concert.interfaces.consumer.KafkaConsumer
import org.assertj.core.api.Assertions.*
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.annotation.DirtiesContext
import java.util.concurrent.TimeUnit

@SpringBootTest
@EmbeddedKafka(partitions = 1, brokerProperties = ["listeners=PLAINTEXT://localhost:9092"], ports = [9092])
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class KafkaIntegrationTest {

    @Autowired
    private lateinit var producer : KafkaProducer

    @Autowired
    private lateinit var consumer: KafkaConsumer

    private val log : Logger = LoggerFactory.getLogger(this.javaClass)

    @Test
    fun `카프카 메시지 전송 테스트`() {
        val topic = "test_topic"
        val message = "테스트 메시지"

        producer.send(topic, message)

        await().atMost(10, TimeUnit.SECONDS).untilAsserted {
            assertThat(consumer.lastReceivedMessage).isEqualTo(message)
            log.info("received message : ${consumer.lastReceivedMessage}")
        }
    }
}