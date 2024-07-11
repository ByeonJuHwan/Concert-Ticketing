package dev.concert.domain.entity

import dev.concert.domain.entity.status.QueueTokenStatus
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
import java.time.LocalDateTime

@Entity
@Table(name = "queue_token")
class QueueTokenEntity (
    token : String,
    user : UserEntity,
) : BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id : Long = 0

    @Column(nullable = false)
    var token: String = token
        protected set

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT))
    var user : UserEntity = user
        protected set

    // 만료 시간은 1시간으로 설정
    var expiresAt: LocalDateTime = LocalDateTime.now().plusHours(1)
        protected set

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: QueueTokenStatus = QueueTokenStatus.WAITING
        protected set

    fun changeStatusToActive() {
        this.status = QueueTokenStatus.ACTIVE
    }

    fun changeStatusExpired() {
        this.status = QueueTokenStatus.EXPIRED
    }
}