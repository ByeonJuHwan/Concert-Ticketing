package dev.concert.application.concert.facade

import dev.concert.application.concert.ConcertFacade
import dev.concert.application.concert.dto.ConcertReservationDto
import dev.concert.domain.service.reservation.ReservationService
import dev.concert.domain.service.user.UserService
import dev.concert.domain.repository.ConcertRepository
import dev.concert.domain.entity.ConcertEntity
import dev.concert.domain.entity.ConcertOptionEntity
import dev.concert.domain.entity.SeatEntity
import dev.concert.domain.entity.UserEntity
import dev.concert.domain.entity.status.ReservationStatus
import dev.concert.domain.entity.status.SeatStatus
import dev.concert.domain.repository.ReservationRepository
import dev.concert.domain.repository.SeatRepository
import dev.concert.domain.repository.UserRepository
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDateTime
import java.util.concurrent.CompletableFuture

@SpringBootTest
class ConcertFacadeTest {

    @Autowired
    private lateinit var concertFacade: ConcertFacade

    @Autowired
    private lateinit var concertRepository: ConcertRepository

    @Autowired
    private lateinit var userRepository : UserRepository

    @Autowired
    private lateinit var reservationService: ReservationService

    @Autowired
    private lateinit var userService : UserService

    @Autowired
    private lateinit var seatRepository: SeatRepository

    @Autowired
    private lateinit var reservationRepository: ReservationRepository

    @BeforeEach
    fun setUp() {
        userService.saveUser(UserEntity(name = "user1"))
        userService.saveUser(UserEntity(name = "user2"))
        userService.saveUser(UserEntity(name = "user3"))
        userService.saveUser(UserEntity(name = "user4"))
        userService.saveUser(UserEntity(name = "user5"))
        userService.saveUser(UserEntity(name = "user6"))
        userService.saveUser(UserEntity(name = "user7"))
        userService.saveUser(UserEntity(name = "user8"))
        userService.saveUser(UserEntity(name = "user9"))
        userService.saveUser(UserEntity(name = "user10"))

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

    @AfterEach
    fun tearDown() {
        concertRepository.deleteAll()
        seatRepository.deleteAll()
        userRepository.deleteAll()
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
        val seat = seatRepository.findById(1L)?: throw Exception("seat not found")
        val reservations = reservationRepository.findAll()
        assertThat(seat).isNotNull
        assertThat(seat.seatStatus).isEqualTo(SeatStatus.TEMPORARILY_ASSIGNED)
        assertThat(reservations).hasSize(1)
    }

    @Test
    fun `콘서트 좌석 예약 10명 동시성 테스트`() {
        // given
        val seatId = 1L
        val userIds = (1L..10L).toList() // 10명의 사용자 ID 리스트

        // 10개의 예약 요청 생성
        val requests = userIds.map { userId ->
            ConcertReservationDto(
                userId = userId,
                seatId = seatId
            )
        }

        // when
        val startTime = System.currentTimeMillis() // 시간 측정 시작
        requests.map { request ->
            CompletableFuture.runAsync {
                try {
                    concertFacade.reserveSeat(request)
                } catch (e: Exception) {
                    println(e.message)
                }
            }
        }.forEach { it.join() }

        val endTime = System.currentTimeMillis() // 시간 측정 종료

        // 소요 시간 계산
        val elapsedTime = endTime - startTime
        println("Elapsed time: $elapsedTime ms")

        // then
        val seat = seatRepository.findById(1L)?: throw Exception("seat not found")
        val reservations = reservationRepository.findAll()
        assertThat(seat).isNotNull
        assertThat(seat.seatStatus).isEqualTo(SeatStatus.TEMPORARILY_ASSIGNED)
        assertThat(reservations).hasSize(1)
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

        val seat = seatRepository.save(
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