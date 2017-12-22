package com.sksamuel.elastic4s.cat

import com.sksamuel.elastic4s.{ElasticsearchClientUri, RefreshPolicy}
import com.sksamuel.elastic4s.http.{ElasticDsl, HttpClient}
import com.sksamuel.elastic4s.testkit.DiscoveryLocalNodeProvider
import org.scalatest.{FlatSpec, Matchers}

class CatAliasTest extends FlatSpec with Matchers with DiscoveryLocalNodeProvider with ElasticDsl {

  http.execute {
    indexInto("catalias/landmarks").fields("name" -> "hampton court palace").refresh(RefreshPolicy.Immediate)
  }.await

  http.execute {
    aliases(
      addAlias("ally1").on("catalias"),
      addAlias("ally2").on("catalias")
    )
  }.await


  "cats aliases" should "return all aliases" in {
    val result = http.execute {
      catAliases()
    }.await
    result.right.get.result.map(_.alias).toSet.contains("ally1") shouldBe true
    result.right.get.result.map(_.alias).toSet.contains("ally2") shouldBe true
  }
}
