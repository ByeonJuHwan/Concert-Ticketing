package dev.concert.application.concert.service

import dev.concert.domain.repository.ConcertRepository
import dev.concert.domain.entity.ConcertEntity
import dev.concert.domain.entity.ConcertOptionEntity
import dev.concert.domain.entity.SeatEntity
import dev.concert.domain.service.concert.ConcertServiceImpl
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
}