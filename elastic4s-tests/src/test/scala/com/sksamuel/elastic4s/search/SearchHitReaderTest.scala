package com.sksamuel.elastic4s.search

import com.sksamuel.elastic4s.RefreshPolicy
import com.sksamuel.elastic4s.http.ElasticDsl
import com.sksamuel.elastic4s.testkit.DiscoveryLocalNodeProvider
import org.scalatest.{FlatSpec, Matchers}

class SearchHitReaderTest extends FlatSpec with Matchers with DiscoveryLocalNodeProvider with ElasticDsl {

  import com.sksamuel.elastic4s.jackson.ElasticJackson.Implicits._

  "SearchHit" should "support HitReader[T] for complex types" in {

    val focus = Car(
      name = "Focus",
      manufacturer = Manufacturer("Ford", Seq(Location("Detroit"), Location("Slough"), Location("Mexico City"))),
      models = Map("GTI" -> Seq(Feature("Air Con"), Feature("Power Steering")), "Sport" -> Seq(Feature("Spoiler")))
    )

    http.execute {
      createIndex("cars").mappings(
        mapping("models")
      )
    }.await

    http.execute {
      indexInto("cars" / "models").doc(focus).refresh(RefreshPolicy.Immediate)
    }.await

    Thread.sleep(3000)

    http.execute {
      search("cars").matchAllQuery().limit(1)
    }.await.right.get.result.safeTo[Car] shouldBe Seq(Right(focus))
  }
}

case class Car(name: String, manufacturer: Manufacturer, models: Map[String, Seq[Feature]])
case class Manufacturer(name: String, locations: Seq[Location])
case class Location(name: String)
case class Feature(name: String)
