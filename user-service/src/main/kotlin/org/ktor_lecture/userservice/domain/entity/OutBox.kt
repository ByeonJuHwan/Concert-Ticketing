package org.ktor_lecture.userservice.domain.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "outbox")
class OutBox (

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "event_Id", nullable = false)
    val eventId: String,

    // 이벤트가 발생한 엔티티 타입 (User, Reservation...)
    @Column(name = "aggregate_type", nullable = false)
    val aggregateType: String,

    // 엔티티 ID
    @Column(name = "aggregate_id", nullable = false)
    val aggregateId: String,

    // 이벤트 타입(UserCreatedEvent..)
    @Column(name = "event_type", nullable = false)
    val eventType: String,

    // 이벤트 페이로드 (Json)
    @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
    var payload: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    var status: OutboxStatus = OutboxStatus.PENDING,

    @Column(name = "retry_count", nullable = false)
    var retryCount: Int = 0,

    @Column(name = "max_retry_count", nullable = false)
    var maxRetryCount: Int = 3,

): BaseEntity() {
}

enum class OutboxStatus {
    PENDING,
    SENT,
    FAILED
}