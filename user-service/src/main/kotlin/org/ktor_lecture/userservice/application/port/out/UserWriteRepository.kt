package org.ktor_lecture.userservice.application.port.out

import org.ktor_lecture.userservice.domain.entity.UserEntity

interface UserWriteRepository {
    fun save(user: UserEntity): UserEntity
    fun deleteAll ()
}