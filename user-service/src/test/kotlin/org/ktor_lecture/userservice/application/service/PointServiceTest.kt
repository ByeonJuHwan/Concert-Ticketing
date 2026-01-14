package org.ktor_lecture.userservice.application.service

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.ktor_lecture.userservice.application.port.out.PointHistoryRepository
import org.ktor_lecture.userservice.application.port.out.PointRepository
import org.ktor_lecture.userservice.application.port.out.UserReadRepository
import org.ktor_lecture.userservice.application.service.command.ChargePointCommand
import org.ktor_lecture.userservice.application.service.command.PointCancelCommand
import org.ktor_lecture.userservice.application.service.command.PointUseCommand
import org.ktor_lecture.userservice.domain.entity.PointEntity
import org.ktor_lecture.userservice.domain.entity.PointHistoryEntity
import org.ktor_lecture.userservice.domain.entity.PointTransactionType
import org.ktor_lecture.userservice.domain.entity.UserEntity
import org.ktor_lecture.userservice.domain.exception.ConcertException
import org.ktor_lecture.userservice.domain.exception.ErrorCode
import java.util.*

@ExtendWith(MockKExtension::class)
class PointServiceTest {

    @MockK
    private lateinit var userReadRepository: UserReadRepository

    @MockK
    private lateinit var pointRepository: PointRepository

    @MockK
    private lateinit var pointHistoryRepository: PointHistoryRepository

    @InjectMockKs
    private lateinit var pointService: PointService

    @Test
    fun `chargePoints_포인트 충전 성공 테스트`() {
        val userId = 1L
        val initialPoint = 0L
        val chargeAmount = 1000L

        val user = UserEntity(id = userId, name = "test")
        val point = PointEntity(user = user, point = initialPoint)
        val command = ChargePointCommand(userId = userId, amount = chargeAmount)

        every { userReadRepository.findById(command.userId) } returns Optional.of(user)
        every { pointRepository.getCurrentPoint(user) } returns point
        every { pointRepository.save(any()) } just runs
        every { pointHistoryRepository.save(any()) } returnsArgument 0

        // when
        val result = pointService.chargePoints(command)

        // then
        assertThat(result.currentPoints).isEqualTo(initialPoint + chargeAmount)
        assertThat(point.point).isEqualTo(initialPoint + chargeAmount)
        verify(exactly = 1) {userReadRepository.findById(userId)}
        verify(exactly = 1) {pointRepository.getCurrentPoint(user)}
        verify(exactly = 1) {pointRepository.save(point)}
    }

    @Test
    fun `chargePoints_0보다 작은 값 충전시 예외 발생`() {
        val userId = 1L
        val initialPoint = 0L
        val chargeAmount = -100L

        val user = UserEntity(id = userId, name = "test")
        val point = PointEntity(user = user, point = initialPoint)
        val command = ChargePointCommand(userId = userId, amount = chargeAmount)

        every { userReadRepository.findById(command.userId) } returns Optional.of(user)
        every { pointRepository.getCurrentPoint(user) } returns point

        // when && then
        assertThrows <IllegalArgumentException> { pointService.chargePoints(command) }
            .also { assertThat(it.message).isEqualTo("0보다 작은 값을 충전할 수 없습니다") }
        verify(exactly = 0) { pointRepository.save(any()) }
        verify(exactly = 0) { pointHistoryRepository.save(any()) }
    }

    @Test
    fun `chargePoints_사용자를 찾을 수 없으면 예외 발생`() {
        val userId = 999L

        val command = ChargePointCommand(userId = userId, amount = 1000L)

        every { userReadRepository.findById(userId) } returns Optional.empty()

        // when && then
        assertThrows<ConcertException> {
            pointService.chargePoints(command)
        }.also {
            assertThat(it.errorCode).isEqualTo(ErrorCode.USER_NOT_FOUND)
        }

        verify(exactly = 0) { pointRepository.getCurrentPoint(any()) }
        verify(exactly = 0) { pointRepository.save(any()) }
        verify(exactly = 0) { pointHistoryRepository.save(any()) }
    }

    @Test
    fun `getCurrentPoint_현재 포인트 정보 반환`() {
        val userId = 1L

        val user = UserEntity(id = userId, name = "test")
        val point = PointEntity(user = user, point = 1000L)

        every { userReadRepository.findById(userId) } returns Optional.of(user)
        every { pointRepository.getCurrentPoint(user) } returns point
        every { pointRepository.save(any())} just runs

        // when
        val result = pointService.getCurrentPoint(userId)

        // then
        assertThat(result.currentPoints).isEqualTo(point.point)
    }

    @Test
    fun `use_포인트사용 테스트`() {
        val userId = 1L
        val pointHistoryId = 1L
        val initialPoints = 1000L
        val usePoints = 1000L

        val user = UserEntity(id = userId, name = "test")
        val point = PointEntity(user = user, point = initialPoints)
        val pointHistory = PointHistoryEntity(pointHistoryId, user, usePoints, PointTransactionType.USE)
        val command = PointUseCommand(userId = userId, amount = usePoints)

        every { userReadRepository.findById(userId) } returns Optional.of(user)
        every { pointRepository.getCurrentPoint(user) } returns point
        every { pointHistoryRepository.save(any()) } returns pointHistory

        // when
        val result = pointService.use(command)

        // then
        assertThat(result.remainingPoints).isEqualTo(initialPoints - usePoints)
    }

    @Test
    fun `use_현재 자기가 가지고 있는 포인트보다 많은 포인트 사용시 예외 발생`() {
        val userId = 1L
        val initialPoints = 1000L
        val usePoints = 1001L

        val user = UserEntity(id = userId, name = "test")
        val point = PointEntity(user = user, point = initialPoints)
        val command = PointUseCommand(userId = userId, amount = usePoints)

        every { userReadRepository.findById(userId) } returns Optional.of(user)
        every { pointRepository.getCurrentPoint(user) } returns point

        // when && then
        assertThrows <ConcertException> {
            pointService.use(command)
        }.also {
            assertThat(it.errorCode).isEqualTo(ErrorCode.POINT_NOT_ENOUGH)
        }

        verify(exactly = 0) { pointHistoryRepository.save(any()) }
    }

    @Test
    fun `cancel_포인트 취소 성공`() {
        val userId = 1L
        val pointHistoryId = 1L
        val initialPoints = 0L
        val cancelPoints = 1000L
        val usePoints = 1000L

        val user = UserEntity(id = userId, name = "test")
        val point = PointEntity(user = user, point = initialPoints)
        val pointHistory = PointHistoryEntity(pointHistoryId, user, usePoints, PointTransactionType.USE)
        val command = PointCancelCommand(user.id.toString(), userId, pointHistoryId, cancelPoints)

        every { userReadRepository.findById(userId) } returns Optional.of(user)
        every { pointHistoryRepository.findById(pointHistoryId) } returns Optional.of(pointHistory)
        every { pointRepository.getCurrentPoint(user)} returns point

        // when
        pointService.cancel(command)

        // then
        assertThat(point.point).isEqualTo(cancelPoints)
        assertThat(pointHistory.type).isEqualTo(PointTransactionType.CANCEL)
    }

    @Test
    fun `cancel_포인트 이력정보가 없으면 예외 발생`() {
        val userId = 1L
        val pointHistoryId = 999L
        val cancelPoints = 1000L

        val user = UserEntity(id = userId, name = "test")
        val command = PointCancelCommand(userId.toString(), userId, pointHistoryId, cancelPoints)

        every { userReadRepository.findById(userId) } returns Optional.of(user)
        every { pointHistoryRepository.findById(pointHistoryId) } returns Optional.empty()

        // when && then
        assertThrows <ConcertException> { pointService.cancel(command) }
            .also { assertThat(it.errorCode).isEqualTo(ErrorCode.POINT_HISTORY_NOT_FOUND) }

        verify(exactly = 0) { pointRepository.getCurrentPoint(user)}
    }
}