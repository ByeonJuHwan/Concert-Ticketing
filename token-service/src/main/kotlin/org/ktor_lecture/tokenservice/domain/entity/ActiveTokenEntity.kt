package org.ktor_lecture.tokenservice.domain.entity

import jakarta.persistence.*

@Entity
class ActiveTokenEntity (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(unique = true, nullable = false)
    val token: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "queue_token_user_id", foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT))
    val queueTokenUser: QueueTokenUserEntity,
) {
}