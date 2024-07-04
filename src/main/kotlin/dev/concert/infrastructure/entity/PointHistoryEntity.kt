package dev.concert.infrastructure.entity

import dev.concert.infrastructure.entity.status.PointTransactionType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "point_history")
class PointHistoryEntity (
    userId: Long,
    amount : Long,
    type : PointTransactionType,
) : BaseEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id : Long = 0

    @Column(nullable = false)
    var userId : Long = userId
        protected set

    @Column(nullable = false)
    val amount: Long = amount

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var type : PointTransactionType = type
        protected set
}