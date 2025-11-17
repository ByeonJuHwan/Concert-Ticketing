package org.ktor_lecture.tokenservice.application.port.out

import org.ktor_lecture.tokenservice.domain.entity.QueueTokenUserEntity
import java.util.Optional

interface TokenUserRepository {
    fun createTokenUser(user: QueueTokenUserEntity)
    fun getTokenUser(userId: Long): Optional<QueueTokenUserEntity>

}