package dev.concert.application.point.service

import dev.concert.application.point.dto.PointRequestDto
import dev.concert.domain.PointRepository
import dev.concert.domain.UserRepository
import dev.concert.domain.entity.PointEntity
import dev.concert.domain.entity.UserEntity
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class PointServiceImplTest {

    @Mock
    private lateinit var userRepository: UserRepository

    @Mock
    private lateinit var pointRepository: PointRepository

    @InjectMocks
    private lateinit var pointService: PointServiceImpl

    @Test
    fun `처음충전한 회원은 1000원을 충전하면 1000원 충전이 성공한다`() {
        // given
        val userId = 1L
        val amount = 1000L
        val user = UserEntity(name = "test")
        val point = PointEntity(user,0)

        // when
        `when`(userRepository.findById(userId)).thenReturn(user)
        `when`(pointRepository.findByUser(user)).thenReturn(point)
        val chargePoints = pointService.chargePoints(PointRequestDto(userId, amount))

        // then
        assertThat(chargePoints.point).isEqualTo(1000L)
    }

    @Test
    fun `기존의 회원이 1000원의 포인트를 가지고 있으면 1000원 충전히 2000 포인트가 되어야한다`() {
        // given
        val userId = 1L
        val amount = 1000L
        val user = UserEntity(name = "test")
        val point = PointEntity(user, 1000L)

        // when
        `when`(userRepository.findById(userId)).thenReturn(user)
        `when`(pointRepository.findByUser(user)).thenReturn(point)
        val chargePoints = pointService.chargePoints(PointRequestDto(userId, amount))

        // then
        assertThat(chargePoints.point).isEqualTo(2000L)
    }

    @Test
    fun `0이하의 포인트를 넣으면 IllegalArgumentException 이 발생한다`() {
        // given
        val userId = 1L
        val amount = -1000L
        val user = UserEntity(name = "test")
        val point = PointEntity(user,0)

        // when
        `when`(userRepository.findById(userId)).thenReturn(user)
        `when`(pointRepository.findByUser(user)).thenReturn(point)

        // then
        assertThatThrownBy { pointService.chargePoints(PointRequestDto(userId, amount)) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("0보다 작은 값을 충전할 수 없습니다")
    }

    @Test
    fun `현재 포인트를 조회합니다`() {
        // given
        val userId = 1L
        val user = UserEntity(name = "test")
        val point = PointEntity(user, 1000L)

        // when
        `when`(userRepository.findById(userId)).thenReturn(user)
        `when`(pointRepository.findByUser(user)).thenReturn(point)
        val currentPoint = pointService.getCurrentPoint(userId)

        // then
        assertThat(currentPoint.point).isEqualTo(1000L)
    }
}