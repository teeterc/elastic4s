package com.sksamuel.elastic4s.search.aggs

import com.sksamuel.elastic4s.http.ElasticDsl
import com.sksamuel.elastic4s.http.search.RangeBucket
import com.sksamuel.elastic4s.testkit.DiscoveryLocalNodeProvider
import org.scalatest.{FreeSpec, Matchers}

import scala.util.Try

class RangeAggregationHttpTest extends FreeSpec with DiscoveryLocalNodeProvider with Matchers with ElasticDsl {

  Try {
    http.execute {
      deleteIndex("rangeaggs")
    }.await
  }

  http.execute {
    createIndex("rangeaggs") mappings {
      mapping("tv") fields(
        textField("name").fielddata(true),
        intField("grade")
      )
    }
  }.await

  http.execute(
    bulk(
      indexInto("rangeaggs/tv").fields("name" -> "Breaking Bad", "grade" -> 9),
      indexInto("rangeaggs/tv").fields("name" -> "Better Call Saul", "grade" -> 9),
      indexInto("rangeaggs/tv").fields("name" -> "Star Trek Discovery", "grade" -> 7),
      indexInto("rangeaggs/tv").fields("name" -> "Game of Thrones", "grade" -> 8),
      indexInto("rangeaggs/tv").fields("name" -> "Designated Survivor", "grade" -> 6),
      indexInto("rangeaggs/tv").fields("name" -> "Walking Dead", "grade" -> 5)
    ).refreshImmediately
  ).await

  "range agg" - {
    "should aggregate ranges" in {

      val resp = http.execute {
        search("rangeaggs").matchAllQuery().aggs {
          rangeAgg("agg1", "grade")
              .unboundedTo("meh", to = 5.5)
              .range("cool", from = 5.5, to = 7.5)
              .unboundedFrom("awesome", from = 7.5)
        }
      }.await.right.get.result

      resp.totalHits shouldBe 6

      val agg = resp.aggs.range("agg1")
      agg.buckets.map(_.copy(data = Map.empty)) shouldBe Seq(
        RangeBucket(Some("meh"), None, Some(5.5), 1, Map.empty),
        RangeBucket(Some("cool"), Some(5.5), Some(7.5), 2, Map.empty),
        RangeBucket(Some("awesome"), Some(7.5), None, 3, Map.empty)
      )
    }
  }
}
