package dev.concert.domain.service.user

import dev.concert.domain.repository.UserRepository
import dev.concert.domain.entity.UserEntity
import dev.concert.domain.exception.ConcertException
import dev.concert.domain.exception.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserServiceImpl (
    private val userRepository: UserRepository
) : UserService {

    @Transactional(readOnly = true)
    override fun getUser(userId: Long): UserEntity {
        return userRepository.findById(userId) ?: throw ConcertException(ErrorCode.USER_NOT_FOUND)
    }

    @Transactional
    override fun saveUser(user: UserEntity): UserEntity {
        return userRepository.save(user)
    }
}
