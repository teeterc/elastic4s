package com.sksamuel.elastic4s.cat

import com.sksamuel.elastic4s.RefreshPolicy
import com.sksamuel.elastic4s.http.ElasticDsl
import com.sksamuel.elastic4s.testkit.DiscoveryLocalNodeProvider
import org.scalatest.{FlatSpec, Matchers}

import scala.util.Try

class CatShardsTest extends FlatSpec with Matchers with DiscoveryLocalNodeProvider with ElasticDsl {

  Try {
    http.execute {
      deleteIndex("catshards1")
    }.await
    http.execute {
      deleteIndex("catshards2")
    }.await
  }

  http.execute {
    bulk(
      indexInto("catshards1/landmarks").fields("name" -> "hampton court palace"),
      indexInto("catshards1/landmarks").fields("name" -> "stonehenge"),
      indexInto("catshards1/landmarks").fields("name" -> "kensington palace"),
      indexInto("catshards2/landmarks").fields("name" -> "blenheim palace"),
      indexInto("catshards2/landmarks").fields("name" -> "london eye"),
      indexInto("catshards2/landmarks").fields("name" -> "tower of london")
    ).refresh(RefreshPolicy.Immediate)
  }.await

  "cats shards" should "return all shards" ignore {
    val result = http.execute {
      catShards()
    }.await
    result.right.get.result.map(_.state).toSet shouldBe Set("STARTED", "UNASSIGNED")
    result.right.get.result.map(_.index).contains("catshards1") shouldBe true
    result.right.get.result.map(_.index).contains("catshards2") shouldBe true
  }
}
