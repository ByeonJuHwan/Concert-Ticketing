package dev.concert.infrastructure.jpa


import dev.concert.domain.entity.SeatEntity
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface SeatJpaRepository : JpaRepository<SeatEntity, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM SeatEntity s JOIN FETCH s.concertOption co JOIN FETCH co.concert c WHERE s.id = :seatId")
    fun getSeatWithLock(@Param("seatId") seatId: Long): SeatEntity?
}