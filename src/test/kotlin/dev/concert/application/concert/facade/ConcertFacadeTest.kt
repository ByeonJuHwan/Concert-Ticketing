package dev.concert.application.concert.facade

import dev.concert.application.concert.ConcertFacade
import dev.concert.application.concert.dto.ConcertReservationDto
import dev.concert.domain.service.reservation.ReservationService
import dev.concert.domain.service.seat.SeatService
import dev.concert.domain.service.user.UserService
import dev.concert.domain.repository.ConcertRepository
import dev.concert.domain.entity.ConcertEntity
import dev.concert.domain.entity.ConcertOptionEntity
import dev.concert.domain.entity.SeatEntity
import dev.concert.domain.entity.UserEntity
import dev.concert.domain.entity.status.ReservationStatus
import dev.concert.domain.entity.status.SeatStatus
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDateTime
import java.util.concurrent.CompletableFuture

//@Transactional
@SpringBootTest
class ConcertFacadeTest {

    @Autowired
    private lateinit var concertFacade: ConcertFacade

    @Autowired
    private lateinit var concertRepository: ConcertRepository

    @Autowired
    private lateinit var seatService: SeatService

    @Autowired
    private lateinit var reservationService: ReservationService

    @Autowired
    private lateinit var userService : UserService

    @BeforeEach
    fun setUp() {
        userService.saveUser(UserEntity(name = "user1"))
        userService.saveUser(UserEntity(name = "user2"))

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
        // when
        val reservation = concertFacade.reserveSeat(
            ConcertReservationDto(
                userId = 1L,
                seatId = 1L,
            )
        )

        // then
        assertNotNull(reservation)
        assertThat(reservation.status).isEqualTo(ReservationStatus.PENDING)
        assertThat(reservation.reservationExpireTime).isAfter(LocalDateTime.now())
    }

    @Test
    fun `콘서트 좌석 예약 동시성 테스트`() {
        // given
        val request1 = ConcertReservationDto(
            userId = 1L,
            seatId = 1L,
        )

        val request2 = ConcertReservationDto(
            userId = 2L,
            seatId = 1L,
        )

        // when
        CompletableFuture.allOf(
            CompletableFuture.runAsync{
                try{
                    concertFacade.reserveSeat(request1)
                }catch (e: Exception){
                    println(e.message)
                }
            },
            CompletableFuture.runAsync{
                try{
                    concertFacade.reserveSeat(request2)
                }catch (e: Exception){
                    println(e.message)
                }
            },
        ).join()

        // then
        val seat = seatService.getSeat(1L)
        assertThat(seat).isNotNull
        assertThat(seat.seatStatus).isEqualTo(SeatStatus.TEMPORARILY_ASSIGNED)
    }

    @Test
    fun `콘서트 좌석 예약 스케줄러가 돌아서 바로 예약한 직후는 상태를 변경하지 않는다`() {
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

        reservationService.manageReservationStatus()

        // then
        assertThat(reservation.status).isEqualTo(ReservationStatus.PENDING)
    }
}