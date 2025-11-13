package org.ktor_lecture.userservice.application.service

import org.ktor_lecture.userservice.application.port.`in`.CreateUserUseCase
import org.ktor_lecture.userservice.application.port.out.EventPublisher
import org.ktor_lecture.userservice.application.port.out.UserWriteRepository
import org.ktor_lecture.userservice.application.service.command.CreateUserCommand
import org.ktor_lecture.userservice.domain.entity.UserEntity
import org.ktor_lecture.userservice.domain.event.UserCreatedEvent
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserWriteService(
    private val userWriteRepository: UserWriteRepository,
    @Qualifier("application") private val eventPublisher: EventPublisher,
) : CreateUserUseCase {

    @Transactional
    override fun createUser(command: CreateUserCommand) {
        val user = UserEntity(
            name = command.name,
        )

        val savedUser = userWriteRepository.save(user)

        val userCreatedEvent = UserCreatedEvent(
            userId = savedUser.id!!.toString(),
            userName = savedUser.name,
        )

        eventPublisher.publish(userCreatedEvent)
    }
}