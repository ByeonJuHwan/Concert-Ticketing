package dev.concert.domain

import dev.concert.domain.entity.UserEntity

interface UserRepository {
    fun findById(id: Long): UserEntity?
    fun save(user: UserEntity): UserEntity
}