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
import org.ktor_lecture.tokenservice.domain.status.QueueTokenStatus
import java.time.LocalDateTime

@Entity
class WaitingQueueEntity (

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "queue_token_user_id", foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT))
    val queueTokenUser: QueueTokenUserEntity,

    @Column(nullable = false)
    val score: Double,

): BaseEntity()

