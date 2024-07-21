package dev.concert.application.point.service

import dev.concert.domain.repository.PointRepository
import dev.concert.domain.entity.PointEntity
import dev.concert.domain.entity.UserEntity
import dev.concert.domain.service.point.PointServiceImpl
import dev.concert.domain.exception.NotEnoughPointException
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class PointServiceImplTest {

    @Mock
    private lateinit var pointRepository: PointRepository

    @InjectMocks
    private lateinit var pointService: PointServiceImpl

    @Test
    fun `처음충전한 회원은 1000원을 충전하면 1000원 충전이 성공한다`() {
        // given
        val amount = 1000L
        val user = UserEntity(name = "test")
        val point = PointEntity(user,0)

        given(pointRepository.findByUser(user)).willReturn(point)
        given(pointRepository.save(point)).willReturn(point)

        // when
        val chargePoints = pointService.chargePoints(user , amount)
        // then
        assertThat(chargePoints.point).isEqualTo(1000L)
    }

    @Test
    fun `기존의 회원이 1000원의 포인트를 가지고 있으면 1000원 충전히 2000 포인트가 되어야한다`() {
        // given
        val amount = 1000L
        val user = UserEntity(name = "test")
        val point = PointEntity(user, 1000L)

        given(pointRepository.findByUser(user)).willReturn(point)
        given(pointRepository.save(point)).willReturn(point)

        // when
        val chargePoints = pointService.chargePoints(user , amount)

        // then
        assertThat(chargePoints.point).isEqualTo(2000L)
    }

    @Test
    fun `0이하의 포인트를 넣으면 IllegalArgumentException 이 발생한다`() {
        // given
        val amount = -1000L
        val user = UserEntity(name = "test")
        val point = PointEntity(user,0)

        // when
        `when`(pointRepository.findByUser(user)).thenReturn(point)

        // then
        assertThatThrownBy { pointService.chargePoints(user , amount) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("0보다 작은 값을 충전할 수 없습니다")
    }

    @Test
    fun `현재 포인트를 조회합니다`() {
        // given
        val user = UserEntity(name = "test")
        val point = PointEntity(user, 1000L)

        given(pointRepository.findByUser(user)).willReturn(point)

        // when
        val currentPoint = pointService.getCurrentPoint(user)

        // then
        assertThat(currentPoint.point).isEqualTo(1000L)
    }

    @Test
    fun `현재 포인트가 결제 포인트 보다 작으면 NotEnoughPointException 을 터트린다`() {
        // given
        val user = UserEntity(name = "test")
        val point = PointEntity(user, 0)

        given(pointRepository.findByUser(user)).willReturn(point)

        // when & then
        assertThatThrownBy {
            pointService.checkPoint(user, 1000L)
        }.isInstanceOf(NotEnoughPointException::class.java)
    }

    @Test
    fun `현재 포인트가 결제 포인트 보다 같거나 많으면 현재 포인트를 응답한다`() {
        // given
        val user = UserEntity(name = "test")
        val point = PointEntity(user, 1000)
        given(pointRepository.findByUser(user)).willReturn(point)

        // when
        val currentPoint = pointService.checkPoint(user, 1000L)

        // then
        assertThat(currentPoint.point).isEqualTo(1000L)
    }

    @Test
    fun `현재 포인트에서 결제 포인트를 차감한다`() {
        // given
        val user = UserEntity(name = "test")
        val point = PointEntity(user, 1000)

        // when
        pointService.deductPoints(point, 1000L)

        // then
        assertThat(point.point).isEqualTo(0L)
    }
}