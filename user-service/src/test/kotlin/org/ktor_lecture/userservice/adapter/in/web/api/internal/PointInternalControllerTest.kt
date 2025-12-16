package org.ktor_lecture.userservice.adapter.`in`.web.api.internal

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.ktor_lecture.userservice.IntegrationTestBase
import org.ktor_lecture.userservice.adapter.`in`.web.api.UserController
import org.ktor_lecture.userservice.adapter.`in`.web.request.PointCancelRequest
import org.ktor_lecture.userservice.application.port.`in`.point.PointCancelUseCase
import org.ktor_lecture.userservice.application.port.`in`.point.PointUseUseCase
import org.ktor_lecture.userservice.application.port.out.PointRepository
import org.ktor_lecture.userservice.application.port.out.UserReadRepository
import org.ktor_lecture.userservice.application.port.out.UserWriteRepository
import org.ktor_lecture.userservice.application.service.command.PointCancelCommand
import org.ktor_lecture.userservice.application.service.command.PointUseCommand
import org.ktor_lecture.userservice.common.JsonUtil
import org.ktor_lecture.userservice.domain.entity.PointEntity
import org.ktor_lecture.userservice.domain.entity.UserEntity
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

class PointInternalControllerTest() : IntegrationTestBase() {

    @Autowired
    private lateinit var userWriteRepository: UserWriteRepository

    @Autowired
    private lateinit var pointRepository: PointRepository

    @Autowired
    private lateinit var userReadRepository: UserReadRepository

    @Autowired
    private lateinit var pointUseUseCase: PointUseUseCase

    @Autowired
    private lateinit var pointCancelUseCase: PointCancelUseCase

    private var testUserId = 0L

    @BeforeEach
    fun setUp() {
        val savedUser = userWriteRepository.save(UserEntity(name = "test"))
        pointRepository.save(PointEntity(user = savedUser, point = 1000L))
        testUserId = savedUser.id!!
    }

    @AfterEach
    fun tearDown() {
        userWriteRepository.deleteAll()
        pointRepository.deleteAll()
    }

    @Test
    fun `getCurrentPoint_유저의 현재 포인트를 조회한다`() {
        // when && then
        mockMvc.get("/points/current/$testUserId")
            .andExpect {
                status { isOk() }
                jsonPath("$.currentPoints") { value(1000L) }
            }
    }

    @Test
    fun `use_포인트를 사용한다_동시성 테스트`() {
        // given
        val usePoint = 100L
        val threadCount = 10
        val executorService = Executors.newFixedThreadPool(32)
        val latch = CountDownLatch(threadCount)

        val user = userReadRepository.findById(testUserId).orElseThrow()

        val command = PointUseCommand (
            user.id!!,
            usePoint
        )

        // when
        repeat(threadCount) {
            executorService.submit {
                try {
                    pointUseUseCase.use(command)
                } finally {
                    latch.countDown()
                }
            }
        }

        latch.await()
        executorService.shutdown()

        // then
        val point = pointRepository.findById(user.id!!).orElseThrow()
        assertThat(point.point).isEqualTo(0L)
    }

    @Test
    fun `cancel_포인트 사용을 취소한다`() {
        // given
        val usePoint = 100L
        val user = userReadRepository.findById(testUserId).orElseThrow()
        val useCommand = PointUseCommand (
            user.id!!,
            usePoint
        )

        pointUseUseCase.use(useCommand)

        val cancelCommand = PointCancelCommand (
            user.id!!,
            1L,
            usePoint
        )

        // when
        pointCancelUseCase.cancel(cancelCommand)

        // then
        val point = pointRepository.findById(user.id!!).orElseThrow()
        assertThat(point.point).isEqualTo(1000L)
    }

}