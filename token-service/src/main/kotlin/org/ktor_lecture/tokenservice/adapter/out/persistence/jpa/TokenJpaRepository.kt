package org.ktor_lecture.tokenservice.adapter.out.persistence.jpa

import com.querydsl.jpa.impl.JPAQueryFactory
import org.ktor_lecture.tokenservice.domain.entity.QTokenEntity
import org.ktor_lecture.tokenservice.domain.entity.TokenEntity
import org.springframework.data.jpa.repository.JpaRepository

interface TokenJpaRepository: JpaRepository<TokenEntity, Long>, TokenJpaCustomRepository {
}

interface TokenJpaCustomRepository {
    fun findByUserId(userId: Long): String?
}

class TokenJpaCustomRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
): TokenJpaCustomRepository {

    override fun findByUserId(userId: Long): String? {
        val tokenEntity = QTokenEntity.tokenEntity

        val entity = queryFactory
            .selectFrom(tokenEntity)
            .where(tokenEntity.queueTokenUser.id.eq(userId))
            .fetchOne()

        return entity?.token
    }
}
