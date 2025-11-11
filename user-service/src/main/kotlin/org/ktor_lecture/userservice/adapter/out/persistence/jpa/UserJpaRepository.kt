package org.ktor_lecture.userservice.adapter.out.persistence.jpa

import org.ktor_lecture.userservice.domain.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository

interface UserJpaRepository: JpaRepository<UserEntity, Long> {
}