package org.ktor_lecture.userservice.domain.entity

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType.IDENTITY
import jakarta.persistence.Id
import jakarta.persistence.Table

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

}


enum class PointReservationStatus {
    RESERVED,
    CONFIRMED,
    CANCELED,
}