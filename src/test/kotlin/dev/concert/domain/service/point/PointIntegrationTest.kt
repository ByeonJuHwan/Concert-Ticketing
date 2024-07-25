package dev.concert.domain.service.point

import dev.concert.domain.entity.ConcertEntity
import dev.concert.domain.entity.ConcertOptionEntity
import dev.concert.domain.entity.ReservationEntity
import dev.concert.domain.entity.SeatEntity
import dev.concert.domain.entity.UserEntity
import dev.concert.domain.repository.ConcertRepository
import dev.concert.domain.repository.ReservationRepository
import dev.concert.domain.repository.SeatRepository
import dev.concert.domain.service.payment.PaymentService
import dev.concert.domain.service.user.UserService
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDateTime
import java.util.concurrent.CompletableFuture

@SpringBootTest
class PointIntegrationTest {

    @Autowired
    private lateinit var pointService: PointService

    @Autowired
    private lateinit var paymentService: PaymentService

    @Autowired
    private lateinit var reservationRepository: ReservationRepository

    @Autowired
    private lateinit var seatRepository: SeatRepository

    @Autowired
    private lateinit var concertRepository: ConcertRepository

    @Autowired
    private lateinit var userService : UserService

    @BeforeEach
    fun setUp() {
        // given
        val user = userService.saveUser(UserEntity("변주환"))
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
                price = 1000,
                seatNo = 1,
            )
        )

        val expiresAt = LocalDateTime.now().plusMinutes(5)

        val reservation = ReservationEntity(
            user = user,
            seat = seat,
            expiresAt = expiresAt,
        )

        // 예약 정보를 저장한다
        reservationRepository.saveReservation(reservation)

        pointService.chargePoints(user,1000)
    }

    @Test
    fun `유저 포인트 저장 차감 동시성 테스트`() {
        // given
        val user = userService.getUser(1L)
        val reservation = reservationRepository.findById(1L) ?: throw RuntimeException("예약 없음")

        // when
        CompletableFuture.allOf(
            CompletableFuture.runAsync {
                paymentService.processReservationPayment(reservation.id)
            },
            CompletableFuture.runAsync {
                pointService.chargePoints(user, 300)
            },
            CompletableFuture.runAsync {
                pointService.chargePoints(user, 200)
            }
        ).join()

        // then
        val currentPoint = pointService.getCurrentPoint(user)
        assertEquals(1000 - 1000 + 300 + 200, currentPoint.point)
    }
}