package org.ktor_lecture.userservice.application.service

import org.ktor_lecture.userservice.adapter.`in`.web.response.CurrentPointResponse
import org.ktor_lecture.userservice.application.port.`in`.point.ChargePointUseCase
import org.ktor_lecture.userservice.application.port.`in`.point.SearchCurrentPointsUseCase
import org.ktor_lecture.userservice.application.port.out.PointRepository
import org.ktor_lecture.userservice.application.port.out.UserReadRepository
import org.ktor_lecture.userservice.application.service.command.ChargePointCommand
import org.ktor_lecture.userservice.domain.entity.PointEntity
import org.ktor_lecture.userservice.domain.exception.ConcertException
import org.ktor_lecture.userservice.domain.exception.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PointService (
    private val userReadRepository: UserReadRepository,
    private val pointRepository: PointRepository,
): ChargePointUseCase, SearchCurrentPointsUseCase {

    /**
     * 포인트를 충전합니다
     *
     * 1. 유저조회
     * 2. 해당 유저에 포인트 충전
     */
    @Transactional
    override fun chargePoints(command: ChargePointCommand): CurrentPointResponse {
        val user = userReadRepository.findById(command.userId).orElseThrow { throw ConcertException(ErrorCode.USER_NOT_FOUND) }

        // 비관적 락
        val point = pointRepository.getCurrentPoint(user) ?: PointEntity(user = user, point = 0L)

        point.charge(command.amount)

        pointRepository.save(point)

        return CurrentPointResponse(
            currentPoints = point.point
        )
    }

    @Transactional
    override fun getCurrentPoint(userId: Long): CurrentPointResponse {
        val user = userReadRepository.findById(userId).orElseThrow { throw ConcertException(ErrorCode.USER_NOT_FOUND) }

        val point = pointRepository.getCurrentPoint(user) ?: PointEntity(user = user, point = 0L)

        pointRepository.save(point)

        return CurrentPointResponse(
            currentPoints = point.point
        )
    }
}