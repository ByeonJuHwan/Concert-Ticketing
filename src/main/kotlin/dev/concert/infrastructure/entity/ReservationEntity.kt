package dev.concert.infrastructure.entity

import dev.concert.infrastructure.entity.status.ReservationStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "reservation")
class ReservationEntity(
    userId: Long,
    seatNo: Int,
    concertName: String,
    price: Long,
    expiresAt: LocalDateTime,
    concertDate: String,
    concertTime: String,
    concertVenue: String,
): BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id : Long = 0

    @Column(nullable = false)
    var userId: Long = userId
        protected set

    @Column(nullable = false)
    var seatNo: Int = seatNo
        protected set

    @Column(nullable = false)
    var price : Long = price
        protected set

    @Column(nullable = false)
    var expiresAt: LocalDateTime = expiresAt
        protected set

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var status: ReservationStatus = ReservationStatus.PENDING
        protected set

    @Column(nullable = false)
    var concertName : String = concertName
        protected set

    @Column(nullable = false)
    var concertVenue : String = concertVenue
        protected set

    @Column(nullable = false)
    var concertDate : String = concertDate
        protected set

    @Column(nullable = false)
    var concertTime : String = concertTime
        protected set

}