package org.ktor_lecture.tokenservice.adapter.out.persistence

import org.ktor_lecture.tokenservice.application.port.out.LockKeyGenerator
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

@Component
@Qualifier("DB")
class TokenDbLockKeyGenerator : LockKeyGenerator {

    override fun generateLockKeyWithPrefix(prefix: String, value: String): String {
        return "$value"
    }
}