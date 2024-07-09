package dev.concert.application.concert.facade

import dev.concert.domain.ConcertRepository
import dev.concert.domain.entity.ConcertEntity
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class ConcertFacadeTest {

    @Autowired
    private lateinit var concertFacade: ConcertFacade

    @Autowired
    private lateinit var concertRepository: ConcertRepository

    @Test
    fun `콘서트 목록 조회 통합 테스트`() {
        // given
        concertRepository.saveConcert(
            ConcertEntity(
                concertName = "콘서트1",
                singer = "가수1",
                startDate = "20241201",
                endDate = "20241201",
                reserveStartDate = "20241201",
                reserveEndDate = "20241201",
            )
        )

        // when
        val concerts = concertFacade.getConcerts()

        // then
        assertNotNull(concerts)
        assertThat(concerts.size).isEqualTo(1)
    }
}