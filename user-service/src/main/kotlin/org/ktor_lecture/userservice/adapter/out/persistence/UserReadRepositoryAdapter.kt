package org.ktor_lecture.userservice.adapter.out.persistence

import org.ktor_lecture.userservice.adapter.out.persistence.jpa.UserJpaRepository
import org.ktor_lecture.userservice.application.port.out.UserReadRepository
import org.ktor_lecture.userservice.domain.entity.UserEntity
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.Optional

@Component
class UserReadRepositoryAdapter (
    private val userJpaRepository: UserJpaRepository,
): UserReadRepository {

    @Transactional(readOnly = true)
    override fun findById(userId: Long): Optional<UserEntity> {
        return userJpaRepository.findById(userId)
    }

    @Transactional(readOnly = true)
    override fun findAll(): List<UserEntity> {
        return userJpaRepository.findAll()
    }

}