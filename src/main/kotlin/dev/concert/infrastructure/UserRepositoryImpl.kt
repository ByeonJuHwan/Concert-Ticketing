package dev.concert.infrastructure

import dev.concert.domain.repository.UserRepository
import dev.concert.domain.entity.UserEntity
import dev.concert.infrastructure.jpa.UserJpaRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class UserRepositoryImpl (
    private val userJpaRepository: UserJpaRepository
) : UserRepository {
    override fun findById(id: Long) = userJpaRepository.findByIdOrNull(id)
    override fun save(user: UserEntity): UserEntity = userJpaRepository.saveAndFlush(user)
    override fun deleteAll() {
        userJpaRepository.deleteAll()
    }
}