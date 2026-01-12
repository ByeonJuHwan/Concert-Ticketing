package org.ktor_lecture.concertservice.application.service

import org.ktor_lecture.concertservice.domain.annotation.ReadOnlyTransactional
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

interface TransactionRunner {
    fun <T> run(func: () -> T?): T?
    fun <T> readOnly(func: () -> T?): T?
    fun <T> runNew(func: () -> T?): T?
}

@Component
class TransactionAdvice : TransactionRunner {

    @Transactional
    override fun <T> run(func: () -> T?): T? {
        return func()
    }

    @ReadOnlyTransactional
    override fun <T> readOnly(func: () -> T?): T? {
        return func()
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    override fun <T> runNew(func: () -> T?): T? {
        return func()
    }

}

@Component
class TxAdvice (
    private val advice: TransactionAdvice,
) {

    fun <T> run(func: () -> T?): T? = advice.run(func)
    fun <T> readOnly(func: () -> T?): T? = advice.readOnly(func)
    fun <T> runNew(func: () -> T?): T? = advice.runNew(func)
}