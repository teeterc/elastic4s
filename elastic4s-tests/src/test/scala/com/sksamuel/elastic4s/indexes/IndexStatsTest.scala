package com.sksamuel.elastic4s.indexes

import com.sksamuel.elastic4s.RefreshPolicy
import com.sksamuel.elastic4s.http.ElasticDsl
import com.sksamuel.elastic4s.http.index.Stats
import com.sksamuel.elastic4s.testkit.DiscoveryLocalNodeProvider
import org.scalatest.{FlatSpec, Inspectors, Matchers}

class IndexStatsTest
  extends FlatSpec
    with Matchers
    with DiscoveryLocalNodeProvider
    with ElasticDsl
    with Inspectors {

  http.execute {
    bulk(
      indexInto("indexstats1/landmarks").fields("name" -> "hampton court palace"),
      indexInto("indexstats2/landmarks").fields("name" -> "tower of london"),
      indexInto("indexstats3/landmarks").fields("name" -> "fountains abbey")
    ).refresh(RefreshPolicy.Immediate)
  }.await

  "index stats" should "return all indexes" in {
    val stats = http.execute {
      indexStats()
    }.await.right.get.result

    def testStats(stats: Seq[Stats]): Unit = {
      stats.map(_.docs.count).sum > 0 shouldBe true
      stats.map(_.store.sizeInBytes).sum > 0 shouldBe true
      stats.map(_.translog.operations).sum > 0 shouldBe true
      stats.map(_.segments.count).sum > 0 shouldBe true
      stats.map(_.segments.memoryInBytes).sum > 0 shouldBe true
      stats.map(_.segments.normsMemoryInBytes).sum > 0 shouldBe true
      stats.map(_.segments.storedFieldsMemoryInBytes).sum > 0 shouldBe true
    }

    testStats(Seq(stats.all.primaries, stats.all.total))
    testStats(stats.indices.values.map(_.primaries).toList ++ stats.indices.values.map(_.total))
  }

}
