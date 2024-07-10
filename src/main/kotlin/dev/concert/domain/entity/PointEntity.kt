package dev.concert.domain.entity

import jakarta.persistence.Column
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
@Table(name = "point")
class PointEntity(
    user: UserEntity,
    point : Long
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT))
    var user : UserEntity = user
        protected set

    @Column(nullable = false)
    var point : Long = point
        protected set

    fun charge(amount: Long) {
        if (amount <= 0) throw IllegalArgumentException("0보다 작은 값을 충전할 수 없습니다")
        point += amount
    }

    fun deduct(amount: Long) {
        point -= amount
    }
}