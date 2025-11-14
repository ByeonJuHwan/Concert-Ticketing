package org.ktor_lecture.userservice.adapter.`in`.batch

import org.ktor_lecture.userservice.application.port.`in`.UserCreatedEventRetryUseCase
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class UsersEventRetryScheduler (
    private val userCreatedEventRetryUseCase: UserCreatedEventRetryUseCase,
) {

    /**
     * [아웃박스 패턴]
     * 이벤트의 상태가 SENT 가 아니면서,
     * CREATED_AT 이 현 시간 기준으로 10분 이상 넘어간 이벤트 재시도
     */
    @Scheduled(fixedRate = 60000)
    fun userCreatedEventRetryScheduler() {
        userCreatedEventRetryUseCase.userCreatedEventRetryScheduler()
    }
}