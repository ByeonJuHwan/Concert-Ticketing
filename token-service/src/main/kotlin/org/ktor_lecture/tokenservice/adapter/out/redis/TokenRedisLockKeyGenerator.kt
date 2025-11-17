package org.ktor_lecture.tokenservice.adapter.out.redis

import org.ktor_lecture.tokenservice.application.port.out.LockKeyGenerator
import org.springframework.stereotype.Component

@Component
class TokenRedisLockKeyGenerator : LockKeyGenerator {

    override fun generateLockKeyWithPrefix(prefix: String, value: String): String {
        return "$prefix:$value"
    }
}