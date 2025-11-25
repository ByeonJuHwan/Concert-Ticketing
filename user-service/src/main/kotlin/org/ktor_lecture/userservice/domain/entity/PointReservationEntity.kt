package org.ktor_lecture.userservice.domain.entity

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType.IDENTITY
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.lang.RuntimeException

@Entity
@Table(name = "point_reservation")
class PointReservationEntity (
    @Id
    @GeneratedValue(strategy = IDENTITY)
    val id: Long? = null,

    val requestId: String,

    val pointId: Long,

    val reservedAmount: Long,

    @Enumerated(EnumType.STRING)
    var status: PointReservationStatus = PointReservationStatus.RESERVED,
) {

    fun confirm() {
        if (this.status == PointReservationStatus.CANCELED) {
            throw RuntimeException("취소된 예약은 확정할 수 없습니다.")
        }
        this.status = PointReservationStatus.CONFIRMED
    }

    fun cancel() {
        if (this.status == PointReservationStatus.CONFIRMED) {
            throw RuntimeException("확정된 예약은 취소할 수 없습니다.")
        }

        this.status = PointReservationStatus.CANCELED
    }
}


enum class PointReservationStatus {
    RESERVED,
    CONFIRMED,
    CANCELED,
}