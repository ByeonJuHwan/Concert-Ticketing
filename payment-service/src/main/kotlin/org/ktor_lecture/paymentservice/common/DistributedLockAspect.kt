package org.ktor_lecture.paymentservice.common

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.slf4j.LoggerFactory
import org.springframework.core.annotation.Order
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration.*
import java.util.UUID
import java.util.concurrent.TimeUnit

@Order(1)
@Aspect
@Component
class DistributedLockAspect (
    private val redisTemplate: RedisTemplate<String, String>,
) {

    private val log = LoggerFactory.getLogger(DistributedLockAspect::class.java)


    @Around("@annotation(distributedLock)")
    fun around(joinPoint: ProceedingJoinPoint, distributedLock: DistributedLock): Any? {
        val key = distributedLock.key
        val lockValue = UUID.randomUUID().toString()
        val leaseTimeMillis = TimeUnit.MILLISECONDS.toMillis(distributedLock.leaseTime)

        var lockAcquired = false

        try {
            lockAcquired = redisTemplate.opsForValue().setIfAbsent(key, lockValue, ofMillis(leaseTimeMillis)) ?: false

            if (!lockAcquired) {
                log.warn("락 획득에 실패했습니다.")
                return null
            }

            return joinPoint.proceed()
        } catch (e: InterruptedException) {
            log.error("락 획득 중 인터럽트: key={}", key, e)
            Thread.currentThread().interrupt()
            throw e
        } finally {
            if (lockAcquired) {
                redisTemplate.delete(key)
            }
        }
    }
}