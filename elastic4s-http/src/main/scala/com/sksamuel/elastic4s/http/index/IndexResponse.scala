package com.sksamuel.elastic4s.http.index

import com.fasterxml.jackson.annotation.JsonProperty
import com.sksamuel.elastic4s.DocumentRef
import com.sksamuel.elastic4s.http.Shards

case class IndexResponse(@JsonProperty("_id") id: String,
                         @JsonProperty("_index") index: String,
                         @JsonProperty("_type") `type`: String,
                         @JsonProperty("_version") version: Long,
                         result: String,
                         @JsonProperty("forced_refresh") forcedRefresh: Boolean,
                         shards: Shards) {
  def ref = DocumentRef(index, `type`, id)
}
