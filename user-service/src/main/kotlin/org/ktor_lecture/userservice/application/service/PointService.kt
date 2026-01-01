package org.ktor_lecture.userservice.application.service

import org.ktor_lecture.userservice.adapter.`in`.web.response.CurrentPointResponse
import org.ktor_lecture.userservice.adapter.`in`.web.response.PointUseResponse
import org.ktor_lecture.userservice.application.port.`in`.point.ChargePointUseCase
import org.ktor_lecture.userservice.application.port.`in`.point.PointCancelUseCase
import org.ktor_lecture.userservice.application.port.`in`.point.PointUseUseCase
import org.ktor_lecture.userservice.application.port.`in`.point.SearchCurrentPointsUseCase
import org.ktor_lecture.userservice.application.port.out.IdempotencyRepository
import org.ktor_lecture.userservice.application.port.out.PointHistoryRepository
import org.ktor_lecture.userservice.application.port.out.PointRepository
import org.ktor_lecture.userservice.application.port.out.UserReadRepository
import org.ktor_lecture.userservice.application.service.command.ChargePointCommand
import org.ktor_lecture.userservice.application.service.command.PointCancelCommand
import org.ktor_lecture.userservice.application.service.command.PointUseCommand
import org.ktor_lecture.userservice.domain.entity.IdempotencyEntity
import org.ktor_lecture.userservice.domain.entity.PointEntity
import org.ktor_lecture.userservice.domain.entity.PointHistoryEntity
import org.ktor_lecture.userservice.domain.entity.PointTransactionType
import org.ktor_lecture.userservice.domain.exception.ConcertException
import org.ktor_lecture.userservice.domain.exception.ErrorCode
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PointService (
    private val userReadRepository: UserReadRepository,
    private val pointRepository: PointRepository,
    private val pointHistoryRepository: PointHistoryRepository,
    private val idempotencyRepository: IdempotencyRepository,
): ChargePointUseCase, SearchCurrentPointsUseCase, PointUseUseCase, PointCancelUseCase {

    private val log = LoggerFactory.getLogger(PointService::class.java)

    /**
     * 포인트를 충전합니다
     *
     * 1. 유저조회
     * 2. 해당 유저에 포인트 충전
     * 3. 포인트 충전 히스토리 저장
     */
    @Transactional
    override fun chargePoints(command: ChargePointCommand): CurrentPointResponse {
        val user = userReadRepository.findById(command.userId).orElseThrow { throw ConcertException(ErrorCode.USER_NOT_FOUND) }

        // 비관적 락
        val point = pointRepository.getCurrentPoint(user) ?: PointEntity(user = user, point = 0L)

        point.charge(command.amount)

        pointRepository.save(point)

        val pointHistory = PointHistoryEntity(
            user = user,
            amount = command.amount,
            type = PointTransactionType.CHARGE,
        )

        pointHistoryRepository.save(pointHistory)

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

    /**
     * 유저 포인트 사용
     * 1. 유저 검색
     * 2. 유저 - 포인트 검색
     * 3. 유저 포인트 차감
     * 4. 포인트 사용 히스토리 저장
     */
    @Transactional
    override fun use(command: PointUseCommand): PointUseResponse {
        val user = userReadRepository.findById(command.userId.toLong()).orElseThrow { throw ConcertException(ErrorCode.USER_NOT_FOUND) }

        val point = pointRepository.getCurrentPoint(user) ?: PointEntity(user = user, point = 0L)

        point.use(command.amount)

        val pointHistory = pointHistoryRepository.save(
            PointHistoryEntity(
                user = user,
                amount = command.amount,
                type = PointTransactionType.USE,
            )
        )

        return PointUseResponse(
            userId = user.id!!,
            pointHistoryId = pointHistory.id!!,
            remainingPoints = point.point
        )
    }

    /**
     * 유저 포인트 감소
     * 1. 멱등성 체크
     * --- 처음 들어오는 요청의 경우 아래 과정 진행
     * 2. 유저 조회
     * 3. 포인트 히스토리 조회
     * 4. 포인트 취소(롤백)
     * 5. 포인트 히스토리 취소 상태 변경
     * 6. 멱등성 저장
     */
    @Transactional
    override fun cancel(command: PointCancelCommand) {
        val sagaId = command.sagaId

        val idempotency = idempotencyRepository.findBySagaId(sagaId)
        if (idempotency != null) {
            log.info("이미 취소된 요청입니다 sagaId: $sagaId")
            return
        }

        val user = userReadRepository.findById(command.userId).orElseThrow { throw ConcertException(ErrorCode.USER_NOT_FOUND) }
        val pointHistory = pointHistoryRepository.findById(command.pointHistoryId).orElseThrow { throw ConcertException(ErrorCode.POINT_HISTORY_NOT_FOUND) }


        val point = pointRepository.getCurrentPoint(user) ?: PointEntity(user = user, point = 0L)

        point.cancel(command.amount)
        pointHistory.cancel()

        idempotencyRepository.save(
            IdempotencyEntity(
                sagaId = sagaId,
            )
        )
    }
}