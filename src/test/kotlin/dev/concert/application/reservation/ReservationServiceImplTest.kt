package dev.concert.application.reservation

import dev.concert.domain.ReservationRepository
import dev.concert.domain.entity.ConcertEntity
import dev.concert.domain.entity.ConcertOptionEntity
import dev.concert.domain.entity.ReservationEntity
import dev.concert.domain.entity.SeatEntity
import dev.concert.domain.entity.UserEntity
import dev.concert.domain.entity.status.ReservationStatus
import dev.concert.domain.entity.status.SeatStatus
import dev.concert.exception.ReservationAlreadyPaidException
import dev.concert.exception.ReservationExpiredException
import dev.concert.exception.ReservationNotFoundException
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class ReservationServiceImplTest {

    @Mock
    private lateinit var reservationRepository: ReservationRepository

    @InjectMocks
    private lateinit var reservationService: ReservationServiceImpl

    @Test
    fun `유저정보와 좌석정보가 주어지면 예약정보를 생성해서 저장한다`() {
        // given
        val user = UserEntity(name = "test")
        val seat = stubSeatEntity()

        assertDoesNotThrow {
            reservationService.saveReservation(user, seat)
        }
    }

    @Test
    fun `예약 ID 가 주어지면 예약 정보를 응답해준다`() {
        // given
        val reservationId = 1L
        val reservation = ReservationEntity(
            user = UserEntity(name = "test"),
            seat = stubSeatEntity(),
            expiresAt = LocalDateTime.now().plusMinutes(5)
        )
        given(reservationRepository.findById(reservationId)).willReturn(reservation)

        // when
        val result = reservationService.getReservation(reservationId)

        // then
        assertThat(result).isEqualTo(reservation)
    }

    @Test
    fun `예약 ID 가 주어졌을때 값이 없으면 ReservationNotFoundException 을 터트린다`() {
        // given
        val reservationId = 1L
        given(reservationRepository.findById(reservationId)).willReturn(null)

        assertThatThrownBy {
            reservationService.getReservation(1L)
        }.isInstanceOf(ReservationNotFoundException::class.java)
    }

    @Test
    fun `예약 만료시간이 지나면 ReservationExpiredException 에러를 터트린다`() {
        // given
        val reservation = ReservationEntity(
            user = UserEntity(name = "test"),
            seat = stubSeatEntity(),
            expiresAt = LocalDateTime.now().minusMinutes(5)
        )

        val result = reservationService.isExpired(reservation)

        assertThat(result).isTrue()
    }

    @Test
    fun `예약 만료시간이 지나면 예약 상태를 Expired 로 변경한다`() {
        // given
        val reservation = ReservationEntity(
            user = UserEntity(name = "test"),
            seat = stubSeatEntity(),
            expiresAt = LocalDateTime.now().minusMinutes(5)
        )

        // when
        reservationService.isExpired(reservation)

        assertThat(reservation.status).isEqualTo(ReservationStatus.EXPIRED)
    }

    @Test
    fun `예약 상태가 주어지면 예약 상태를 변경한다`() {
        // given
        val reservation = ReservationEntity(
            user = UserEntity(name = "test"),
            seat = stubSeatEntity(),
            expiresAt = LocalDateTime.now().plusMinutes(5)
        )

        // when
        reservationService.changeReservationStatusPaid(reservation)

        // then
        assertThat(reservation.status).isEqualTo(ReservationStatus.PAID)
    }

    @Test 
    fun `예약 상태가 Expired 가 되면 자리 상태를 AVAILABLE 로 변경한다`() { 
        // given 
        val reservation = ReservationEntity( 
            user = UserEntity(name = "test"), 
            seat = stubSeatEntity(), 
            expiresAt = LocalDateTime.now().minusMinutes(5) 
        ) 
 
        given(reservationRepository.findExpiredReservations()).willReturn(listOf(reservation)) 
        // when 
        reservationService.manageReservationStatus() 
 
        // then 
        assertThat(reservation.status).isEqualTo(ReservationStatus.EXPIRED) 
        assertThat(reservation.seat.seatStatus).isEqualTo(SeatStatus.AVAILABLE) 
    } 


    @Test
    fun `현재 예약상태가 Pending 이 아니고 결제 완료 상태라면 ReservationAlreadyPaidException 예외를 발생시킨다`() {
        // given
        val reservation = ReservationEntity(
            user = UserEntity(name = "test"),
            seat = stubSeatEntity(),
            expiresAt = LocalDateTime.now().plusMinutes(5),
        )

        reservation.changeStatus(ReservationStatus.PAID)

        assertThatThrownBy {
            reservationService.isPending(reservation)
        }.isInstanceOf(ReservationAlreadyPaidException::class.java)
    }

    @Test
    fun `현재 예약상태가 Pending 이 아니고 만료 상태라면 ReservationExpiredException 예외를 발생시킨다`() {
        // given
        val reservation = ReservationEntity(
            user = UserEntity(name = "test"),
            seat = stubSeatEntity(),
            expiresAt = LocalDateTime.now().plusMinutes(5),
        )

        reservation.changeStatus(ReservationStatus.EXPIRED)

        assertThatThrownBy {
            reservationService.isPending(reservation)
        }.isInstanceOf(ReservationExpiredException::class.java)
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
