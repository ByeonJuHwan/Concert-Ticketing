package dev.concert.domain.service.point

import dev.concert.domain.repository.PointRepository
import dev.concert.domain.entity.PointEntity
import dev.concert.domain.entity.PointHistoryEntity
import dev.concert.domain.entity.UserEntity
import dev.concert.domain.entity.status.PointTransactionType
import dev.concert.domain.repository.PointHistoryRepository
import dev.concert.exception.NotEnoughPointException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PointServiceImpl (
    private val pointRepository: PointRepository,
    private val pointHistoryRepository: PointHistoryRepository,
) : PointService {

    @Transactional 
    override fun chargePoints(user: UserEntity, amount: Long): PointEntity {
        val point = getPoint(user)
        val chargePoints = chargePoints(point, amount)
        savePointHistoryCharged(user, amount)
        return chargePoints
    }

    @Transactional(readOnly = true)
    override fun getCurrentPoint(user: UserEntity): PointEntity = getPoint(user)

    @Transactional(readOnly = true)
    override fun checkPoint(user: UserEntity, price: Long) : PointEntity {
        val currentPoint = getPoint(user)
        if(currentPoint.point < price){
            throw NotEnoughPointException("포인트가 부족합니다.")
        }
        return currentPoint
    }

    @Transactional
    override fun deductPoints(currentPoint: PointEntity, price: Long) {
        currentPoint.deduct(price)
        pointRepository.save(currentPoint)
    }

    private fun getPoint(user: UserEntity) : PointEntity =
        pointRepository.findByUser(user) ?: PointEntity(user, 0)

    private fun chargePoints(
        point: PointEntity,
        amount : Long
    ) : PointEntity {
        point.charge(amount)
        return pointRepository.save(point)
    }

    private fun savePointHistoryCharged(user: UserEntity, amount: Long) {
        pointHistoryRepository.saveHistory(PointHistoryEntity(user, amount, PointTransactionType.CHARGE))
    }
}
