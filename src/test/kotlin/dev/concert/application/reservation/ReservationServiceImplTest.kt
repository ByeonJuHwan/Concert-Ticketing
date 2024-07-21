package dev.concert.application.reservation

import dev.concert.domain.repository.ReservationRepository
import dev.concert.domain.entity.ConcertEntity
import dev.concert.domain.entity.ConcertOptionEntity
import dev.concert.domain.entity.ReservationEntity
import dev.concert.domain.entity.SeatEntity
import dev.concert.domain.entity.UserEntity
import dev.concert.domain.repository.SeatRepository
import dev.concert.domain.service.reservation.ReservationServiceImpl
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class ReservationServiceImplTest {

    @Mock
    private lateinit var reservationRepository: ReservationRepository

    @Mock
    private lateinit var seatRepository: SeatRepository

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
    fun `예약 상태를 변경하는 스케줄러가 돌면 상테를 바꿔주는 쿼리가 호출된다`() {
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
        verify(reservationRepository).updateReservationStatusToExpired(listOf(reservation.id))
        verify(seatRepository).updateSeatStatusToAvailable(listOf(reservation.seat.id))
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
