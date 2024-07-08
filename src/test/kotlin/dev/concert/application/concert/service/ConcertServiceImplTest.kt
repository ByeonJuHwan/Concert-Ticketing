package dev.concert.application.concert.service

import dev.concert.application.concert.dto.ConcertReservationDto
import dev.concert.domain.ConcertRepository
import dev.concert.domain.ReservationRepository
import dev.concert.domain.UserRepository
import dev.concert.domain.entity.ConcertEntity
import dev.concert.domain.entity.ConcertOptionEntity
import dev.concert.domain.entity.SeatEntity
import dev.concert.domain.entity.UserEntity
import dev.concert.domain.entity.status.SeatStatus
import dev.concert.exception.SeatIsNotAvailableException
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class ConcertServiceImplTest {

    @Mock
    private lateinit var concertRepository: ConcertRepository

    @Mock
    private lateinit var reservationRepository: ReservationRepository

    @Mock
    private lateinit var userRepository: UserRepository

    @InjectMocks
    private lateinit var concertService: ConcertServiceImpl

    @Test
    fun `콘서트 목록을 조회한다`() {
        // given
        val concertList = listOf(
            ConcertEntity(
                concertName = "콘서트1",
                singer = "가��1",
                startDate = "20241201",
                endDate = "20241201",
                reserveStartDate = "20241201",
                reserveEndDate = "20241201",
            ),
            ConcertEntity(
                concertName = "콘서트2",
                singer = "가수2",
                startDate = "20241201",
                endDate = "20241201",
                reserveStartDate = "20241201",
                reserveEndDate = "20241201",
            ),
        )
        given(concertRepository.getConcerts()).willReturn(concertList)

        // when
        val concerts = concertService.getConcerts()

        // then
        assertNotNull(concerts)
        assertEquals(concertList.size, concerts.size)
        assertEquals(concertList[0].concertName, concerts[0].concertName)
        assertEquals(concertList[0].singer, concerts[0].singer)
    }

    @Test
    fun `콘서트 예약 가능한 날짜를 조회한다`() {
        // given
        val concertId = 1L
        val concert = ConcertEntity(
            concertName = "새해 콘서트",
            singer = "에스파",
            startDate = "20241201",
            endDate = "20241201",
            reserveStartDate = "20241201",
            reserveEndDate = "20241201"
        )
        val concertOptions = listOf(
            ConcertOptionEntity(
                concert = concert,
                availableSeats = 50,
                concertTime = "14:00",
                concertVenue = "올림픽공원",
                concertDate = "20241201"
            ),
            ConcertOptionEntity(
                concert = concert,
                availableSeats = 50,
                concertTime = "14:00",
                concertVenue = "올림픽공원",
                concertDate = "20241202"
            )
        )

        given(concertRepository.getAvailableDates(concertId)).willReturn(concertOptions)

        // when
        val availableDates = concertService.getAvailableDates(concertId)

        // then
        assertEquals(2, availableDates.size)
        assertEquals("20241201", availableDates[0].concertDate)
        assertEquals("20241202", availableDates[1].concertDate)
    }

    @Test
    fun `콘서트 예약 가능한 좌석을 조회한다`() {
        // given
        val concertOptionId = 1L
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
        val seats = listOf(
            SeatEntity(
                concertOption = concertOption,
                seatNo = 1,
                price = 100000,
            ),
            SeatEntity(
                concertOption = concertOption,
                seatNo = 2,
                price = 100000,
            )
        )

        given(concertRepository.getAvailableSeats(concertOptionId)).willReturn(seats)

        // when
        val availableSeats = concertService.getAvailableSeats(concertOptionId)

        // then
        assertEquals(2, availableSeats.size)
    }

    @Test
    fun `예약가능한 좌석을 예약하면 좌석이 임시저장 상태가된다`() {
        // given
        val request = ConcertReservationDto(
            userId = 1L,
            seatId = 1L
        )

        val user = stubUserEntity()
        val seat = stubSeatEntity()

        given(userRepository.findById(request.userId)).willReturn(user)
        given(concertRepository.getSeatWithLock(request.seatId)).willReturn(seat)

        // when
        val reservation = concertService.reserveSeat(request)

        // then
        assertThat(SeatStatus.TEMPORARILY_ASSIGNED).isEqualTo(seat.seatStatus)
        assertThat(reservation).isNotNull
    }

    @Test
    fun `임시저장되거나 이미 예약된 좌석을 예약하려하면 SeatIsNotAvailableException 예외가 발생한다`() {
        // given
        val request = ConcertReservationDto(
            userId = 1L,
            seatId = 1L
        )

        val user = stubUserEntity()
        val seat = stubSeatEntity()

        seat.changeStatus(SeatStatus.TEMPORARILY_ASSIGNED)

        given(userRepository.findById(request.userId)).willReturn(user)
        given(concertRepository.getSeatWithLock(request.seatId)).willReturn(seat)

        // when
        assertThatThrownBy { concertService.reserveSeat(request) }
            .isInstanceOf(SeatIsNotAvailableException::class.java)
            .hasMessage("예약 가능한 상태가 아닙니다")
    }

    private fun stubUserEntity() = UserEntity(
        name = "변주환",
    )

    private fun stubSeatEntity(): SeatEntity {
        val seat = SeatEntity(
            seatNo = 1,
            price = 100000,
            concertOption = ConcertOptionEntity(
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
            ),
        )
        return seat
    }
}