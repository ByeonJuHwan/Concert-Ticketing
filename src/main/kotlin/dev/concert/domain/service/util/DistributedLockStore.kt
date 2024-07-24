package dev.concert.domain.service.util

import org.redisson.api.RLock

interface DistributedLockStore {
    fun lock(key: String, timeout: Long): RLock
    fun unlock(lock: RLock)
    fun tryLock(key: String, waitTime: Long, leaseTime: Long): Boolean
}