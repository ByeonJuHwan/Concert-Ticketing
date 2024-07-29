package dev.concert.domain.util

interface LockKeyGenerator {

    fun generateLockKeyWithPrefix(prefix : String, param : Any): String
}