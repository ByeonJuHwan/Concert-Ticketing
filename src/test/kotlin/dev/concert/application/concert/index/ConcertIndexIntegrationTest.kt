package dev.concert.application.concert.index

import dev.concert.domain.entity.ConcertEntity
import dev.concert.domain.entity.ConcertOptionEntity
import dev.concert.domain.entity.SeatEntity
import dev.concert.domain.entity.UserEntity
import dev.concert.domain.entity.status.SeatStatus
import dev.concert.domain.repository.ConcertRepository
import dev.concert.domain.repository.SeatRepository
import dev.concert.domain.service.seat.SeatService
import dev.concert.domain.service.user.UserService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.random.Random
import kotlin.system.measureTimeMillis

//@Transactional
@SpringBootTest
class ConcertIndexIntegrationTest {

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var seatRepository: SeatRepository

    @Autowired
    private lateinit var concertRepository: ConcertRepository

    @Autowired
    private lateinit var seatService: SeatService

    //@BeforeEach
    fun setUp() {
        userService.saveUser(UserEntity("user1"))

        val dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")

        for (i in 1..1) { // 콘서트 100,000개 생성
            val randomStartDate = getRandomDateBetween("20240101", "20241231", dateFormatter)
            val randomEndDate = randomStartDate.plusDays(Random.nextLong(0, 6))
            val randomReserveStartDate = getRandomDateBetween("20240101", "20241231", dateFormatter)
            val randomReserveEndDate = randomReserveStartDate.plusDays(Random.nextLong(0, 11))

            if (randomEndDate <= LocalDate.parse("20241231", dateFormatter) &&
                randomReserveEndDate <= LocalDate.parse("20241231", dateFormatter)
            ) {

                val concert = ConcertEntity(
                    concertName = "콘서트$i",
                    singer = "가수${Random.nextInt(1, 101)}",
                    startDate = randomStartDate.format(dateFormatter),
                    endDate = randomEndDate.format(dateFormatter),
                    reserveStartDate = randomReserveStartDate.format(dateFormatter),
                    reserveEndDate = randomReserveEndDate.format(dateFormatter),
                )
                concertRepository.saveConcert(concert)

                repeat(50) { optionIndex ->
                    val concertOption = ConcertOptionEntity(
                        concert = concert,
                        availableSeats = 10000, // 각 옵션에 10,000개 좌석
                        concertDate = randomStartDate.format(dateFormatter),
                        concertTime = if (optionIndex % 2 == 0) "18:00" else "20:00",
                        concertVenue = if (optionIndex % 2 == 0) "올림픽체조경기장" else "국립극장"
                    )
                    concertRepository.saveConcertOption(concertOption)

                    // 각 콘서트 옵션에 대해 10,000개의 좌석 저장
                    repeat(1000) { seatIndex ->
                        val seat = seatRepository.save(
                            SeatEntity(
                                concertOption = concertOption,
                                price = 10000,
                                seatNo = seatIndex + 1, // 좌석 번호 1부터 시작
                            )
                        )

                        if (seatIndex % 3 == 1) {
                            seat.changeSeatStatus(SeatStatus.TEMPORARILY_ASSIGNED)
                            seatRepository.save(seat)
                        } else if (seatIndex % 3 == 2) {
                            seatService.changeSeatStatusReserved(seat)
                        }
                    }
                }
            }
        }
    }

    @Test
    fun `콘서트 목록 조회 인덱스 테스트`() {
        // 쿼리 성능 측정
        val duration = measureTimeMillis {
            val concerts = concertRepository.getConcerts()
            println("콘서트 개수 : ${concerts.size}")
            assertThat(concerts).isNotNull
        }

        // 결과 출력
        println("인덱스 적용 후 DB 조회 시간 : $duration ms")
    }

    @Test
    fun `콘서트 날짜 조회 인덱스 테스트`() {
        // 쿼리 성능 측정
        val duration = measureTimeMillis {
            val concerts = concertRepository.getAvailableDates(501L)
            println("콘서트 날짜 엔티티 개수 : ${concerts.size}")
        }

        // 결과 출력
        println("인덱스 적용 전 DB 조회 시간 : $duration ms")
    }

    @Test
    fun `콘서트 예약 가능 좌석 조회 인덱스 테스트`() {
        val duration = measureTimeMillis {
            val seats = concertRepository.getAvailableSeats(40L)
            println("콘서트 예약 가능 좌석 개수 : ${seats.size}")
        }

        // 결과 출력
        println("인덱스 적용 후 DB 조회 시간 : $duration ms")
    }

    @Test
    fun `콘서트 예약 가능 좌석 조회 concert_option_id 가 먼저오는 인덱스 테스트`() {
        val duration = measureTimeMillis {
            val seats = concertRepository.getAvailableSeats(40L)
            println("콘서트 예약 가능 좌석 개수 : ${seats.size}")
        }

        // 결과 출력
        println("concert_option_id 가 먼저 오는 인덱스 적용 후 DB 조회 시간 : $duration ms")
    }

    @Test
    fun `콘서트 예약 가능 좌석 조회 seat_status 가 먼저오는 인덱스 테스트`() {
        val duration = measureTimeMillis {
            val seats = concertRepository.getAvailableSeats(40L)
            println("콘서트 예약 가능 좌석 개수 : ${seats.size}")
        }

        // 결과 출력
        println("seat_status 가 먼저 오는 인덱스 적용 후 DB 조회 시간 : $duration ms")
    }

    // 주어진 범위 내에서 랜덤 날짜 생성 함수
    private fun getRandomDateBetween(startDate: String, endDate: String, formatter: DateTimeFormatter): LocalDate {
        val start = LocalDate.parse(startDate, formatter)
        val end = LocalDate.parse(endDate, formatter)
        val randomEpochDay = Random.nextLong(start.toEpochDay(), end.toEpochDay())
        return LocalDate.ofEpochDay(randomEpochDay)
    }
}
