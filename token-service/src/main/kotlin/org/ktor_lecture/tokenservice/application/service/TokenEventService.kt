package org.ktor_lecture.tokenservice.application.service

import org.ktor_lecture.tokenservice.application.port.`in`.QueueTokenUserCreateUseCase
import org.ktor_lecture.tokenservice.application.port.out.TokenRepository
import org.ktor_lecture.tokenservice.domain.entity.QueueTokenUserEntity
import org.ktor_lecture.tokenservice.domain.event.UserCreatedEvent
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TokenEventService (
    private val tokenRepository: TokenRepository,
): QueueTokenUserCreateUseCase {

    @Transactional
    override fun createTokenUser(event: UserCreatedEvent) {
        val user = QueueTokenUserEntity(
            username = event.userName,
        )

        tokenRepository.createTokenUser(user)
    }
}