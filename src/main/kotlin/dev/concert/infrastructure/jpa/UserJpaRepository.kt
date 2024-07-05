package dev.concert.infrastructure.jpa

import dev.concert.domain.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository

interface UserJpaRepository : JpaRepository<UserEntity, Long>