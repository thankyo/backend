package com.clemble.thank.util

import reactivemongo.bson.BSONObjectID

object IDGenerator {

  def generate() = BSONObjectID.generate().stringify

}
