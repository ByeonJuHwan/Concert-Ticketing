package dev.concert.infrastructure

import dev.concert.domain.PointRepository
import dev.concert.domain.entity.PointEntity
import dev.concert.domain.entity.UserEntity
import dev.concert.infrastructure.jpa.PointJpaRepository
import org.springframework.stereotype.Repository

@Repository
class PointRepositoryImpl (
    private val pointJpaRepository: PointJpaRepository,
) : PointRepository { 
    override fun findByUser(user: UserEntity): PointEntity? { 
        return pointJpaRepository.findByUser(user) 
    } 

    override fun save(point: PointEntity): PointEntity {
        return pointJpaRepository.save(point)
    }

    override fun findByUserWithLock(user: UserEntity): PointEntity? {
        return pointJpaRepository.findByUserWithLock(user)
    }

    override fun deleteAll() {
        return pointJpaRepository.deleteAll()
    }
}
