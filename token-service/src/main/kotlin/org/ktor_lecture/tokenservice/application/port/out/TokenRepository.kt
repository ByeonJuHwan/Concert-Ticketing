package org.ktor_lecture.tokenservice.application.port.out

import org.ktor_lecture.tokenservice.domain.entity.QueueTokenUserEntity

interface TokenRepository {
    fun createTokenUser(user: QueueTokenUserEntity)
}