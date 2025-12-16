package org.ktor_lecture.userservice.application.service

import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.extension.ExtendWith
import org.ktor_lecture.userservice.application.port.out.EventPublisher
import org.ktor_lecture.userservice.application.port.out.UserWriteRepository
import org.ktor_lecture.userservice.application.service.command.CreateUserCommand
import org.ktor_lecture.userservice.domain.entity.UserEntity
import kotlin.test.Test


@ExtendWith(MockKExtension::class)
class UserWriteServiceTest {


    @MockK
    private lateinit var userWriteRepository: UserWriteRepository

    @MockK
    private lateinit var eventPublisher: EventPublisher

    @InjectMockKs
    private lateinit var userService: UserService

    @Test
    fun `유저 회원가입`() {
        // given
        val command = CreateUserCommand(name = "test")
        val savedUserId = 1L

        every { userWriteRepository.save(any())} answers {
            val user = firstArg<UserEntity>()
            UserEntity (
                id = savedUserId,
                name = user.name,
            )
        }
        every { eventPublisher.publish(any()) } just runs

        // when
        userService.createUser(command)

        // then
        verify(exactly = 1) { userWriteRepository.save(any()) }
    }
}