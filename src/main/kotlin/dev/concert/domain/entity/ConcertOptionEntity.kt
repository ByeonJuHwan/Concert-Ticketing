package dev.concert.domain.entity

import jakarta.persistence.ConstraintMode
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "concert_option")
class ConcertOptionEntity (
    concert: ConcertEntity,
    availableSeats : Int,
    concertDate : String,
    concertTime : String,
    concertVenue : String,
) : BaseEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id : Long = 0

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "concert_id", foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT))
    var concert : ConcertEntity = concert
        protected set

    var availableSeats : Int = availableSeats
        protected set

    var concertVenue : String = concertVenue
        protected set

    var concertDate : String = concertDate
        protected set

    var concertTime : String = concertTime
        protected set
}