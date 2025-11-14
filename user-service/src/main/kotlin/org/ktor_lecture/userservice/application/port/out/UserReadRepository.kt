package org.ktor_lecture.userservice.application.port.out

import org.ktor_lecture.userservice.domain.entity.UserEntity
import java.util.Optional

interface UserReadRepository {
    fun findById(userId: Long): Optional<UserEntity>
}