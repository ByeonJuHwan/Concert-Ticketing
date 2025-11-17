package org.ktor_lecture.tokenservice.adapter.out

import org.ktor_lecture.tokenservice.adapter.out.persistence.jpa.QueueTokenJpaRepository
import org.ktor_lecture.tokenservice.application.port.out.TokenUserRepository
import org.ktor_lecture.tokenservice.domain.entity.QueueTokenUserEntity
import org.springframework.stereotype.Component
import java.util.Optional

@Component
class TokenUserRepositoryAdapter (
    private val queueTokenJpaRepository: QueueTokenJpaRepository,
): TokenUserRepository {


    override fun createTokenUser(user: QueueTokenUserEntity) {
        queueTokenJpaRepository.save(user)
    }

    override fun getTokenUser(userId: Long): Optional<QueueTokenUserEntity> {
        return queueTokenJpaRepository.findById(userId)
    }
}