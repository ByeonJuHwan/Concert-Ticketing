package org.ktor_lecture.concertservice.domain.entity

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
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.ktor_lecture.concertservice.domain.exception.ConcertException
import org.ktor_lecture.concertservice.domain.exception.ErrorCode
import org.ktor_lecture.concertservice.domain.status.ReservationStatus
import org.ktor_lecture.concertservice.domain.status.SeatStatus

@Entity
@Table( 
    name = "seat", 
    indexes = [Index(name = "idx_seat_status_concert_option_id", columnList = "seat_status,concert_option_id")] 
) 
class SeatEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "concert_option_id", foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT))
    val concertOption: ConcertOptionEntity,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var seatStatus: SeatStatus = SeatStatus.AVAILABLE,

    @Column(nullable = false)
    val price: Long,

    @Column(nullable = false)
    val seatNo: Int,
) {
    fun temporarilyReserve() {
        if (seatStatus != SeatStatus.AVAILABLE){
             throw ConcertException(ErrorCode.SEAT_NOT_AVAILABLE)
         }

        this.seatStatus = SeatStatus.TEMPORARILY_ASSIGNED
    }

    fun changeStatus(seatStatus: SeatStatus) {
        this.seatStatus = seatStatus
    }

    fun reserve() {
        if (seatStatus != SeatStatus.TEMPORARILY_ASSIGNED) {
            throw ConcertException(ErrorCode.SEAT_NOT_TEMPORARILY_ASSIGNED)
        }

        this.seatStatus = SeatStatus.RESERVED
    }

    fun temporarilyAssign() {
        if(seatStatus != SeatStatus.RESERVED) {
            throw ConcertException(ErrorCode.SEAT_NOT_RESERVED)
        }
        this.seatStatus = SeatStatus.TEMPORARILY_ASSIGNED
    }
}
