package dev.concert.domain.service.payment

import dev.concert.domain.entity.ConcertEntity
import dev.concert.domain.entity.ConcertOptionEntity
import dev.concert.domain.entity.PaymentEntity
import dev.concert.domain.entity.PointEntity
import dev.concert.domain.entity.ReservationEntity
import dev.concert.domain.entity.SeatEntity
import dev.concert.domain.entity.UserEntity
import dev.concert.domain.entity.status.PaymentStatus
import dev.concert.domain.entity.status.PaymentType
import dev.concert.domain.entity.status.ReservationStatus
import dev.concert.domain.entity.status.SeatStatus
import dev.concert.domain.exception.ConcertException
import dev.concert.domain.repository.PaymentRepository
import dev.concert.domain.repository.PointHistoryRepository
import dev.concert.domain.repository.PointRepository
import dev.concert.domain.repository.ReservationRepository
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class PaymentServiceImplTest {

    @Mock
    private lateinit var reservationRepository: ReservationRepository

    @Mock
    private lateinit var paymentRepository: PaymentRepository

    @Mock
    private lateinit var pointRepository: PointRepository

    @Mock
    private lateinit var pointHistoryRepository: PointHistoryRepository

    @InjectMocks
    private lateinit var paymentService: PaymentServiceImpl

    @Test
    fun `잘못된 예약 ID 를 주면 ReservationNotFoundException 을 발생시킨다`() {
        // given
        val reservationId = 1L
        given(reservationRepository.findById(reservationId)).willReturn(null)

        // when & then
        assertThrows(ConcertException::class.java) {
            paymentService.processReservationPayment(reservationId)
        }.also {
            assertEquals("존재하는 예약이 없습니다", it.message)
        }
    }

    @Test
    fun `이미 결재한 예약이면 ReservationAlreadyPaidException 를 발생시킨다`() {
        // given
        val reservationId = 1L
        val reservation = stubReservationEntity()
        reservation.changeStatus(ReservationStatus.PAID)

        given(reservationRepository.findById(reservationId)).willReturn(reservation)

        // when & then
        assertThrows(ConcertException::class.java) {
            paymentService.processReservationPayment(reservationId)
        }.also {
            assertEquals("이미 결제된 예약입니다", it.message)
        }
    }

    @Test
    fun `이미 만료된 예약이면 ReservationExpiredException 를 발생시킨다`() {
        // given
        val reservationId = 1L
        val reservation = stubReservationEntity()
        reservation.changeStatus(ReservationStatus.EXPIRED)

        given(reservationRepository.findById(reservationId)).willReturn(reservation)

        // when & then
        assertThrows(ConcertException::class.java) {
            paymentService.processReservationPayment(reservationId)
        }.also {
            assertEquals("예약이 만료되었습니다", it.message)
        }
    }

    @Test
    fun `5분이 지난 예약이면 ReservationExpiredException 을 발생시킨다`() {
        // given
        val reservationId = 1L
        val reservation = stubExpiredReservationEntity()

        given(reservationRepository.findById(reservationId)).willReturn(reservation)

        // when & then
        assertThrows(ConcertException::class.java) {
            paymentService.processReservationPayment(reservationId)
        }.also {
            assertEquals("예약이 만료되었습니다", it.message)
        }
    }

    @Test
    fun `현재 포인트가 좌석의 가격보다 적으면 NotEnoughPointException 예외를 발생시킨다`() {
        // given
        val reservationId = 1L
        val reservation = stubReservationEntity()
        val user = reservation.user

        given(reservationRepository.findById(reservationId)).willReturn(reservation)
        given(pointRepository.findByUser(user)).willReturn(null)

        // when & then
        assertThrows(ConcertException::class.java) {
            paymentService.processReservationPayment(reservationId)
        }.also {
            assertEquals("포인트가 부족합니다", it.message)
        }
    }

    @Test
    fun `예약이 정상적으로 진행되면 예약상태를 결재완료로 변경한다`() {
        // given
        val reservationId = 1L
        val reservation = stubReservationEntity()
        val user = reservation.user

        given(reservationRepository.findById(reservationId)).willReturn(reservation)
        given(pointRepository.findByUser(user)).willReturn(stubPointEntity(user))

        // when
        paymentService.processReservationPayment(reservationId)

        // then
        assertEquals(ReservationStatus.PAID, reservation.status)
    }

    @Test
    fun `예약이 정상적으로 진행되면 좌석상태를 예약 완료로 변경한다`() {
        // given
        val reservationId = 1L
        val reservation = stubReservationEntity()
        val user = reservation.user

        given(reservationRepository.findById(reservationId)).willReturn(reservation)
        given(pointRepository.findByUser(user)).willReturn(stubPointEntity(user))

        // when
        paymentService.processReservationPayment(reservationId)

        // then
        assertEquals(SeatStatus.RESERVED, reservation.seat.seatStatus)
    }

    private fun stubPaymentEntity(reservation: ReservationEntity): PaymentEntity {
        return PaymentEntity(
            reservation = reservation,
            price = reservation.seat.price,
            paymentStatus = PaymentStatus.SUCCESS,
            paymentType = PaymentType.POINT
        )
    }

    private fun stubPointEntity(user: UserEntity): PointEntity {
        return PointEntity(user, 100000)
    }

    private fun stubReservationEntity(): ReservationEntity {
        val user = UserEntity(name = "test")

        return ReservationEntity(
            user = user,
            seat = stubSeatEntity(),
            expiresAt = LocalDateTime.now().plusMinutes(5),
        )
    }

    private fun stubExpiredReservationEntity(): ReservationEntity {
        val user = UserEntity(name = "test")

        return ReservationEntity(
            user = user,
            seat = stubSeatEntity(),
            expiresAt = LocalDateTime.now().minusMinutes(5),
        )
    }

    private fun stubSeatEntity(): SeatEntity {
        val concertOption = ConcertOptionEntity(
            concert = ConcertEntity(
                concertName = "새해 콘서트",
                singer = "에스파",
                startDate = "20241201",
                endDate = "20241201",
                reserveStartDate = "20241201",
                reserveEndDate = "20241201"
            ),
            availableSeats = 50,
            concertTime = "14:00",
            concertVenue = "올림픽공원",
            concertDate = "20241201"
        )
        val seatEntity = SeatEntity(
            concertOption = concertOption,
            seatNo = 1,
            price = 100000,
        )
        return seatEntity
    }
}