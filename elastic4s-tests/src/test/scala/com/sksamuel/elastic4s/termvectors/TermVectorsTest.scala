package com.sksamuel.elastic4s.termvectors

import com.sksamuel.elastic4s.RefreshPolicy
import com.sksamuel.elastic4s.http.ElasticDsl
import com.sksamuel.elastic4s.http.termvectors._
import com.sksamuel.elastic4s.testkit.DiscoveryLocalNodeProvider
import org.scalatest.{FlatSpec, Matchers}

class TermVectorsTest extends FlatSpec with Matchers with ElasticDsl with DiscoveryLocalNodeProvider {

  http.execute {
    createIndex("hansz").mappings(
      mapping("albums").fields(
        textField("name").stored(true).termVector("with_positions_offsets_payloads"),
        intField("rating")
      )
    )
  }.await

  http.execute(
    bulk(
      indexInto("hansz/albums").fields("name" -> "interstellar", "rating" -> 10) id "1",
      indexInto("hansz/albums").fields("name" -> "lion king", "rating" -> 8) id "2"
    ).refresh(RefreshPolicy.Immediate)
  ).await

  "term vectors" should "return full stats" in {
    val response = http.execute {
      termVectors("hansz", "albums", "1")
    }.await.right.get.result

    response.index shouldBe "hansz"
    response.`type` shouldBe "albums"
    response.id shouldBe "1"
    response.found shouldBe true
    response.termVectors shouldBe Map("name" -> TermVectors(FieldStatistics(1, 1, 1), Map("interstellar" -> Terms(0, 0, 1.0, 1, List(Token(0, 0, 12))))))
  }
}
