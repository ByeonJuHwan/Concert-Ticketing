package org.ktor_lecture.concertservice.adapter.out.persistence.jpa

import com.querydsl.jpa.impl.JPAQueryFactory
import org.ktor_lecture.concertservice.domain.entity.ConcertOptionEntity
import org.ktor_lecture.concertservice.domain.entity.QConcertEntity
import org.ktor_lecture.concertservice.domain.entity.QConcertOptionEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ConcertOptionJpaRepository: JpaRepository<ConcertOptionEntity, Long>, ConcertOptionJpaRepositoryCustom {
}

interface ConcertOptionJpaRepositoryCustom {
    fun findAvailableDates(concertId: Long): List<ConcertOptionEntity>
}

class ConcertOptionJpaRepositoryImpl (
    private val queryFactory: JPAQueryFactory
) : ConcertOptionJpaRepositoryCustom {
    override fun findAvailableDates(concertId: Long): List<ConcertOptionEntity> {
        val concertOption = QConcertOptionEntity.concertOptionEntity
        val concert = QConcertEntity.concertEntity

        return queryFactory.selectFrom(concertOption)
            .join(concertOption.concert, concert).fetchJoin()
            .where(concertOption.concert.id.eq(concertId))
            .fetch()
    }

}