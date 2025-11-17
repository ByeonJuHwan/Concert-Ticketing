package org.ktor_lecture.paymentservice.domain.entity

import jakarta.persistence.*

@Entity
@Table(name="payment")
class PaymentEntity (

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val price : Long,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val paymentStatus: PaymentStatus,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val paymentType : PaymentType,
) : BaseEntity() {
}

enum class PaymentStatus {
    SUCCESS,
    CANCEL,
    PENDING,
}

enum class PaymentType {
    CARD,
    CASH,
    POINT,
    COUPON,
    ETC,
}