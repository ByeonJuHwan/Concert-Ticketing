package dev.concert.domain.service.util

interface DistributedLockStore {
    fun lock(key: Long): String?
    fun unlock(key: Long, value: String): Boolean
}