package dev.concert.domain.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table

@Entity
@Table(name = "concert" , indexes = [Index(name = "idx_start_date", columnList = "startDate")])
class ConcertEntity(
    concertName: String,
    singer: String,
    startDate: String,
    endDate: String,
    reserveStartDate: String,
    reserveEndDate: String,
) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    var concertName: String = concertName
        protected set

    var singer: String = singer
        protected set

    var startDate: String = startDate
        protected set

    var endDate: String = endDate
        protected set

    var reserveStartDate: String = reserveStartDate
        protected set

    var reserveEndDate: String = reserveEndDate
        protected set


}