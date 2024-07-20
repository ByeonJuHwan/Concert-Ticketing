package dev.concert.domain.repository

import dev.concert.domain.entity.UserEntity

interface UserRepository {
    fun findById(id: Long): UserEntity?
    fun save(user: UserEntity): UserEntity
}