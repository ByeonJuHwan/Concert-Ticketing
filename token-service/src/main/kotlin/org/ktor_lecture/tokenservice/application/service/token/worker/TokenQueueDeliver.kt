package org.ktor_lecture.tokenservice.application.service.token.worker

interface TokenQueueDeliver {
    fun deliverWaitingToActiveQueue()
}