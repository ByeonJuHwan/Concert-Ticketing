package org.ktor_lecture.tokenservice.application.service.token.generator

import org.ktor_lecture.tokenservice.application.port.out.TokenRepository
import org.ktor_lecture.tokenservice.application.port.out.TokenUserRepository
import org.ktor_lecture.tokenservice.common.Base64Util
import org.ktor_lecture.tokenservice.domain.entity.QueueTokenUserEntity
import org.ktor_lecture.tokenservice.domain.exception.ConcertException
import org.ktor_lecture.tokenservice.domain.exception.ErrorCode
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Component
@Qualifier("DB")
class TokenDbGenerator(
    private val tokenUserRepository: TokenUserRepository,
    @Qualifier("DB") private val tokenRepository: TokenRepository,
): TokenGenerator {

    @Transactional
    override fun generateToken(userId: Long): String {
        // 유저 정보조회
        val user: QueueTokenUserEntity = tokenUserRepository.getTokenUser(userId)
            .orElseThrow { ConcertException(ErrorCode.USER_NOT_FOUND) }

        var token = tokenRepository.findTokenById(userId.toString())

        if(token != null) {
            return token
        }

        // 토큰을 WAITING_QUEUE에 추가하고 토큰 생성
        token = createQueueToken()

        tokenRepository.addWaitingQueue(user, System.currentTimeMillis().toDouble())
        tokenRepository.saveToken(user, token)

        return token
    }

    /**
     * 기본 Token 을 만든다
     */
    private fun createQueueToken(): String {
        val uuid = UUID.randomUUID().toString()
        val timeStamp = System.currentTimeMillis().toString()
        return Base64Util.encode((uuid + timeStamp).toByteArray())
    }
}