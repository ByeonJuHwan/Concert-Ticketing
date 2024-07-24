package dev.concert.domain.service.util

import org.redisson.api.RLock
import org.springframework.stereotype.Component

@Component
class DistributedLockManager (
    private val distributedLockStore: DistributedLockStore,
) {
    fun lock(key: String, timeout: Long) = distributedLockStore.lock(key, timeout)
    fun unlock(lock: RLock) = distributedLockStore.unlock(lock)
}