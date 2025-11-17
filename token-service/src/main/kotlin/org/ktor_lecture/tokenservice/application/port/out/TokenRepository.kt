package org.ktor_lecture.tokenservice.application.port.out


interface TokenRepository {
    fun findTokenById(key: String): String?
    fun addWaitingQueue(userJson: String, toDouble: Double)
    fun createToken(key: String, token: String)
    fun getTokenExpireTime(key: String): Long?
    fun isTokenInActiveQueue(token: String): Boolean
    fun getRankInWaitingQueue(userJson: String): Long?
}