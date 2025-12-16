package org.ktor_lecture.userservice.adapter.`in`.web.api

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.ktor_lecture.userservice.IntegrationTestBase
import org.ktor_lecture.userservice.adapter.`in`.web.request.PointChargeRequest
import org.ktor_lecture.userservice.application.port.`in`.point.ChargePointUseCase
import org.ktor_lecture.userservice.application.port.out.PointRepository
import org.ktor_lecture.userservice.application.port.out.UserReadRepository
import org.ktor_lecture.userservice.application.port.out.UserWriteRepository
import org.ktor_lecture.userservice.domain.entity.PointEntity
import org.ktor_lecture.userservice.domain.entity.UserEntity
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.get
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

class PointControllerTest() : IntegrationTestBase() {

    @Autowired
    private lateinit var userWriteRepository: UserWriteRepository

    @Autowired
    private lateinit var userReadRepository: UserReadRepository

    @Autowired
    private lateinit var pointRepository: PointRepository

    @Autowired
    private lateinit var chargePointUseCase: ChargePointUseCase



    @BeforeEach
    fun setUp() {
        val savedUser = userWriteRepository.save(UserEntity(name = "test"))
        pointRepository.save(PointEntity(user = savedUser, point = 0L))
    }

    @AfterEach
    fun tearDown() {
        userWriteRepository.deleteAll()
        pointRepository.deleteAll()
    }

    @Test
    fun `pointCharge_포인트를 충전한다_동시성테스트`() {
        val chargePoint = 1000L
        val threadCount = 10
        val executorService = Executors.newFixedThreadPool(32)
        val latch = CountDownLatch(threadCount)

        val user = userReadRepository.findById(1L).orElseThrow()

        val request = PointChargeRequest(user.id!!, chargePoint)

        repeat(threadCount) {
            executorService.submit {
                try {
                    chargePointUseCase.chargePoints(request.toCommand())
                } finally {
                    latch.countDown()
                }
            }
        }

        latch.await()
        executorService.shutdown()

        val point = pointRepository.findById(user.id!!).orElseThrow()
        assertThat(point.point).isEqualTo(chargePoint * threadCount)
    }

    @Test
    fun `getCurrentPoint_현재 포인트를 조회한다`() {
        // when
        mockMvc.get("/api/v1/points/current/1")
            .andExpect {
                status { isOk() }
                status { isOk() }
                jsonPath("$.status") { value(200) }
                jsonPath("$.message") { value("API 응답 성공") }
                jsonPath("$.data.currentPoints") { exists() }
                jsonPath("$.data.currentPoints") { isNumber() }
                jsonPath("$.data.currentPoints") { value(0L)}
            }
    }

}