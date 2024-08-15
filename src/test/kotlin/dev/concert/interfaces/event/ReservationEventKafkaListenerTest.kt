package dev.concert.interfaces.event

import dev.concert.application.concert.ConcertFacade
import dev.concert.application.concert.dto.ConcertReservationDto
import dev.concert.domain.entity.ConcertEntity
import dev.concert.domain.entity.ConcertOptionEntity
import dev.concert.domain.entity.SeatEntity
import dev.concert.domain.entity.UserEntity
import dev.concert.domain.entity.status.OutBoxMsgStats
import dev.concert.domain.repository.ConcertRepository
import dev.concert.domain.repository.ReservationOutBoxRepository
import dev.concert.domain.repository.SeatRepository
import dev.concert.domain.service.user.UserService
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.annotation.DirtiesContext
import java.time.Duration
import java.util.concurrent.TimeUnit

@SpringBootTest
@EmbeddedKafka(partitions = 1, brokerProperties = ["listeners=PLAINTEXT://localhost:9092"], ports = [9092])
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ReservationEventKafkaListenerTest {

    @Autowired
    private lateinit var concertFacade: ConcertFacade

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var seatRepository: SeatRepository

    @Autowired
    private lateinit var concertRepository: ConcertRepository

    @Autowired
    private lateinit var reservationOutBoxRepository: ReservationOutBoxRepository

    @BeforeEach
    fun setUp() {
        userService.saveUser(UserEntity(name = "user"))

        val concert = concertRepository.saveConcert(
            ConcertEntity(
                concertName = "콘서트1",
                singer = "가수1",
                startDate = "20241201",
                endDate = "20241201",
                reserveStartDate = "20241201",
                reserveEndDate = "20241201",
            )
        )

        val concertOption = concertRepository.saveConcertOption(
            ConcertOptionEntity(
                concert = concert,
                concertDate = "20241201",
                concertTime = "12:00",
                concertVenue = "올림픽체조경기장",
                availableSeats = 100,
            )
        )

        seatRepository.save(
            SeatEntity(
                concertOption = concertOption,
                price = 10000,
                seatNo = 1,
            )
        )
    }

    /**
     * AFTER_COMMIT 에서 정상적으로 카프카 이벤트가 발행되어 SEND_SUCCESS 로 변경되는지 테스트
     */
    @Test
    fun `예약을 생성하고 트랜잭션이 종료되면 카프카 이벤트가 발행되어 아웃박스의 상태가 SEND_SUCCESS 로 변경된다`() {
        // given
        val userId = 1L
        val seatId = 1L
        val reservationId = 1L

        // when
        concertFacade.reserveSeat(
            ConcertReservationDto(
                userId = userId,
                seatId = seatId,
            )
        )

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