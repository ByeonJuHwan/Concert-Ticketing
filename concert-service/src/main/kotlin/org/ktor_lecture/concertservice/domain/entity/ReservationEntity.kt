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
import org.ktor_lecture.concertservice.domain.status.ReservationStatus
import java.time.LocalDateTime

@Entity
@Table(name = "reservation", indexes = [Index(name = "idx_expires_at", columnList = "expires_at")]) 
class ReservationEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id : Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT))
    val user: ConcertUserEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id", foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT))
    val seat: SeatEntity,

    @Column(nullable = false)
    val expiresAt: LocalDateTime,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var status: ReservationStatus = ReservationStatus.PENDING
): BaseEntity() {

    fun changeStatus(status: ReservationStatus) {
        this.status = status
    }

}
