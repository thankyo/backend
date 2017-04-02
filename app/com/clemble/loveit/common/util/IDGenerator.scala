package com.clemble.loveit.common.util

import reactivemongo.bson.BSONObjectID

object IDGenerator {

  def generate() = BSONObjectID.generate().stringify

}
