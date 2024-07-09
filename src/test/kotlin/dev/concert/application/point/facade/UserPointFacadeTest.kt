package dev.concert.application.point.facade

import dev.concert.application.point.dto.PointRequestDto
import dev.concert.application.point.service.PointHistoryService
import dev.concert.application.user.UserService
import dev.concert.domain.entity.UserEntity
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class UserPointFacadeTest {

    @Autowired
    private lateinit var userPointFacade: UserPointFacade

    @Autowired
    private lateinit var userService: UserService

    @Test
    fun `포인트 충전 통합 테스트`() {
        // given
        val userId = 1L
        val amount = 1000L
        val request = PointRequestDto(userId, amount)
        val user = UserEntity("변주환")

        userService.saveUser(user)

        // when
        val result = userPointFacade.chargePoints(request)

        // then
        assertEquals(result.point, amount)
    }

    @Test
    fun `포인트 조회 통합 테스트`() {
        // given
        val userId = 1L
        val amount = 1000L
        val request = PointRequestDto(userId, amount)
        val user = UserEntity("변주환")

        userService.saveUser(user)
        userPointFacade.chargePoints(request)

        // when
        val result = userPointFacade.getCurrentPoint(userId)

        // then
        assertEquals(result.point, amount)
    }
}