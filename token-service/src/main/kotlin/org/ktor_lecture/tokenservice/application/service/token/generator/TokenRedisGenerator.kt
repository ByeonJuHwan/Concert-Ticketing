package org.ktor_lecture.tokenservice.application.service.token.generator

import org.ktor_lecture.tokenservice.application.port.out.LockKeyGenerator
import org.ktor_lecture.tokenservice.application.port.out.TokenRepository
import org.ktor_lecture.tokenservice.application.port.out.TokenUserRepository
import org.ktor_lecture.tokenservice.common.Base64Util
import org.ktor_lecture.tokenservice.domain.exception.ConcertException
import org.ktor_lecture.tokenservice.domain.exception.ErrorCode
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import java.util.*


@Component
@Qualifier("REDIS")
class TokenRedisGenerator (
    private val tokenUserRepository: TokenUserRepository,
    @Qualifier("REDIS") private val lockKeyGenerator: LockKeyGenerator,
    @Qualifier("REDIS") private val tokenRepository: TokenRepository,
): TokenGenerator {

    override fun generateToken(userId: Long): String {
        // 유저 정보조회
        val user = tokenUserRepository.getTokenUser(userId)
            .orElseThrow { ConcertException(ErrorCode.USER_NOT_FOUND) }

        // 키 발행
        val key = lockKeyGenerator.generateLockKeyWithPrefix(prefix = "token-user", value = user.id!!.toString())
        var token = tokenRepository.findTokenById(key)

        if(token != null) {
            return token
        }

        token = createQueueToken()

        // 토큰을 WAITING_QUEUE에 추가하고, redis에 추가한다.
        tokenRepository.addWaitingQueue(user, System.currentTimeMillis().toDouble())
        tokenRepository.createToken(key, token)

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