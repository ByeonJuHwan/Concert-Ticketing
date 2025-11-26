package org.ktor_lecture.paymentservice.domain.entity

import jakarta.persistence.*
import org.ktor_lecture.paymentservice.domain.exception.ConcertException
import org.ktor_lecture.paymentservice.domain.exception.ErrorCode

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
    var paymentStatus: PaymentStatus,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val paymentType : PaymentType,
) : BaseEntity() {

    fun cancel() {
        if(this.paymentStatus != PaymentStatus.SUCCESS) {
            throw ConcertException(ErrorCode.PAYMENT_NOT_SUCCESS)
        }
        this.paymentStatus = PaymentStatus.CANCEL
    }
}

enum class PaymentStatus {
    SUCCESS,
    CANCEL,
    FAILED,
    PENDING,
}

enum class PaymentType {
    CARD,
    CASH,
    POINT,
    COUPON,
    ETC,
}