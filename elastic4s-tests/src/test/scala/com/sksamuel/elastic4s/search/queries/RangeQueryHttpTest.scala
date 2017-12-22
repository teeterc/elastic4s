package com.sksamuel.elastic4s.search.queries

import com.sksamuel.elastic4s.RefreshPolicy
import com.sksamuel.elastic4s.http.ElasticDsl
import com.sksamuel.elastic4s.testkit.{DiscoveryLocalNodeProvider, ElasticMatchers, HttpElasticSugar}
import org.scalatest.WordSpec

import scala.util.Try

class RangeQueryHttpTest
  extends WordSpec
    with HttpElasticSugar
    with DiscoveryLocalNodeProvider
    with ElasticMatchers
    with ElasticDsl {

  Try {
    http.execute {
      ElasticDsl.deleteIndex("rangequeryhttptest")
    }.await
  }

  http.execute {
    createIndex("rangequeryhttptest").mappings(
      mapping("pieces").fields(
        textField("name").fielddata(true)
      )
    )
  }.await

  http.execute {
    bulk(
      indexInto("rangequeryhttptest/pieces").fields(
        "name" -> "queen",
        "value" -> 10,
        "count" -> 1
      ),
      indexInto("rangequeryhttptest/pieces").fields(
        "name" -> "king",
        "value" -> 0,
        "count" -> 1
      ),
      indexInto("rangequeryhttptest/pieces").fields(
        "name" -> "bishop",
        "value" -> 3,
        "count" -> 2
      ),
      indexInto("rangequeryhttptest/pieces").fields(
        "name" -> "knight",
        "value" -> 3,
        "count" -> 2
      ),
      indexInto("rangequeryhttptest/pieces").fields(
        "name" -> "rook",
        "value" -> 5,
        "count" -> 2
      ),
      indexInto("rangequeryhttptest/pieces").fields(
        "name" -> "pawn",
        "value" -> 1,
        "count" -> 8
      )
    ).refresh(RefreshPolicy.Immediate)
  }.await

  "a range query" should {
    "support using gte" in {
      val resp = http.execute {
        search("rangequeryhttptest") query {
          // bishop, rook, castle, queen
          rangeQuery("value").gte("3")
        }
      }.await.right.get.result
      resp.totalHits shouldBe 4
    }
    "support lte" in {
      val resp = http.execute {
        search("rangequeryhttptest") query {
          // pawns, king, bisho
          rangeQuery("value").lte("3")
        }
      }.await.right.get.result
      resp.totalHits shouldBe 4
    }
    "support using both lte & gte" in {
      val resp = http.execute {
        search("rangequeryhttptest") query {
          rangeQuery("value").gte("5").lte("7")
        }
      }.await.right.get.result
      resp.totalHits shouldBe 1
    }
    "support integers" in {
      val resp = http.execute {
        search("rangequeryhttptest") query {
          rangeQuery("value").gte(5).lte(7)
        }
      }.await.right.get.result
      resp.totalHits shouldBe 1
    }
    "support doubles" in {
      val resp = http.execute {
        search("rangequeryhttptest") query {
          rangeQuery("value").gte(5.0).lte(7.0)
        }
      }.await.right.get.result
      resp.totalHits shouldBe 1
    }
    "support boost" in {
      val resp = http.execute {
        search("rangequeryhttptest") query {
          rangeQuery("value").lte("3").boost(14.5)
        }
      }.await.right.get.result
      resp.maxScore shouldBe 14.5
    }
  }
}
