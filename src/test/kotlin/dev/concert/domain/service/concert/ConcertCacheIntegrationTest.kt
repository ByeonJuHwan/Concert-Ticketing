package dev.concert.domain.service.concert

import dev.concert.domain.entity.ConcertEntity
import dev.concert.domain.entity.ConcertOptionEntity
import dev.concert.domain.entity.UserEntity
import dev.concert.domain.repository.ConcertRepository
import dev.concert.domain.service.user.UserService
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cache.annotation.EnableCaching
import org.springframework.transaction.annotation.Transactional

@Transactional
@EnableCaching
@SpringBootTest
class ConcertCacheIntegrationTest {

    @Autowired
    private lateinit var concertService: ConcertService

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var concertRepository: ConcertRepository

    @BeforeEach
    fun setUp() {
        userService.saveUser(UserEntity(name = "변주환"))
        userService.saveUser(UserEntity(name = "kim"))

        for (i in 1..100) {
            val concert = ConcertEntity(
                concertName = "콘서트$i",
                singer = "가수$i",
                startDate = "20241201",
                endDate = "20241201",
                reserveStartDate = "20241201",
                reserveEndDate = "20241201",
            )
            concertRepository.saveConcert(concert)
        }

        val concert = concertRepository.findById(1L) ?: throw RuntimeException("콘서트를 찾을 수 없습니다")

        for (i in 1..100) {
            concertRepository.saveConcertOption(
                ConcertOptionEntity(
                    concert = concert,
                    concertDate = "20241201",
                    concertTime = "12:00",
                    concertVenue = "올림픽체조경기장",
                    availableSeats = 100,
                )
            )
        }
    }

    @Test
    fun `캐싱을 활용한 콘서트 목록 조회 테스트`() {
        // 첫 번째 호출: DB 에서 데이터를 조회해 가져옵니다
        val startTime1 = System.currentTimeMillis()
        val concerts1 = concertService.getConcerts()
        val endTime1 = System.currentTimeMillis()
        val duration1 = endTime1 - startTime1

        assertEquals(100, concerts1.size)
        println("DB 에서 콘서트 목록 조회 소요 시간 : $duration1 ms")

        // 두 번째 호출: 캐시가 되어 있으므로 캐시에서 데이터를 가져옵니다.
        val startTime2 = System.currentTimeMillis()
        val concerts2 = concertService.getConcerts()
        val endTime2 = System.currentTimeMillis()
        val duration2 = endTime2 - startTime2

        assertEquals(100, concerts2.size, "The number of concerts should be 50")
        println("로컬 캐싱에서 콘서트 목록 조회 소요 시간 : $duration2 ms")

        //assertSame(concerts1, concerts2)
        assertTrue(duration2 < duration1)
    }

    @Test
    fun `캐싱을 활용한 콘서트 예약가능 날짜 목록 조회 테스트`() {
        // 첫 번째 호출: DB 에서 데이터를 조회해 가져옵니다
        val startTime1 = System.currentTimeMillis()
        val concertOptions1 = concertService.getAvailableDates(1L)
        val endTime1 = System.currentTimeMillis()
        val duration1 = endTime1 - startTime1

        assertEquals(100, concertOptions1.size)
        println("DB 에서 콘서트 예약가능 날짜 목록 조회 소요 시간 : $duration1 ms")

        // 두 번째 호출: 캐시가 되어 있으므로 캐시에서 데이터를 가져옵니다.
        val startTime2 = System.currentTimeMillis()
        val concertOptions2 = concertService.getAvailableDates(1L)
        val endTime2 = System.currentTimeMillis()
        val duration2 = endTime2 - startTime2

        assertEquals(100, concertOptions2.size)
        println("로컬 캐싱에서 콘서트 예약가능 날짜 목록 조회 소요 시간 : $duration2 ms")

        assertTrue(duration2 < duration1)
    }

    @Test
    fun `캐싱을 활용한 콘서트 목록 조회 후 캐시 삭제 테스트`() {
        // 첫 번째 호출: DB 에서 데이터를 조회해 가져옵니다
        val startTime1 = System.currentTimeMillis()
        val concerts1 = concertService.getConcerts()
        val endTime1 = System.currentTimeMillis()
        val duration1 = endTime1 - startTime1

        assertEquals(100, concerts1.size)
        println("DB 에서 콘서트 목록 조회 소요 시간 : $duration1 ms")

        // 콘서트 데이터 1개 추가
        insertNewConcert()

        // 두 번째 호출: 캐시가 되어 있으므로 캐시에서 데이터를 가져옵니다.
        val startTime2 = System.currentTimeMillis()
        val concerts2 = concertService.getConcerts()
        val endTime2 = System.currentTimeMillis()
        val duration2 = endTime2 - startTime2

        assertEquals(100, concerts2.size)
        println("Redis 캐싱에서 콘서트 목록 조회 소요 시간 : $duration2 ms")

        assertTrue(duration2 < duration1)

        // 3초 대기: 캐시가 만료되도록 대기합니다.
        Thread.sleep(4000)

        // 세 번째 호출: 캐시가 만료되어 다시 DB 에서 데이터를 조회해 가져옵니다
        val startTime3 = System.currentTimeMillis()
        val concerts3 = concertService.getConcerts()
        val endTime3 = System.currentTimeMillis()
        val duration3 = endTime3 - startTime3

        assertEquals(101, concerts3.size)
        println("캐시 만료 후 DB 에서 콘서트 목록 조회 소요 시간 : $duration3 ms")

        // 네 번째 호출: 캐시가 되어 있으므로 캐시에서 데이터를 가져옵니다.
        val startTime4 = System.currentTimeMillis()
        val concerts4 = concertService.getConcerts()
        val endTime4 = System.currentTimeMillis()
        val duration4 = endTime4 - startTime4

        assertEquals(101, concerts4.size)
        println("Redis 캐싱에서 콘서트 목록 조회 소요 시간 : $duration4 ms")

        assertTrue(duration4 < duration3)
    }

    private fun insertNewConcert() {
        concertRepository.saveConcert(
            ConcertEntity(
                concertName = "새로운 콘서트 데이터 추가",
                singer = "새로운 콘서트 가수 추가",
                startDate = "20241201",
                endDate = "20241201",
                reserveStartDate = "20241201",
                reserveEndDate = "20241201",
            )
        )
    }
}