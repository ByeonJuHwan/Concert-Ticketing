package dev.concert.infrastructure

import dev.concert.domain.TokenRepository
import dev.concert.domain.entity.QueueTokenEntity
import dev.concert.domain.entity.UserEntity
import dev.concert.domain.entity.status.QueueTokenStatus
import dev.concert.infrastructure.jpa.TokenJpaRepository
import org.springframework.stereotype.Repository

@Repository 
class TokenRepositoryImpl( 
    private val tokenJpaRepository: TokenJpaRepository 
) : TokenRepository { 
 
    override fun saveToken(tokenEntity: QueueTokenEntity): QueueTokenEntity { 
        return tokenJpaRepository.save(tokenEntity) 
    } 
 
    override fun findFirstQueueOrderId(): Long { 
        tokenJpaRepository.findFirstIdInQueueOrderStatusWating(QueueTokenStatus.WAITING)?.let{ 
            return it.id 
        } 
        return 0 
    } 
 
    override fun findByToken(token: String): QueueTokenEntity? { 
        return tokenJpaRepository.findByToken(token) 
    } 
 
    override fun deleteToken(user: UserEntity) { 
        tokenJpaRepository.deleteByUser(user) 
    } 
 
    override fun findWaitingAndActiveTokens(): List<QueueTokenEntity> { 
        return tokenJpaRepository.findAvaliableTokens() 
    } 
 
    override fun deleteByToken(tokenEntity: QueueTokenEntity) { 
        tokenJpaRepository.delete(tokenEntity) 
    } 
}
