package org.ktor_lecture.userservice.domain.entity

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
import org.ktor_lecture.userservice.domain.exception.ConcertException
import org.ktor_lecture.userservice.domain.exception.ErrorCode

@Entity
@Table(name = "point")
class PointEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT))
    val user: UserEntity,

    @Column(nullable = false)
    var point : Long,

) {
    fun charge(amount: Long) {
        if (amount <= 0) throw IllegalArgumentException("0보다 작은 값을 충전할 수 없습니다")
        point += amount
    }

    fun deduct(amount: Long) {
        point -= amount
    }

    fun use(amount: Long) {
        if(this.point < amount) {
            throw ConcertException(ErrorCode.POINT_NOT_ENOUGH)
        }

        this.point -= amount
    }

    fun cancel(amount: Long) {
        this.point += amount
    }
}