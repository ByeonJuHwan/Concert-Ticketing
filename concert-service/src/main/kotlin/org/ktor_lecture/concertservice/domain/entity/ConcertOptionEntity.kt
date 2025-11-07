package org.ktor_lecture.concertservice.domain.entity

import jakarta.persistence.Column
import jakarta.persistence.ConstraintMode
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table( 
    name = "concert_option", 
    indexes = [Index(name = "idx_concert_id", columnList = "concert_id")] 
) 
class ConcertOptionEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "concert_id", foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT))
    val concert: ConcertEntity,

    @Column(nullable = false)
    val availableSeats: Int,
    @Column(nullable = false)
    val concertDate: String,
    @Column(nullable = false)
    val concertTime: String,
    @Column(nullable = false)
    val concertVenue: String,
) : BaseEntity() {
}
