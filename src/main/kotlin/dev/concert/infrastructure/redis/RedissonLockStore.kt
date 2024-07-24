package dev.concert.infrastructure.redis

import dev.concert.domain.service.util.DistributedLockStore
import org.redisson.api.RLock
import org.redisson.api.RedissonClient
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class RedissonLockStore (
    private val redissonClient: RedissonClient
) : DistributedLockStore {

    override fun lock(key: String, timeout: Long): RLock {
        val lock = redissonClient.getLock(key)
        lock.lock(timeout, TimeUnit.MILLISECONDS)
        return lock
    }

    override fun unlock(lock: RLock) {
        if (lock.isHeldByCurrentThread) {
            lock.unlock()
        }
    }

    override fun tryLock(key: String, waitTime: Long, leaseTime: Long): Boolean {
        val lock = redissonClient.getLock(key)
        return lock.tryLock(waitTime, leaseTime, TimeUnit.MILLISECONDS)
    }
}