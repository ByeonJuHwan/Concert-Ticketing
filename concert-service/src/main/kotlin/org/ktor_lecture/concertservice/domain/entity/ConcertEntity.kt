package org.ktor_lecture.concertservice.domain.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table

@Entity
@Table(name = "concert" , indexes = [Index(name = "idx_start_date", columnList = "startDate")]) 
class ConcertEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val concertName: String,
    @Column(nullable = false)
    val singer: String,
    @Column(nullable = false)
    val startDate: String,
    @Column(nullable = false)
    val endDate: String,
    @Column(nullable = false)
    val reserveStartDate: String,
    @Column(nullable = false)
    val reserveEndDate: String,
) {
}
