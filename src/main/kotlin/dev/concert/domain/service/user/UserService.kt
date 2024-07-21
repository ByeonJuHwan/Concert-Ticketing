package dev.concert.domain.service.user

import dev.concert.domain.entity.UserEntity

interface UserService {
    fun getUser(userId: Long): UserEntity
    fun saveUser(user: UserEntity): UserEntity
}