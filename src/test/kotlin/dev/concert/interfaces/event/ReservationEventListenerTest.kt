package dev.concert.interfaces.event

import dev.concert.application.concert.ConcertFacade
import dev.concert.application.concert.dto.ConcertReservationDto
import dev.concert.domain.entity.ConcertEntity
import dev.concert.domain.entity.ConcertOptionEntity
import dev.concert.domain.entity.SeatEntity
import dev.concert.domain.entity.UserEntity
import dev.concert.domain.event.reservation.ReservationEvent
import dev.concert.domain.repository.ConcertRepository
import dev.concert.domain.repository.SeatRepository
import dev.concert.domain.service.user.UserService
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.event.ApplicationEvents
import org.springframework.test.context.event.RecordApplicationEvents
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Transactional
@RecordApplicationEvents
class ReservationEventListenerTest {

    @Autowired
    private lateinit var concertFacade: ConcertFacade

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var seatRepository: SeatRepository

    @Autowired
    private lateinit var concertRepository: ConcertRepository

    @Autowired
    private lateinit var applicationEvents: ApplicationEvents // IDE 에서는 컴파일 오류라고 뜨지만 정상실행됩니다

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

    @Test
    fun `예약을 생성하면 1개의 이벤트를 발행한다`() {
        // given
        val userId = 1L
        val seatId = 1L

        concertFacade.reserveSeat(
            ConcertReservationDto(
                userId = userId,
                seatId = seatId,
            )
        )

        val events = applicationEvents.stream(ReservationEvent::class.java).toList()
        assertThat(events).hasSize(1)
    }
}