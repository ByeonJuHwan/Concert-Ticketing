package org.ktor_lecture.concertservice.adapter.out.persistence.jpa

import org.ktor_lecture.concertservice.domain.entity.ConcertUserEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ConcertUserJpaRepository: JpaRepository<ConcertUserEntity, Long> {
}