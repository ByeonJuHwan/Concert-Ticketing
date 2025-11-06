package org.ktor_lecture.concertservice.adapter.out.persistence.jpa

import org.ktor_lecture.concertservice.domain.entity.ConcertEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ConcertJpaRepository : JpaRepository<ConcertEntity, Long> {
}
