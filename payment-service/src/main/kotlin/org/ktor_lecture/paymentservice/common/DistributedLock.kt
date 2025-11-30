package org.ktor_lecture.paymentservice.common


@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class DistributedLock(
    val key: String,
    val leaseTime: Long = 10000
)
