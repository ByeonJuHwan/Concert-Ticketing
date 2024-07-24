package dev.concert.domain.service.util

import org.springframework.stereotype.Component

@Component
class DistributedLockManager(
    private val distributedLockStore: DistributedLockStore
) {
    fun lock(lockKey: Long): String? {
        return distributedLockStore.lock(lockKey)
    }

    fun unlock(lockKey: Long, lockValue: String): Boolean {
        return distributedLockStore.unlock(lockKey, lockValue)
    }
}