package org.ktor_lecture.userservice.application.port.`in`

import org.ktor_lecture.userservice.application.service.command.CreateUserCommand

interface CreateUserUseCase {
    fun createUser(toCommand: CreateUserCommand)
}