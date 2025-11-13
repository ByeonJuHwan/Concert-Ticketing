package org.ktor_lecture.userservice.adapter.out.persistence

import org.ktor_lecture.userservice.adapter.out.persistence.jpa.UserJpaRepository
import org.ktor_lecture.userservice.application.port.out.UserWriteRepository
import org.ktor_lecture.userservice.domain.entity.UserEntity
import org.springframework.stereotype.Component

@Component
class UserWriteRepositoryAdapter (
    private val userJpaRepository: UserJpaRepository,
): UserWriteRepository {

    override fun save(user: UserEntity): UserEntity {
        return userJpaRepository.save(user)
    }
}