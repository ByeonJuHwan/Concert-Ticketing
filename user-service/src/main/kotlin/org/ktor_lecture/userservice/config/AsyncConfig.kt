package org.ktor_lecture.userservice.config

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.AsyncConfigurer
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor

@Configuration
class AsyncConfig : AsyncConfigurer {

    private val log : Logger = LoggerFactory.getLogger(AsyncConfig::class.java)

    override fun getAsyncExecutor(): Executor {
        return ThreadPoolTaskExecutor().apply{
            corePoolSize = 5
            maxPoolSize = 10
            queueCapacity = 10
            setThreadNamePrefix("Event - ")
            initialize()
        }
    }

    override fun getAsyncUncaughtExceptionHandler(): AsyncUncaughtExceptionHandler? {
        return AsyncUncaughtExceptionHandler { ex, method, params ->
            when(method.name) {
                "publishUseCreatedEvent" -> {
                    log.error("[${method.name} Exception occurred] Kafka Message Published Error || Exception Message : ${ex.message}", ex)
                }
                else -> {
                    log.error("이벤트 처리 예외 발생!! : ${ex.message}")
                    log.error("메소드 이름 : ${method.name}")
                    log.error("파라미터 : ${params.joinToString()}}")
                }
            }
        }
    }
}