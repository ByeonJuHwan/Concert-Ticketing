package org.ktor_lecture.concertservice.adapter.`in`.web.api

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.ktor_lecture.concertservice.IntegrationTestBase
import org.ktor_lecture.concertservice.application.port.out.ConcertReadRepository
import org.ktor_lecture.concertservice.application.port.out.ConcertWriteRepository
import org.ktor_lecture.concertservice.application.port.out.SeatRepository
import org.ktor_lecture.concertservice.application.service.ConcertWriteService
import org.ktor_lecture.concertservice.application.service.command.ReserveSeatCommand
import org.ktor_lecture.concertservice.domain.entity.ConcertUserEntity
import org.ktor_lecture.concertservice.domain.entity.SeatEntity
import org.ktor_lecture.concertservice.fixture.ConcertFixtures
import org.springframework.beans.factory.annotation.Autowired
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

class ConcertRaceConditionTest : IntegrationTestBase() {

    @Autowired
    private lateinit var concertWriteRepository: ConcertWriteRepository

    @Autowired
    private lateinit var concertWriteService: ConcertWriteService

    @Autowired
    private lateinit var concertReadRepository: ConcertReadRepository

    @Autowired
    private lateinit var seatRepository: SeatRepository

    @AfterEach
    fun tearDown() {
        concertWriteRepository.deleteAll()
        concertWriteRepository.deleteAllUser()
    }


    @Test
    fun `콘서트 좌석 예약 동시성 테스트`() {
        // given
        concertWriteRepository.saveAll(createConcertUsers())
        val savedConcertUsers = concertReadRepository.findAllUser()

        val seat = seatRepository.save(createSeat())

        val threadCount = 10
        val executorService = Executors.newFixedThreadPool(32)
        val countDownLatch = CountDownLatch(threadCount)
        val successCount = AtomicInteger(0)
        val failCount = AtomicInteger(0)

        // when
        repeat(threadCount) { index ->
            executorService.submit {
                try {
                    val command = ReserveSeatCommand(
                        seat.id!!,
                        savedConcertUsers[index].id!!
                    )

                    concertWriteService.reserveSeat(command)

                    successCount.incrementAndGet()
                } catch (_: Exception) {
                    failCount.incrementAndGet()
                } finally {
                    countDownLatch.countDown()
                }
            }
        }

        countDownLatch.await()
        executorService.shutdown()

        // then
        assertThat(successCount.get()).isEqualTo(1)
        assertThat(failCount.get()).isEqualTo(9)
    }

    private fun createConcertUsers(count: Int = 10) =
            (1..count).map {
                ConcertUserEntity(
                    name = "test::$it",
                )
            }


    private fun createSeat(): SeatEntity {
        val concert = ConcertFixtures.createConcert(id = null)
        val savedConcert = concertWriteRepository.saveConcert(concert)

        val concertOption = ConcertFixtures.createConcertOption(id = null, concert = savedConcert)
        val savedConcertOption = concertWriteRepository.saveConcertOption(concertOption)

        val seat = ConcertFixtures.createSeat(id = null, concertOption = savedConcertOption)

        return seat
    }
}