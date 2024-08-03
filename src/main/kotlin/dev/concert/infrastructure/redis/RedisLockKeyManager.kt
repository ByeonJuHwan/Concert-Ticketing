package dev.concert.infrastructure.redis

import dev.concert.domain.util.lock.LockKeyGenerator
import org.springframework.stereotype.Component
 
@Component 
class RedisLockKeyManager : LockKeyGenerator { 
    override fun generateLockKeyWithPrefix(prefix: String, param: Any): String { 
        return "$prefix:$param" 
    } 
} 
