package org.ktor_lecture.userservice.adapter.out.persistence

import org.ktor_lecture.userservice.adapter.out.persistence.jpa.PointJpaRepository
import org.ktor_lecture.userservice.application.port.out.PointRepository
import org.ktor_lecture.userservice.domain.entity.PointEntity
import org.ktor_lecture.userservice.domain.entity.UserEntity
import org.springframework.stereotype.Component

@Component
class PointRepositoryAdapter (
    private val pointJpaRepository: PointJpaRepository,
): PointRepository {


    override fun getCurrentPoint(user: UserEntity): PointEntity? {
        return pointJpaRepository.findByUser(user)
    }

    override fun save(point: PointEntity) {
        pointJpaRepository.save(point)
    }
}