package org.ktor_lecture.concertservice.adapter.`in`.web.api

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.ktor_lecture.concertservice.IntegrationTestBase
import org.ktor_lecture.concertservice.application.port.out.ConcertWriteRepository
import org.ktor_lecture.concertservice.application.service.ConcertWriteService
import org.ktor_lecture.concertservice.application.service.command.CreateConcertCommand
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.get
import java.time.LocalDate

class ConcertControllerTest : IntegrationTestBase() {

    @Autowired
    private lateinit var concertWriteRepository: ConcertWriteRepository

    @Autowired
    private lateinit var concertWriteService: ConcertWriteService

    @AfterEach
    fun tearDown() {
        concertWriteRepository.deleteAll()
    }

    @Test
    fun `콘서트를 조회한다`() {
        // given
        val command = CreateConcertCommand(
            concertName = "아이유 콘서트",
            singer = "아이유",
            startDate = LocalDate.now(),
            endDate = LocalDate.now(),
            reserveStartDate = LocalDate.now(),
            reserveEndDate = LocalDate.now()
        )

        concertWriteService.createConcert(command)

        // when && then
        mockMvc.get("/api/v1/concerts")
        {
            param("concertName", "아이유")
        }.andExpect {
            status { isOk() }
            jsonPath("$.data[0].concertName") { value(command.concertName)}
        }
    }

    @Test
    fun `콘서트 이름을 입력할때마다 자동완성된 리스트를 제공한다`() {
        // given
        val command = CreateConcertCommand(
            concertName = "아이유 콘서트",
            singer = "아이유",
            startDate = LocalDate.now(),
            endDate = LocalDate.now(),
            reserveStartDate = LocalDate.now(),
            reserveEndDate = LocalDate.now()
        )

        concertWriteService.createConcert(command)

        val query = "아이"

        // when && then
        mockMvc.get("/api/v1/concerts/suggestions") {
            param("query", query)
        }.andExpect {
            status { isOk() }
            jsonPath("$.data[0]") {value("아이유 콘서트")}
        }
    }
}