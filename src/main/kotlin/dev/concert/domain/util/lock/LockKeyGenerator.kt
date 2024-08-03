package dev.concert.domain.util.lock
 
interface LockKeyGenerator { 
 
    fun generateLockKeyWithPrefix(prefix : String, param : Any): String 
} 
