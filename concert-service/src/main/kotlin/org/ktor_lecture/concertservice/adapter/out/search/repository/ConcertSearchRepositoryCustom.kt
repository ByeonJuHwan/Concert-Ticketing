package org.ktor_lecture.concertservice.adapter.out.search.repository

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery
import co.elastic.clients.elasticsearch._types.query_dsl.MultiMatchQuery
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType
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

    fun getSuggestions(query: String): List<String>
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

    override fun getSuggestions(keyword: String): List<String> {
        val query = MultiMatchQuery.of { m ->
            m.query(keyword)
                .fields(
                    "concertName.auto_complete",
                    "concertName.auto_complete._2gram",
                    "concertName.auto_complete._3gram"
                )
                .type(TextQueryType.BoolPrefix)
        }._toQuery()

        val nativeQuery = NativeQuery.builder()
            .withQuery(query)
            .withMaxResults(10)
            .build()

        return elasticSearchOperations
            .search(nativeQuery, ConcertDocument::class.java)
            .map { it.content.concertName }
            .distinct()
            .toList()
    }
}