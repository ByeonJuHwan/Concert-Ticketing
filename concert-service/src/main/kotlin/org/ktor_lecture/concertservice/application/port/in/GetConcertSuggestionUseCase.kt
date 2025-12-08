package org.ktor_lecture.concertservice.application.port.`in`

interface GetConcertSuggestionUseCase {
    fun getConcertSuggestions(query: String): List<String>
}