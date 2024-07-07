package dev.concert.infrastructure.jpa

import dev.concert.domain.entity.ConcertOptionEntity
import dev.concert.domain.entity.QConcertEntity.concertEntity
import dev.concert.domain.entity.QConcertOptionEntity.concertOptionEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport

interface ConcertOptionJpaRepository : JpaRepository<ConcertOptionEntity, Long> , ConcertOptionJpaRepositoryCustom

interface ConcertOptionJpaRepositoryCustom {
    fun findAvailableDates(concertId: Long): List<ConcertOptionEntity>
}

class ConcertOptionJpaRepositoryImpl : ConcertOptionJpaRepositoryCustom, QuerydslRepositorySupport(ConcertOptionEntity::class.java) {
    override fun findAvailableDates(concertId: Long): List<ConcertOptionEntity> {
        return from(concertOptionEntity)
            .join(concertOptionEntity.concert, concertEntity).fetchJoin()
            .where(concertOptionEntity.concert.id.eq(concertId))
            .fetch()
    }
}