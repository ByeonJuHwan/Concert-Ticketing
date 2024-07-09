package dev.concert.application.user.dto

import dev.concert.domain.entity.UserEntity

data class UserDto(
    val username: String,
) {
    companion object {
        fun from(user: UserEntity): UserDto {
            return UserDto (
                username = user.name,
            )
        }
    }
}
