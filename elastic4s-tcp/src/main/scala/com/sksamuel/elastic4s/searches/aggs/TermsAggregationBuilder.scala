package com.sksamuel.elastic4s.searches.aggs

import com.sksamuel.elastic4s.{EnumConversions, ScriptBuilder}
import org.elasticsearch.search.aggregations.bucket.terms.{IncludeExclude, TermsAggregationBuilder}
import org.elasticsearch.search.aggregations.{AggregationBuilders, BucketOrder}

import scala.collection.JavaConverters._

object TermsAggregationBuilder {

  private def toBucketOrder(termsOrder: TermsOrder): BucketOrder = {
    termsOrder match {
      case TermsOrder("_count", asc) => BucketOrder.count(asc)
      case TermsOrder("_term", asc) => BucketOrder.key(asc)
      case TermsOrder(field, asc) if field.contains(".") =>
        val parts = field.split('.')
        BucketOrder.aggregation(parts(0), parts(1), asc)
      case TermsOrder(field, asc) => BucketOrder.aggregation(field, asc)
    }
  }

  def apply(agg: TermsAggregationDefinition): TermsAggregationBuilder = {

    val builder = AggregationBuilders.terms(agg.name)

    agg.field.foreach(builder.field)
    agg.collectMode.map(EnumConversions.collectMode).foreach(builder.collectMode)
    agg.executionHint.foreach(builder.executionHint)
    agg.includeExclude.foreach { it =>
      val inc = if (it.include.isEmpty) null else it.include.toArray
      val exc = if (it.exclude.isEmpty) null else it.exclude.toArray
      builder.includeExclude(new IncludeExclude(inc, exc))
    }
    agg.includePartition.foreach { it => builder.includeExclude(new IncludeExclude(it.partition, it.numPartitions)) }
    agg.minDocCount.foreach(builder.minDocCount)
    agg.missing.foreach(builder.missing)
    agg.orders match {
      case order if order.isEmpty =>
      case Seq(order) => builder.order(toBucketOrder(order))
      case _ => builder.order(BucketOrder.compound(agg.orders.map(toBucketOrder): _*))
    }
    agg.script.map(ScriptBuilder.apply).foreach(builder.script)
    agg.shardSize.foreach(builder.shardSize)
    agg.showTermDocCountError.foreach(builder.showTermDocCountError)
    agg.size.foreach(builder.size)
    agg.valueType.map(EnumConversions.valueType).foreach(builder.valueType)

    SubAggsFn(builder, agg.subaggs)
    if (agg.metadata.nonEmpty) builder.setMetaData(agg.metadata.asJava)

    builder
  }
}
