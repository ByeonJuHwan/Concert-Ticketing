package org.ktor_lecture.concertservice.common


@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class DistributedLock(
    val key: String,
    val leaseTime: Long = 10000
)
