package dev.concert.application.concert.facade

import dev.concert.application.concert.dto.ConcertReservationDto
import dev.concert.application.concert.service.ConcertService
import dev.concert.application.seat.SeatService
import dev.concert.application.user.UserService
import dev.concert.domain.ConcertRepository
import dev.concert.domain.entity.ConcertEntity
import dev.concert.domain.entity.ConcertOptionEntity
import dev.concert.domain.entity.SeatEntity
import dev.concert.domain.entity.UserEntity
import dev.concert.domain.entity.status.ReservationStatus
import dev.concert.infrastructure.jpa.ConcertJpaRepository
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Transactional
@SpringBootTest
class ConcertFacadeTest {

    @Autowired
    private lateinit var concertFacade: ConcertFacade

    @Autowired
    private lateinit var concertRepository: ConcertRepository

    @Autowired
    private lateinit var seatService: SeatService

    @Autowired
    private lateinit var concertService: ConcertService

    @Autowired
    private lateinit var userService : UserService

    @BeforeEach
    fun setUp() {
        userService.saveUser(UserEntity(name = "test"))

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

        seatService.saveSeat(
            SeatEntity(
                concertOption = concertOption,
                price = 10000,
                seatNo = 1,
            )
        )
    }

    @Test
    fun `콘서트 목록 조회 통합 테스트`() {
        // when
        val concerts = concertFacade.getConcerts()

        // then
        assertNotNull(concerts)
        assertThat(concerts.size).isEqualTo(1)
    }

    @Test
    fun `콘서트 예약가능 날짜 조회 테스트`() {
        // given
        val concerts = concertFacade.getConcerts()
        val concert = concerts[0]

        // when
        val availableDates = concertFacade.getAvailableDates(concert.id)

        // then
        assertNotNull(availableDates)
        assertThat(availableDates.size).isEqualTo(1)
    }

    @Test
    fun `콘서트 예약가능 좌석 조회 테스트`() {
        // given
        val concerts = concertFacade.getConcerts()

        // when
        val availableSeats = concertFacade.getAvailableSeats(concerts[0].id)

        // then
        assertNotNull(availableSeats)
        assertThat(availableSeats.size).isEqualTo(1)
    }

    @Test
    fun `콘서트 좌석 예약 테스트`() {
        val user = userService.saveUser(UserEntity(name = "test"))

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

        val seat = seatService.saveSeat(
            SeatEntity(
                concertOption = concertOption,
                price = 10000,
                seatNo = 1,
            )
        )

        // when
        val reservation = concertFacade.reserveSeat(
            ConcertReservationDto(
                userId = user.id,
                seatId = seat.id,
            )
        )

        // then
        assertNotNull(reservation)
        assertThat(reservation.status).isEqualTo(ReservationStatus.PENDING)
        assertThat(reservation.reservationExpireTime).isAfter(LocalDateTime.now())
    }
}