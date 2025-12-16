package org.ktor_lecture.userservice.adapter.out.persistence

import org.ktor_lecture.userservice.adapter.out.persistence.jpa.UserJpaRepository
import org.ktor_lecture.userservice.application.port.out.UserWriteRepository
import org.ktor_lecture.userservice.domain.entity.UserEntity
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class UserWriteRepositoryAdapter (
    private val userJpaRepository: UserJpaRepository,
): UserWriteRepository {

    @Transactional
    override fun save(user: UserEntity): UserEntity {
        return userJpaRepository.save(user)
    }

    @Transactional
    override fun deleteAll() {
        userJpaRepository.deleteAll()
    }
}