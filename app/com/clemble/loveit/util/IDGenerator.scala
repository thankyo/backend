package com.clemble.loveit.util

import reactivemongo.bson.BSONObjectID

object IDGenerator {

  def generate() = BSONObjectID.generate().stringify

}
