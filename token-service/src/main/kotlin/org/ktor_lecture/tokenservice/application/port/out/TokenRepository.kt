package org.ktor_lecture.tokenservice.application.port.out


interface TokenRepository {
    fun findTokenById(key: String): String?
    fun addWaitingQueue(userJson: String, toDouble: Double)
    fun createToken(key: String, token: String)
    fun getTokenExpireTime(key: String): Long?
    fun deleteAllActiveTokens(): Boolean
    fun isTokenInActiveQueue(token: String): Boolean
    fun getRankInWaitingQueue(userJson: String): Long?
    fun findTopWaitingTokens(start: Long, end: Long): Set<String>?
    fun addActiveQueue(token: String)
    fun removeWaitingQueueToken(userJson: String)
    fun deleteToken(key: String)
}