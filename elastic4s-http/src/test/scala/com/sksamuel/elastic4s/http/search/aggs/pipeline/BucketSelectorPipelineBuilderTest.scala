package com.sksamuel.elastic4s.http.search.aggs.pipeline

import com.sksamuel.elastic4s.searches.aggs.pipeline.BucketSelectorDefinition
import org.scalatest.{FunSuite, Matchers}

class BucketSelectorPipelineBuilderTest extends FunSuite with Matchers {
  test("bucket selector pipeline aggregation should generate expected json") {
    val q = BucketSelectorDefinition("bucket_selector_test", "params.totalSales > 200", Map("totalSales"->"total_sales"))
    BucketSelectorPipelineBuilder(q).string() shouldBe
      """{"bucket_selector":{"buckets_path":{"totalSales":"total_sales"},"script":{"source":"params.totalSales > 200"}}}"""
  }}
