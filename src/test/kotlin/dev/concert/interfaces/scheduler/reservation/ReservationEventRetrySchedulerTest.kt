package dev.concert.interfaces.scheduler.reservation

import dev.concert.domain.entity.outbox.ReservationEventOutBox
import dev.concert.domain.entity.status.OutBoxMsgStats
import dev.concert.domain.repository.ReservationOutBoxRepository
import org.assertj.core.api.Assertions.*
import org.awaitility.Awaitility.*
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.annotation.DirtiesContext
import java.time.Duration
import java.util.concurrent.TimeUnit

@SpringBootTest
@EmbeddedKafka(partitions = 1, brokerProperties = ["listeners=PLAINTEXT://localhost:9092"], ports = [9092])
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ReservationEventRetrySchedulerTest {

    @Autowired
    private lateinit var reservationOutBoxRepository: ReservationOutBoxRepository

    @Autowired
    private lateinit var reservationEventRetryScheduler : ReservationEventRetryScheduler

    /**
     * Stats 가 init 상태이고, 생성된지 10분이 지난 테스트 데이터 생성
     */
    @BeforeEach
    fun setup() {
        val outbox = reservationOutBoxRepository.save(ReservationEventOutBox(1L))
        reservationOutBoxRepository.updateCreatedAt11MinutesAgo(outbox)
    }

    @Test
    fun `아웃박스 이벤트중 SEND_SUCESS 가 아니고 created_at 이 10분이 지난 이벤드들을 재발행한다`() {
        // given
        val reservationId = 1L

        // when
        reservationEventRetryScheduler.reservationEventRetryScheduler()

        // Then
        await()
            .atMost(10, TimeUnit.SECONDS)
            .pollInterval(Duration.ofMillis(100))
            .untilAsserted {
                val foundOutBoxEntity = reservationOutBoxRepository.findByReservationId(reservationId)
                assertThat(foundOutBoxEntity).isNotNull
                assertThat(foundOutBoxEntity?.status).isEqualTo(OutBoxMsgStats.SEND_SUCCESS)
            }
    }
}