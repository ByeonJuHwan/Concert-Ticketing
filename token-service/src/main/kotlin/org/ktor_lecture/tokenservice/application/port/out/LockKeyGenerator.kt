package org.ktor_lecture.tokenservice.application.port.out

interface LockKeyGenerator {

    fun generateLockKeyWithPrefix(prefix: String, value: String): String
}