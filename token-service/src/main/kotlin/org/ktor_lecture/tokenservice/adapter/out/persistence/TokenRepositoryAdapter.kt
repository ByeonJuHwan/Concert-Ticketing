package org.ktor_lecture.tokenservice.adapter.out.persistence

import org.ktor_lecture.tokenservice.adapter.out.persistence.jpa.QueueTokenJpaRepository
import org.ktor_lecture.tokenservice.application.port.out.TokenRepository
import org.ktor_lecture.tokenservice.domain.entity.QueueTokenUserEntity
import org.springframework.stereotype.Component

@Component
class TokenRepositoryAdapter(
    private val queueTokenJpaRepository: QueueTokenJpaRepository,
): TokenRepository {
    override fun createTokenUser(user: QueueTokenUserEntity) {
        queueTokenJpaRepository.save(user)
    }
}