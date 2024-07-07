package dev.concert.application.concert.service

import dev.concert.application.concert.dto.ConcertsDto
import dev.concert.domain.ConcertRepository
import dev.concert.domain.entity.ConcertEntity
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
}