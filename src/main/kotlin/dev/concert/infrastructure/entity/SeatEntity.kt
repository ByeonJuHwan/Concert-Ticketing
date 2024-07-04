package dev.concert.infrastructure.entity

import dev.concert.infrastructure.entity.status.SeatStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "seat")
class SeatEntity (
    concertOptionId: Long,
    price : Long,
    seatNo : Int,
){

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    var concertOptionId : Long = concertOptionId
        protected set

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var seatStatus : SeatStatus = SeatStatus.AVAILABLE
        protected set

    @Column(nullable = false)
    var seatNo : Int = seatNo
        protected set

    @Column(nullable = false)
    var price : Long = price
        protected set
}