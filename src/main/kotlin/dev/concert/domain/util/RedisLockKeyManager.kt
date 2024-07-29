package dev.concert.domain.util

import org.springframework.stereotype.Component

@Component
class RedisLockKeyManager : LockKeyGenerator {
    override fun generateLockKeyWithPrefix(prefix: String, param: Any): String {
        return "$prefix:$param"
    }
}