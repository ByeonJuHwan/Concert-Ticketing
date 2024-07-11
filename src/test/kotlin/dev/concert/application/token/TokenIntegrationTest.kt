package dev.concert.application.token

import dev.concert.application.user.UserService
import dev.concert.domain.entity.UserEntity
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional

@Transactional
@SpringBootTest 
class TokenIntegrationTest {

    @Autowired
    private lateinit var tokenFacade: TokenFacade

    @Autowired
    private lateinit var userService: UserService

    @Test 
    fun `userId 로 토큰을 암호화 하여 발급한다`() { 
        val user = userService.saveUser(UserEntity("변주환")) 
 
        // when 
        val generateToken = tokenFacade.generateToken(user.id) 
 
        // then 
        assertThat(generateToken).isNotBlank() 
    } 

    @Test 
    fun `발급된 토큰의 유효시간 보다 현재시간이 전이면 TRUE 를 반환한다`() { 
        val user = userService.saveUser(UserEntity("변주환")) 
        val token = tokenFacade.generateToken(user.id) 
 
        // when 
        val isTokenAllowed = tokenFacade.isTokenExpired(token) 
 
        // then 
        assertThat(isTokenAllowed).isFalse() 
    } 

    @Test 
    fun `현재 토큰정보를 반환한다 `() { 
        val user = userService.saveUser(UserEntity("변주환")) 
        val token = tokenFacade.generateToken(user.id) 
 
        // when 
        val tokenResponseDto = tokenFacade.getToken(token) 
 
        // then 
        assertThat(tokenResponseDto.token).isEqualTo(token) 
    } 
} 
