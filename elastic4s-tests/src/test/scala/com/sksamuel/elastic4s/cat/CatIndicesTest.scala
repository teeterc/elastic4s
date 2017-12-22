package com.sksamuel.elastic4s.cat

import com.sksamuel.elastic4s.http.ElasticDsl
import com.sksamuel.elastic4s.testkit.DiscoveryLocalNodeProvider
import com.sksamuel.elastic4s.{HealthStatus, RefreshPolicy}
import org.scalatest.{FlatSpec, Matchers}

class CatIndicesTest extends FlatSpec with Matchers with DiscoveryLocalNodeProvider with ElasticDsl {

  http.execute {
    bulk(
      indexInto("catindex1/landmarks").fields("name" -> "hampton court palace"),
      indexInto("catindex2/landmarks").fields("name" -> "hampton court palace"),
      indexInto("catindex3/landmarks").fields("name" -> "hampton court palace")
    ).refresh(RefreshPolicy.Immediate)
  }.await


  "catIndices" should "return all indexes" in {
    val indexes = http.execute {
      catIndices()
    }.await.right.get.result.map(_.index).toSet
    indexes.contains("catindex1") shouldBe true
    indexes.contains("catindex2") shouldBe true
    indexes.contains("catindex3") shouldBe true
  }

  it should "use health param" in {
    http.execute {
      catIndices(HealthStatus.Red)
    }.await.right.get.result.isEmpty shouldBe true
  }

  it should "include pri.store.size" in {
    http.execute {
      catIndices()
    }.await.right.get.result.head.priStoreSize > 0 shouldBe true
  }

}
