package dev.concert.application.user

import dev.concert.application.user.dto.UserDto
import dev.concert.domain.UserRepository
import dev.concert.exception.UserNotFountException
import org.springframework.stereotype.Service

@Service
class UserServiceImpl (
    private val userRepository: UserRepository,
)  : UserService {
    override fun findUser(userId: Long): UserDto {
        val user = userRepository.findById(userId) ?: throw UserNotFountException("존재하는 회원이 없습니다")
        return UserDto.from(user)
    }
}