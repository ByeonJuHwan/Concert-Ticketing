package dev.concert.application.point.facade

import dev.concert.application.point.UserPointFacade
import dev.concert.application.point.dto.PointRequestDto
import dev.concert.domain.service.user.UserService
import dev.concert.domain.repository.PointRepository
import dev.concert.domain.entity.UserEntity
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest 
class UserPointIntegrationTest {

    @Autowired
    private lateinit var userPointFacade: UserPointFacade

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var pointRepository: PointRepository

    @BeforeEach
    fun beforeEach() {
        userService.saveUser(UserEntity(name = "test"))
    }

    @AfterEach
    fun afterEach() {
        pointRepository.deleteAll()
    }

    @Test 
    fun `1000원의 포인트를 충전한다`() { 
        // given 
        val userId = 1L 
        val amount = 1000L 

        // when 
        val point = userPointFacade.chargePoints(PointRequestDto(userId, amount)) 

        // then 
        assertEquals(1000L, point.point) 
    } 

    @Test 
    fun `현재 포인트를 조회환다`() { 
        val userId = 1L 
        val amount = 1000L 
 
        userPointFacade.chargePoints(PointRequestDto(userId, amount)) 

        // when 
        val currentPoint = userPointFacade.getCurrentPoint(userId) 

        // then 
        assertEquals(1000L, currentPoint.point) 
    }
}
