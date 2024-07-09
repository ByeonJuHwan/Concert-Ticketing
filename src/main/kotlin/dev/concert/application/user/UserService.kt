package dev.concert.application.user

import dev.concert.domain.entity.UserEntity

interface UserService {
    fun getUser(userId: Long): UserEntity
}