package dev.concert.application.token

import dev.concert.domain.TokenRepository
import dev.concert.domain.UserRepository
import dev.concert.domain.entity.QueueTokenEntity
import dev.concert.domain.entity.UserEntity
import dev.concert.exception.UserNotFountException
import dev.concert.util.Base64Util
import org.springframework.stereotype.Service
import java.util.*

const val TOKEN_EXPIRATION_TIME_SECONDS = 60 * 60L
@Service
class TokenServiceImpl (
    private val tokenRepository: TokenRepository,
    private val userRepository: UserRepository,
) : TokenService {
    override fun generateToken(userId: Long): String {
        // 유저를찾고
        val user = getUser(userId)

        // userId 를 암호화해서 토큰을 생성하고
        val token = encodeUserId(userId)

        // 현재 몇번째 순서인지 확인하기 위해서 조회??
        // 토큰 테이블에서 Pending 상태인 토큰들을 조회 후, 가장 마지막 순서를 조회
        val queueOrder = getQueueOrder()

        // 토큰 엔티티 생성
        val queueToken = queueTokenEntity(user, token, queueOrder)

        // 토큰 저장
        tokenRepository.saveToken(queueToken)

        return token
    }

    private fun queueTokenEntity(
        user: UserEntity,
        token: String,
        queueOrder: Int
    ) = QueueTokenEntity(
        user = user,
        token = token,
        queueOrder = queueOrder,
        remainingTime = TOKEN_EXPIRATION_TIME_SECONDS,
    )

    private fun getQueueOrder() = tokenRepository.findLastQueueOrder() + 1

    private fun encodeUserId(userId: Long) : String {
        val uuid = UUID.randomUUID().toString()
        val timeStamp = System.currentTimeMillis().toString()
        return Base64Util.encode((uuid + timeStamp).toByteArray())
    }

    private fun getUser(userId: Long) =
        userRepository.findById(userId) ?: throw UserNotFountException("존재하는 회원이 없습니다")
}

