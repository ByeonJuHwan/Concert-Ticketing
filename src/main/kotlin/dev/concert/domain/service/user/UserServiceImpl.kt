package dev.concert.domain.service.user

import dev.concert.domain.repository.UserRepository
import dev.concert.domain.entity.UserEntity
import dev.concert.exception.UserNotFountException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserServiceImpl (
    private val userRepository: UserRepository
) : UserService {

    @Transactional(readOnly = true)
    override fun getUser(userId: Long): UserEntity {
        return userRepository.findById(userId) ?: throw UserNotFountException("존재하는 회원이 없습니다")
    }

    @Transactional
    override fun saveUser(user: UserEntity): UserEntity {
        return userRepository.save(user)
    }
}
