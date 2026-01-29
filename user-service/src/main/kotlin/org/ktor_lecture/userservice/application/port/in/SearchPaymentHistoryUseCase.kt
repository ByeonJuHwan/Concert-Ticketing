package org.ktor_lecture.userservice.application.port.`in`

import org.ktor_lecture.userservice.adapter.`in`.web.response.SearchPaymentHistoryResponse

interface SearchPaymentHistoryUseCase {
    suspend fun searchGrpcPaymentDetailHistory(userId: Long): SearchPaymentHistoryResponse
    fun searchHttpPaymentDetailHistory(userId: Long): SearchPaymentHistoryResponse
}