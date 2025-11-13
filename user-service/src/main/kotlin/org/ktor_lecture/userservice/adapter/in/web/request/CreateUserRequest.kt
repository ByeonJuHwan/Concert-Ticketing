package org.ktor_lecture.userservice.adapter.`in`.web.request

import org.ktor_lecture.userservice.application.service.command.CreateUserCommand

data class CreateUserRequest(
    val name: String,
)

fun CreateUserRequest.toCommand() = CreateUserCommand(name = name)