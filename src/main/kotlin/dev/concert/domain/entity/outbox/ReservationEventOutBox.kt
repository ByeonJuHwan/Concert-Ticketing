package dev.concert.domain.entity.outbox

import dev.concert.domain.entity.BaseEntity
import dev.concert.domain.entity.status.OutBoxMsgStats
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity
class ReservationEventOutBox(
    reservationId: Long,
) : BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id : Long = 0

    var reservationId : Long = reservationId
        protected set

    @Enumerated(EnumType.STRING)
    var status : OutBoxMsgStats = OutBoxMsgStats.INIT
        protected set
}