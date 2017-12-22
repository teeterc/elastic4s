package com.sksamuel.elastic4s.indexes

import com.sksamuel.elastic4s.http.ElasticDsl
import com.sksamuel.elastic4s.testkit.DiscoveryLocalNodeProvider
import org.scalatest.{Matchers, WordSpec}

import scala.util.Try

class ExistsTest extends WordSpec with Matchers with ElasticDsl with DiscoveryLocalNodeProvider {

  Try {
    http.execute {
      deleteIndex("exists")
    }.await
  }

  http.execute {
    createIndex("exists").mappings {
      mapping("flowers") fields textField("name")
    }
  }.await

  http.execute {
    indexInto("exists/flowers").withId("a").fields("name" -> "Narcissus")
  }.await

  "an exists request" should {
    "return true for an existing doc" in {
      http.execute {
        exists("a", "exists", "flowers")
      }.await.right.get.result shouldBe true
    }
    "return false for non existing doc" in {
      http.execute {
        exists("b", "exists", "flowers")
      }.await.right.get.result shouldBe false
    }
  }
}
