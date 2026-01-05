package org.ktor_lecture.userservice.adapter.out.persistence.jpa.query

import com.querydsl.jpa.impl.JPAQueryFactory
import org.ktor_lecture.userservice.domain.annotation.ReadOnlyTransactional
import org.ktor_lecture.userservice.domain.entity.QUserEntity
import org.ktor_lecture.userservice.domain.entity.UserEntity
import org.springframework.stereotype.Component
import java.util.*

interface UserQueryRepository {
    fun findById(id: Long): Optional<UserEntity>
}

@Component
class UserQueryRepositoryImpl (
    private val queryFactory: JPAQueryFactory,
): UserQueryRepository {

    @ReadOnlyTransactional
    override fun findById(id: Long): Optional<UserEntity> {
        val user = QUserEntity.userEntity

        return Optional.ofNullable(queryFactory.selectFrom(user).where(user.id.eq(id)).fetchOne())
    }
}