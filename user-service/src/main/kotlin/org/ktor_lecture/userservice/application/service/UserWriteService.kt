package org.ktor_lecture.userservice.application.service

import org.ktor_lecture.userservice.application.port.`in`.CreateUserUseCase
import org.ktor_lecture.userservice.application.port.out.UserWriteRepository
import org.ktor_lecture.userservice.application.service.command.CreateUserCommand
import org.ktor_lecture.userservice.domain.entity.UserEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserWriteService (
    private val userWriteRepository: UserWriteRepository,
): CreateUserUseCase  {

    @Transactional
    override fun createUser(command: CreateUserCommand) {
        val user = UserEntity(
            name = command.name,
        )

        userWriteRepository.save(user)

        // TODO kafka로 다른 DB로 보내주기
    }
}