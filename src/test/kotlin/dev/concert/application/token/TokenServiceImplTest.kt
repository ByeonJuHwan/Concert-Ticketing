package dev.concert.application.token

import dev.concert.domain.TokenRepository
import dev.concert.domain.UserRepository
import dev.concert.domain.entity.QueueTokenEntity
import dev.concert.domain.entity.UserEntity
import dev.concert.util.Base64Util
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class TokenServiceImplTest {

    @Mock
    lateinit var tokenRepository: TokenRepository

    @Mock
    lateinit var userRepository: UserRepository

    @InjectMocks
    lateinit var tokenServiceImpl: TokenServiceImpl

    @Test
    fun `특정 유저에 대해서 토큰이 생성 되어야 한다`() {
        // given
        val userId = 1L
        val user = UserEntity(name = "test")
        `when`(userRepository.findById(userId)).thenReturn(user)
        `when`(tokenRepository.findLastQueueOrder()).thenReturn(10)

        // when
        val token = tokenServiceImpl.generateToken(userId)

        // then
        assertNotNull(token)
    }
}