package dev.concert.infrastructure

import dev.concert.domain.UserRepository
import dev.concert.infrastructure.jpa.UserJpaRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class UserRepositoryImpl (
    private val userJpaRepository: UserJpaRepository
) : UserRepository {
    override fun findById(id: Long) = userJpaRepository.findByIdOrNull(id)
}