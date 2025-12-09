package org.ktor_lecture.concertservice.adapter.out.persistence.jpa

import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.jpa.impl.JPAQueryFactory
import org.ktor_lecture.concertservice.domain.entity.ConcertEntity
import org.ktor_lecture.concertservice.domain.entity.QConcertEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

interface ConcertJpaRepository : JpaRepository<ConcertEntity, Long>, ConcertJpaRepositoryCustom {
}

interface ConcertJpaRepositoryCustom {
    fun findConcertsByOptions(concertName: String?, singer: String?, startDate: LocalDate?, endDate: LocalDate?): List<ConcertEntity>
}

class ConcertJpaRepositoryCustomImpl (
    private val queryFactory: JPAQueryFactory,
) : ConcertJpaRepositoryCustom {



    override fun findConcertsByOptions(concertName: String?, singer: String?, startDate: LocalDate?, endDate: LocalDate?): List<ConcertEntity> {
        val concert = QConcertEntity.concertEntity


        return queryFactory
            .selectFrom(concert)
            .where(
                concertNameLike(concertName, concert),
                concertSingerLike(singer, concert),
                startDateGoe(startDate, concert),
                endDateLoe(endDate, concert),
            )
            .fetch()
    }

    private fun concertNameLike(concertName: String?, concert: QConcertEntity): BooleanExpression? {
        return concertName?.let {
            concert.concertName.containsIgnoreCase(it)
        }
    }

    private fun concertSingerLike(singer: String?, concert: QConcertEntity): BooleanExpression? {
        return singer?.let {
            concert.singer.containsIgnoreCase(it)
        }
    }

    private fun startDateGoe(startDate: LocalDate?, concert: QConcertEntity): BooleanExpression? {
        return startDate?.let {
            concert.startDate.goe(startDate)
        }
    }

    private fun endDateLoe(endDate: LocalDate?, concert: QConcertEntity): BooleanExpression? {
        return endDate?.let {
            concert.endDate.loe(endDate)
        }
    }
}
