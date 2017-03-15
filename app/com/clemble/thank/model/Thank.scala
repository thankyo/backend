package com.clemble.thank.model

import play.api.libs.json.Json

/**
  * Thank abstraction
  */
case class Thank(
                  resource: Resource,
                  given: Amount = 0L
                ) {

  def withParents(): List[Thank] = {
    resource.parents().map(Thank(_))
  }

}

object Thank {

  /**
    * JSON format for [[Thank]]
    */
  implicit val jsonFormat = Json.format[Thank]

}