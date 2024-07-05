package dev.concert.application.point.service

import dev.concert.application.point.dto.PointRequestDto
import dev.concert.application.point.dto.PointResponseDto
import dev.concert.domain.PointHistoryRepository
import dev.concert.domain.PointRepository
import dev.concert.domain.UserRepository
import dev.concert.domain.entity.PointEntity
import dev.concert.domain.entity.UserEntity
import dev.concert.exception.UserNotFountException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PointServiceImpl (
    private val userRepository: UserRepository,
    private val pointRepository: PointRepository,
    private val pointHistoryRepository: PointHistoryRepository,
) : PointService {

    @Transactional
    override fun chargePoints(request: PointRequestDto): PointResponseDto {
        val user = getUser(request.userId)
        val point = getPoint(user)

        // TODO 나중에 결재와 같이 사용시 동시성 이슈가 발생할 수 있음
        chargePoints(point, request)
        savePointHistory(user, request.amount)

        return PointResponseDto.from(point)
    }

    @Transactional(readOnly = true)
    override fun getCurrentPoint(userId: Long): PointResponseDto {
        val user = getUser(userId)
        val point = getPoint(user)

        return PointResponseDto.from(point)
    }

    private fun getUser(userId: Long) =
        userRepository.findById(userId) ?: throw UserNotFountException("존재하는 회원이 없습니다")

    private fun getPoint(user: UserEntity) =
        pointRepository.findByUser(user) ?: PointEntity(user, 0)

    private fun chargePoints(
        point: PointEntity,
        request: PointRequestDto
    ) {
        point.charge(request.amount)
        pointRepository.save(point)
    }

    private fun savePointHistory(user: UserEntity, amount: Long) {
        pointHistoryRepository.saveHistory(user, amount)
    }
}