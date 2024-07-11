package dev.concert.infrastructure.jpa

import dev.concert.domain.entity.PaymentEntity
import dev.concert.domain.entity.ReservationEntity
import org.springframework.data.jpa.repository.JpaRepository

interface PaymentJpaRepository : JpaRepository<PaymentEntity, Long>{
    fun existsByReservation(reservation: ReservationEntity): Boolean
}