package dev.concert.infrastructure.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "concert_option")
class ConcertOptionEntity (
    concertId: Long,
    concertDate : String,
    concertTime : String,
    concertVenue : String,
) : BaseEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id : Long = 0

    var concertId : Long = concertId
        protected set

    var concertVenue : String = concertVenue
        protected set

    var concertDate : String = concertDate
        protected set

    var concertTime : String = concertTime
        protected set
}