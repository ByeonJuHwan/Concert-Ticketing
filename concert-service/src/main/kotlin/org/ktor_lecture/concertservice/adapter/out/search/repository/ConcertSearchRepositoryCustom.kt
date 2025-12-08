package org.ktor_lecture.concertservice.adapter.out.search.repository

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery
import org.ktor_lecture.concertservice.adapter.out.search.document.ConcertDocument
import org.springframework.data.elasticsearch.client.elc.NativeQuery
import org.springframework.data.elasticsearch.core.ElasticsearchOperations

interface ConcertSearchRepositoryCustom {
    fun searchByOptions(
        concertName: String?,
        singer: String?,
        startDate: String?,
        endDate: String?
    ): List<ConcertDocument>
}

class ConcertSearchRepositoryCustomImpl(
    private val elasticSearchOperations: ElasticsearchOperations,
): ConcertSearchRepositoryCustom {

    override fun searchByOptions(
        concertName: String?,
        singer: String?,
        startDate: String?,
        endDate: String?
    ): List<ConcertDocument> {

        val mustQueries = mutableListOf<Query>()

        concertName?.let {
            val multiMatchQuery = MatchQuery.of { m ->
                m.query(concertName)
                    .field("concertName")
                    .fuzziness("AUTO")
            }._toQuery()

            mustQueries.add(multiMatchQuery)
        }

        singer?.let {
            val matchQuery = MatchQuery.of { m ->
                m.query(it)
                    .field("singer")
                    .fuzziness("AUTO")
            }._toQuery()

            mustQueries.add(matchQuery)
        }

        startDate?.let {
            val rangeQuery = RangeQuery.of { r ->
                r.date { d ->
                    d.field("startDate").gte(it)
                }
            }._toQuery()

            mustQueries.add(rangeQuery)
        }

        endDate?.let {
            val rangeQuery = RangeQuery.of { r ->
                r.date { d ->
                    d.field("endDate").lte(it)
                }
            }._toQuery()

            mustQueries.add(rangeQuery)
        }

        val boolQuery = BoolQuery.of { b->
            b.must(mustQueries)
        }

        val nativeQuery = NativeQuery.builder()
            .withQuery(boolQuery._toQuery())
            .build()

        return elasticSearchOperations
            .search(nativeQuery, ConcertDocument::class.java)
            .map { it.content }
            .toList()
    }
}