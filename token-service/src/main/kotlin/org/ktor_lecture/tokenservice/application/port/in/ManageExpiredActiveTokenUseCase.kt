package org.ktor_lecture.tokenservice.application.port.`in`

interface ManageExpiredActiveTokenUseCase {
    fun deleteExpiredActiveTokens()
}