package com.clemble.loveit.common.util

import reactivemongo.bson.BSONObjectID

object IDGenerator {

  val ZERO = BSONObjectID.fromTime(0).stringify

  def generate() = BSONObjectID.generate().stringify

}
