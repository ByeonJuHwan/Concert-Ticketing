package org.ktor_lecture.userservice.application.port.out

import org.ktor_lecture.userservice.domain.entity.PointEntity
import org.ktor_lecture.userservice.domain.entity.UserEntity
import java.util.Optional

interface PointRepository {

    fun getCurrentPoint(user: UserEntity): PointEntity?
    fun save(point: PointEntity)
    fun findById(userId: Long): Optional<PointEntity>
    fun deleteAll()
}