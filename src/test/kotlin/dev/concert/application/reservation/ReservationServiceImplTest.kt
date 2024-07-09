package dev.concert.application.reservation

import dev.concert.domain.ReservationRepository
import dev.concert.domain.entity.ConcertEntity
import dev.concert.domain.entity.ConcertOptionEntity
import dev.concert.domain.entity.ReservationEntity
import dev.concert.domain.entity.SeatEntity
import dev.concert.domain.entity.UserEntity
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.any
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
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