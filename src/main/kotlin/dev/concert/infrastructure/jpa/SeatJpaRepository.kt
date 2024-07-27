package dev.concert.infrastructure.jpa


import dev.concert.domain.entity.SeatEntity
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface SeatJpaRepository : JpaRepository<SeatEntity, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE) 
    @Query("SELECT s FROM SeatEntity s WHERE s.id = :seatId") 
    fun getSeatWithLock(@Param("seatId") seatId: Long): SeatEntity? 

    @Modifying(clearAutomatically = true)
    @Query("UPDATE SeatEntity s SET s.seatStatus = 'AVAILABLE' WHERE s.id IN :seatIds")
    fun updateSeatStatusToAvailable(seatIds: List<Long>)
}
