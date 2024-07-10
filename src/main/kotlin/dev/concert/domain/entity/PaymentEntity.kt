package dev.concert.domain.entity

import dev.concert.domain.entity.status.PaymentStatus
import dev.concert.domain.entity.status.PaymentType
import jakarta.persistence.ConstraintMode
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name="payment")
class PaymentEntity (
    reservation : ReservationEntity,
    price : Long,
    paymentStatus: PaymentStatus,
    paymentType : PaymentType,
) : BaseEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT))
    var reservation : ReservationEntity = reservation
        protected set

    var price : Long = price
        protected set

    @Enumerated(EnumType.STRING)
    var paymentStatus: PaymentStatus = paymentStatus
        protected set

    @Enumerated(EnumType.STRING)
    var paymentType : PaymentType = paymentType
        protected set
}