package dev.concert.infrastructure

import dev.concert.domain.repository.PaymentRepository
import dev.concert.domain.entity.PaymentEntity
import dev.concert.domain.entity.ReservationEntity
import dev.concert.infrastructure.jpa.PaymentJpaRepository
import org.springframework.stereotype.Repository

@Repository
class PaymentRepositoryImpl (
    private val paymentRepository: PaymentJpaRepository,
) : PaymentRepository {
    override fun save(paymentEntity: PaymentEntity): PaymentEntity {
        return paymentRepository.save(paymentEntity)
    }

    override fun existsByReservation(reservation: ReservationEntity): Boolean {
        return paymentRepository.existsByReservation(reservation)
    }
}