package org.ktor_lecture.tokenservice.application.service

import org.ktor_lecture.tokenservice.adapter.`in`.web.response.TokenInfoResponse
import org.ktor_lecture.tokenservice.adapter.`in`.web.response.TokenResponse
import org.ktor_lecture.tokenservice.application.port.`in`.CreateTokenUseCase
import org.ktor_lecture.tokenservice.application.port.`in`.GetTokenStatusUseCase
import org.ktor_lecture.tokenservice.application.port.out.LockKeyGenerator
import org.ktor_lecture.tokenservice.application.port.out.TokenRepository
import org.ktor_lecture.tokenservice.application.port.out.TokenUserRepository
import org.ktor_lecture.tokenservice.application.service.command.CreateTokenCommand
import org.ktor_lecture.tokenservice.common.Base64Util
import org.ktor_lecture.tokenservice.common.JsonUtil
import org.ktor_lecture.tokenservice.domain.exception.ConcertException
import org.ktor_lecture.tokenservice.domain.exception.ErrorCode
import org.ktor_lecture.tokenservice.domain.status.QueueTokenStatus
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class TokenService (
    private val tokenRepository: TokenRepository,
    private val tokenUserRepository: TokenUserRepository,
    private val lockKeyGenerator: LockKeyGenerator,
): CreateTokenUseCase, GetTokenStatusUseCase {


    /**
     * 대기열 토큰 발행
     *
     * 1. 유저 정보 조회
     * 2. 토큰이 있다면 기존 토큰 반환
     * 3. 토큰이 없다면 토큰을 생성하고 WAITING_QUEUE에 추가한다
     */
    override fun createToken(command: CreateTokenCommand): TokenResponse {
        // 유저 정보조회
        val user = tokenUserRepository.getTokenUser(command.userId)
            .orElseThrow { ConcertException(ErrorCode.USER_NOT_FOUND) }

        // 키 발행
        val key = lockKeyGenerator.generateLockKeyWithPrefix(prefix = "token-user", value = user.id!!.toString())
        val token = tokenRepository.findTokenById(key) ?: createQueueToken()

        // 토큰을 WAITING_QUEUE에 추가하고, redis에 추가한다.
        val userJson = JsonUtil.encodeToJson(user)
        tokenRepository.addWaitingQueue(userJson, System.currentTimeMillis().toDouble())
        tokenRepository.createToken(key, token)

        return TokenResponse(
            token = token,
        )
    }

    /**
     * 현재 토큰의 상태를 조회한다
     */
    override fun getToken(userId: Long): TokenInfoResponse {
        // 유저 정보조회
        val user = tokenUserRepository.getTokenUser(userId)
            .orElseThrow { ConcertException(ErrorCode.USER_NOT_FOUND) }

        val key = lockKeyGenerator.generateLockKeyWithPrefix(prefix = "token-user", value = user.id!!.toString())
        val token = tokenRepository.findTokenById(key) ?: throw ConcertException(ErrorCode.TOKEN_NOT_FOUND)
        val remainingTime = tokenRepository.getTokenExpireTime(key) ?: 0L
        val userJson = JsonUtil.encodeToJson(user)

        // QUEUE 에서 상태 확인
        val status: QueueTokenStatus = checkQueueTokenStatus(userJson, token)

        // 순서조회
        val queueOrder = if (status == QueueTokenStatus.WAITING) {
            tokenRepository.getRankInWaitingQueue(userJson)?.toInt() ?: -1
        } else {
            0
        }

        return TokenInfoResponse(
            token = token,
            status = status.toString(),
            queueOrder = queueOrder,
            remainingTime = remainingTime
        )
    }

    /**
     * ACTIVE_QUEUE, WAITING_QUEUE 에 토큰이 존재하는지 확인한다
     */
    private fun checkQueueTokenStatus(userJson: String, token: String): QueueTokenStatus {
        return when {
            tokenRepository.isTokenInActiveQueue(token) -> QueueTokenStatus.ACTIVE
            tokenRepository.getRankInWaitingQueue(userJson) != null -> QueueTokenStatus.WAITING
            else -> throw ConcertException(ErrorCode.TOKEN_NOT_FOUND)
        }
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