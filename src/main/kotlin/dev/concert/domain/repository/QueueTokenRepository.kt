package dev.concert.domain.repository
 
interface QueueTokenRepository { 
    fun findTokenByKey(key: String) : String? 
    fun addWaitingQueue(userJson: String, currentTime: Double) 
    fun createToken(userKey: String, token: String) 
    fun getTokenExpireTime(userKey: String) : Long? 
    fun deleteTokenActiveQueue(token: String) 
    fun deleteToken(userKey: String) 
    fun findTopWaitingTokens(start: Long, end: Long): Set<String>? 
    fun addActiveQueue(token: String) 
    fun removeWaitingQueueToken(userJson: String) 
    fun isTokenInActiveQueue(userJson : String) : Boolean 
    fun getRankInWaitingQueue(userJson: String) : Long? 
} 
