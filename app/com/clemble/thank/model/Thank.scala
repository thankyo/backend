package com.clemble.thank.model

import com.clemble.thank.util.URIUtils
import play.api.libs.json.Json

/**
  * Thank abstraction
  */
case class Thank(
                  uri: String,
                  given: Amount = 0L
                ) {

  def withParents(): List[Thank] = {
    URIUtils.toParents(uri).map(Thank(_))
  }

}

object Thank {

  /**
    * JSON format for [[Thank]]
    */
  implicit val jsonFormat = Json.format[Thank]

}