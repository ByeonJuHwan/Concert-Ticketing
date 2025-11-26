package org.ktor_lecture.userservice.domain.entity

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
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "point_history")
class PointHistoryEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT))
    val user: UserEntity,

    @Column(nullable = false)
    val amount: Long,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var type: PointTransactionType,
) : BaseEntity() {

    fun cancel() {
        this.type = PointTransactionType.CANCEL
    }
}

enum class PointTransactionType {
    CHARGE,
    USE,
    CANCEL
}