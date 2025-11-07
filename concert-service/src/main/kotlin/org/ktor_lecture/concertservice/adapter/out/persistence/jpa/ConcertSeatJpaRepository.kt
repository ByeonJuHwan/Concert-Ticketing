package org.ktor_lecture.concertservice.adapter.out.persistence.jpa

import org.ktor_lecture.concertservice.domain.entity.SeatEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ConcertSeatJpaRepository: JpaRepository<SeatEntity, Long> {

    fun findByConcertOptionId(findByConcertOptionId: Long): List<SeatEntity>
}