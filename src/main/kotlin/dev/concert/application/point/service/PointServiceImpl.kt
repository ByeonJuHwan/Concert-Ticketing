package dev.concert.application.point.service

import dev.concert.application.point.dto.PointResponseDto
import dev.concert.domain.PointRepository
import dev.concert.domain.entity.PointEntity
import dev.concert.domain.entity.UserEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PointServiceImpl (
    private val pointRepository: PointRepository,
) : PointService {

    @Transactional
    override fun chargePoints(user: UserEntity, amount: Long): PointResponseDto {
        val point = getPoint(user)
        chargePoints(point, amount)

        return PointResponseDto.from(point)
    }

    override fun getCurrentPoint(user: UserEntity): PointResponseDto = PointResponseDto.from(getPoint(user))

    private fun getPoint(user: UserEntity) =
        pointRepository.findByUser(user) ?: PointEntity(user, 0)

    private fun chargePoints(
        point: PointEntity,
        amount : Long
    ) {
        point.charge(amount)
        pointRepository.save(point)
    }
}