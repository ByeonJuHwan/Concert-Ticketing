package org.ktor_lecture.concertservice.adapter.out.search.document

import org.ktor_lecture.concertservice.domain.entity.ConcertEntity
import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.DateFormat
import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType
import org.springframework.data.elasticsearch.annotations.Setting
import java.time.LocalDate

@Document(indexName = "concerts")
@Setting(settingPath = "/elasticsearch/settings.json")
class ConcertDocument (

    @Id
    val id: String,

    @Field(type = FieldType.Text, analyzer = "nori")
    val concertName: String,

    @Field(type = FieldType.Text, analyzer = "nori")
    val singer: String,

    @Field(type = FieldType.Date, format = [DateFormat.date])
    val startDate: LocalDate,

    @Field(type = FieldType.Date, format = [DateFormat.date])
    val endDate: LocalDate,

    @Field(type = FieldType.Date, format = [DateFormat.date])
    val reserveStartDate: LocalDate,

    @Field(type = FieldType.Date, format = [DateFormat.date])
    val reserveEndDate: LocalDate,

) {

    companion object {
        fun from(entity: ConcertEntity): ConcertDocument {
            return ConcertDocument(
                id = entity.id.toString(),
                concertName = entity.concertName,
                singer = entity.singer,
                startDate = entity.startDate,
                endDate = entity.endDate,
                reserveStartDate = entity.reserveStartDate,
                reserveEndDate = entity.reserveEndDate,
            )
        }
    }
}