package org.ktor_lecture.concertservice.application.service

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.ktor_lecture.concertservice.application.port.out.ConcertReadRepository
import org.ktor_lecture.concertservice.domain.entity.ConcertEntity
import org.ktor_lecture.concertservice.domain.entity.ConcertOptionEntity
import org.ktor_lecture.concertservice.domain.entity.SeatEntity
import org.ktor_lecture.concertservice.domain.status.SeatStatus
import java.time.LocalDate

/**
 * 테스트에서 lateinit var 를 사용하는 이유 :
 * - 코틀린에서는 모든 변수가 초기화 되어야 하므로 초기화를 해줘야하는데 미리 초기화 해버리면 다른 테스트에 간섭된다
 * - null 로 두면 null 체크 및 @BeforeEach 에서 각각 초기화를 해줘야한다 -> 보일러플레이트 코드 증가
 *
 * @ExtendWith(MockKExtension::class)
 * 이 어노테이션이 @BeforeEach 에 해줘야하는 mock 의 초기화를 담당해주기 때문에 별다른 초기화 코드 없이 사용가능하다
 */
@ExtendWith(MockKExtension::class)
class ConcertReadServiceTest {


    @MockK
    private lateinit var concertReadRepository: ConcertReadRepository

    @InjectMockKs
    private lateinit var concertReadService: ConcertReadService

    @Test
    fun `getConcerts_현재 콘서트를 조회한다`() {
        // given
        val concertName = "test"
        val singer = "singer"
        val startDate = LocalDate.now()
        val endDate = LocalDate.now()

        val concert1 = ConcertEntity(
            id = 1L,
            concertName = "test_ConcertName_1",
            singer = "test_singer_1",
            startDate = LocalDate.now(),
            endDate = LocalDate.now(),
            reserveStartDate = LocalDate.now(),
            reserveEndDate = LocalDate.now(),
        )

        val concert2 = ConcertEntity(
            id = 2L,
            concertName = "test_ConcertName_2",
            singer = "test_singer_2",
            startDate = LocalDate.now(),
            endDate = LocalDate.now(),
            reserveStartDate = LocalDate.now(),
            reserveEndDate = LocalDate.now(),
        )

        val concerts = listOf(concert1, concert2)

        every {
            concertReadRepository.getConcerts(concertName, singer, startDate, endDate)
        } returns concerts

        // when
        val result = concertReadService.getConcerts(concertName, singer, startDate, endDate)

        // then
        assertThat(result).hasSize(2)
        assertThat(result[0].concertName).isEqualTo("test_ConcertName_1")
        assertThat(result[1].concertName).isEqualTo("test_ConcertName_2")

        verify(exactly = 1) {
            concertReadRepository.getConcerts(concertName, singer, startDate, endDate)
        }
    }

    @Test
    fun `getAvailableDates_콘서트의 상세 날짜 정보를 조회한다`() {
        val concertId = 1L
        val concert = ConcertEntity(
            id = concertId,
            concertName = "test_ConcertName_1",
            singer = "test_singer_1",
            startDate = LocalDate.now(),
            endDate = LocalDate.now(),
            reserveStartDate = LocalDate.now(),
            reserveEndDate = LocalDate.now(),
        )

        val concertOptionId1 = 1L
        val concertOptionId2 = 2L

        val concertOptionEntity1 = ConcertOptionEntity(
            id = concertOptionId1,
            concert = concert,
            availableSeats = 100,
            concertDate = "20251201",
            concertTime = "12:00",
            concertVenue = "test"
        )

        val concertOptionEntity2 = ConcertOptionEntity(
            id = concertOptionId2,
            concert = concert,
            availableSeats = 100,
            concertDate = "20251201",
            concertTime = "13:00",
            concertVenue = "test"
        )

        val concertOptions = listOf(concertOptionEntity1, concertOptionEntity2)

        every { concertReadRepository.getAvailableDates(concertId)} returns concertOptions

        // when
        val result = concertReadService.getAvailableDates(concertId)

        // then
        assertThat(result.size).isEqualTo(2)
        assertThat(result[0].concertTime).isEqualTo("12:00")
    }

    @Test
    fun `getAvailableSeats_예약 가능한 좌석의 정보를 조회한다`() {
        val concertId = 1L
        val concert = ConcertEntity(
            id = concertId,
            concertName = "test_ConcertName_1",
            singer = "test_singer_1",
            startDate = LocalDate.now(),
            endDate = LocalDate.now(),
            reserveStartDate = LocalDate.now(),
            reserveEndDate = LocalDate.now(),
        )

        val concertOptionId = 1L
        val concertOptionEntity = ConcertOptionEntity(
            id = concertOptionId,
            concert = concert,
            availableSeats = 100,
            concertDate = "20251201",
            concertTime = "12:00",
            concertVenue = "test"
        )

        val seat1 = SeatEntity(
            id = 1L,
            concertOption = concertOptionEntity,
            seatStatus = SeatStatus.RESERVED,
            price = 1000,
            seatNo = 1
        )

        val seat2 = SeatEntity(
            id = 2L,
            concertOption = concertOptionEntity,
            seatStatus = SeatStatus.AVAILABLE,
            price = 1000,
            seatNo = 2
        )

        val seats = listOf(seat1, seat2)

        every { concertReadRepository.getAvailableSeats(concertId) } returns seats

        // when
        val result = concertReadService.getAvailableSeats(concertOptionId)

        // then
        assertThat(result.size).isEqualTo(2)
        assertThat(result[0].status).isEqualTo(SeatStatus.RESERVED)
        assertThat(result[1].status).isEqualTo(SeatStatus.AVAILABLE)
    }
}