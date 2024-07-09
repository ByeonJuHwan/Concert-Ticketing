package dev.concert.domain.entity

import dev.concert.domain.entity.status.SeatStatus
import jakarta.persistence.Column
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
@Table(name = "seat")
class SeatEntity (
    concertOption: ConcertOptionEntity,
    price : Long,
    seatNo : Int,
){
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "concert_option_id", foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT))
    var concertOption : ConcertOptionEntity = concertOption
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


    fun changeSeatStatus(status: SeatStatus) {
        this.seatStatus = status
    }
}