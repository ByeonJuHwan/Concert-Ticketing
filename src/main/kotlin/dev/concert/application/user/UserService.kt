package dev.concert.application.user

import dev.concert.application.user.dto.UserDto

interface UserService {
    fun findUser(userId : Long) : UserDto
}