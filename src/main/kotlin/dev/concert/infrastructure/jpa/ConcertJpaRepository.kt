package dev.concert.infrastructure.jpa

import dev.concert.domain.entity.ConcertEntity
import dev.concert.domain.entity.QConcertEntity.concertEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Repository
interface ConcertJpaRepository : JpaRepository<ConcertEntity, Long>, CustomConcertRepository

interface CustomConcertRepository {
    fun findAllByStartDateAfter(): List<ConcertEntity>
}

class ConcertJpaRepositoryImpl : CustomConcertRepository, QuerydslRepositorySupport(ConcertEntity::class.java) {
    override fun findAllByStartDateAfter(): List<ConcertEntity> {
        val today = getToday()
        return from(concertEntity)
            .where(concertEntity.startDate.goe(today))
            .fetch()
    }

    private fun getToday(): String {
        val format = DateTimeFormatter.ofPattern("yyyyMMdd")
        return LocalDate.now().format(format)
    }
}