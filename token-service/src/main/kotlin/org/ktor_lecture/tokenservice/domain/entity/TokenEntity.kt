package org.ktor_lecture.tokenservice.domain.entity

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
import jakarta.persistence.UniqueConstraint
import org.ktor_lecture.tokenservice.domain.status.QueueTokenStatus
import java.time.LocalDateTime

@Entity
@Table(
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_queue_token_user",
            columnNames = ["queue_token_user_id"]
        )
    ]
)
class TokenEntity (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, unique = true)
    val token: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "queue_token_user_id", foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT))
    val queueTokenUser: QueueTokenUserEntity,

    var expiresAt: LocalDateTime = LocalDateTime.now().plusHours(1),

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: QueueTokenStatus = QueueTokenStatus.WAITING
): BaseEntity() {

    fun changeStatusToActive() {
        this.status = QueueTokenStatus.ACTIVE
    }

    fun changeStatusExpired() {
        this.status = QueueTokenStatus.EXPIRED
    }

    fun isExpired(): Boolean {
        if (this.expiresAt.isBefore(LocalDateTime.now())) {
            changeStatusExpired()
            return true
        }
        return false
    }

    fun isAvailable(): Boolean {
        return this.status == QueueTokenStatus.ACTIVE
    }
}